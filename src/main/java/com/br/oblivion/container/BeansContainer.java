package com.br.oblivion.container;

import com.br.oblivion.annotations.OblivionConstructorInject;
import com.br.oblivion.annotations.OblivionPrimary;
import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class BeansContainer {
  private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
  private Map<String, PrototypeBeanMetadata> prototypeBeans = new ConcurrentHashMap<>();

  // lifecycle methods
  private static List<Pair<Object, Method>> preDestroyMethods = new ArrayList<>();
  private static List<Pair<Object, Method>> preShutdownMethods = new ArrayList<>();
  private static List<Pair<Object, Method>> postShutdownMethods = new ArrayList<>();

  // classes loaded from oblivion.config
  public static Map<String, Class<?>> configBeans = new HashMap<>();

  public <T> void registerSingletonBean(String identifier, T bean) {
    singletonBeans.put(identifier, bean);
  }

  public <T> void registerPrototypeBean(
      String identifier, PrototypeBeanMetadata prototypeBeanMetadata) {
    prototypeBeans.put(identifier, prototypeBeanMetadata);
  }

  public Object getSingletonBean(String identifier) {
    Object singletonBean = singletonBeans.get(identifier);

    if (singletonBean != null) {
      return singletonBean;
    }

    return null;
  }

  // beans loaded from oblivion.config file
  public static void registerConfigBean(String identifier, Class<?> clazz) {
    configBeans.put(identifier, clazz);
  }

  public static Map<String, Class<?>> getConfigBeans() {
    return configBeans;
  }

  public static void registerPreDestroyMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preDestroyMethod = new Pair<Object, Method>(instantiatedClass, method);
    preDestroyMethods.add(preDestroyMethod);
  }

  public static void registerPreShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    preShutdownMethods.add(preShutdownMethod);
  }

  public static void registerPostShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> postShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    postShutdownMethods.add(postShutdownMethod);
  }

  Set<Class<?>> currentlyCreatingBeans = new HashSet<Class<?>>();

  public Object resolveDependency(
      Class<?> clazzToResolve,
      String classIdentifier,
      ThreadPoolExecutor threadPoolExecutor,
      Set<Class<?>> scannedClasses)
      throws Exception {

    System.out.println("CURR CLASS RESOLVEDEPENDENCY: " + clazzToResolve.getSimpleName());

    boolean isPrototypeBean = false;
    if (clazzToResolve.isAnnotationPresent(OblivionPrototype.class)) {
      isPrototypeBean = true;
    }

    if (!isPrototypeBean) {
      Object existingInstance = getSingletonBean(clazzToResolve.getName());

      if (existingInstance != null) {
        return existingInstance;
      }
    }

    if (clazzToResolve.isInterface()) {
      System.out.println("is interface -> " + clazzToResolve.getSimpleName());

      Class<?> primaryImplementation = null;
      int primaryCount = 0;

      for (Class<?> clazz : scannedClasses) {
        if (clazzToResolve.isAssignableFrom(clazz)) {
          if (clazz.isAnnotationPresent(OblivionPrimary.class)) {
            primaryImplementation = clazz;
            primaryCount++;
          }
        }
      }

      if (primaryCount == 1) {
        return resolveDependency(
            primaryImplementation,
            primaryImplementation.getName(),
            threadPoolExecutor,
            scannedClasses);
      } else if (primaryCount == 0) {
        throw new OblivionException(
            String.format(
                "Dependency not satisfied for interface '%s'. Is there a class implementing this"
                    + " interface annotated with @OblivionPrimary?",
                clazzToResolve.getName()));
      } else {
        throw new OblivionException(
            String.format(
                "Ambiguos dependency for interface '%s'. Multiple implementations annotated with"
                    + " @OblivionPrimary found!",
                clazzToResolve.getName()));
      }
    } else {

      if (!clazzToResolve.isAnnotationPresent(OblivionService.class)) {
        throw new OblivionException(
            String.format(
                "Cannot resolve class '%s'! Is this class annotated with '@OblivionService'?",
                clazzToResolve.getSimpleName()));
      }

      if (currentlyCreatingBeans.contains(clazzToResolve)) {
        throw new OblivionException(
            String.format(
                "Possible circular dependency found in class: '%s'. A circular dependency is"
                    + " something like this: A -> B -> A. A class that depends on another class,"
                    + " that depends on the previous class.\n"
                    + " Make sure to double check your classes annotated with @OblivionService",
                clazzToResolve.getSimpleName()));
      }

      currentlyCreatingBeans.add(clazzToResolve);

      try {
        ReflectionUtils.runPreInitializationMethods(clazzToResolve);

        Constructor<?> constructorToUse = findInjectableConstructor(clazzToResolve);
        Parameter[] parameters = constructorToUse.getParameters();
        Object newlyCreatedInstance;

        if (parameters.length == 0) {
          newlyCreatedInstance = constructorToUse.newInstance();
        } else {
          List<Object> resolvedArguments = new ArrayList<>();

          for (Parameter parameter : parameters) {
            Class<?> dependencyClass = parameter.getType();
            System.out.println("log before rec: ");
            Object dependencyInstance =
                resolveDependency(
                    dependencyClass, classIdentifier, threadPoolExecutor, scannedClasses);

            resolvedArguments.add(dependencyInstance);
          }

          newlyCreatedInstance = constructorToUse.newInstance(resolvedArguments.toArray());
        }

        if (isPrototypeBean == false) {
          System.out.println("isPrototypeBean: " + isPrototypeBean);
          System.out.println("registering singleton bean");
          registerSingletonBean(clazzToResolve.getSimpleName(), newlyCreatedInstance);
        }

        System.out.println("before reflectiojn");

        ReflectionUtils.runPostConstructMethods(
            clazzToResolve, newlyCreatedInstance, threadPoolExecutor);
        ReflectionUtils.runPostInitializationMethods(
            clazzToResolve, newlyCreatedInstance, threadPoolExecutor);
        ReflectionUtils.registerPersistentBeanLifecycles(clazzToResolve, newlyCreatedInstance);

        return newlyCreatedInstance;
      } finally {
        currentlyCreatingBeans.remove(clazzToResolve);
      }
    }
  }

  private Constructor<?> findInjectableConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    Optional<Constructor<?>> constructorToInject =
        Arrays.stream(constructors)
            .filter(c -> c.isAnnotationPresent(OblivionConstructorInject.class))
            .findFirst();
    return constructorToInject.orElse(constructors[0]);
  }

  public Map<?, Object> getAllSingletonBeans() {
    if (singletonBeans.isEmpty()) {
      return null;
    }
    return singletonBeans;
  }

  public Map<String, PrototypeBeanMetadata> getAllPrototypenBeans() {
    if (prototypeBeans.isEmpty()) {
      return null;
    }
    return prototypeBeans;
  }

  public List<Pair<Object, Method>> getPreDestroyMethods() {
    return this.preDestroyMethods;
  }

  public List<Pair<Object, Method>> getPreShutdownMethods() {
    return this.preShutdownMethods;
  }

  public List<Pair<Object, Method>> getPostShutdownMethods() {
    return this.postShutdownMethods;
  }

  public void clearPreDestroyMap() {
    this.preDestroyMethods.clear();
  }

  public void clearPreShutdownMap() {
    this.preShutdownMethods.clear();
  }

  public void clearPostShutdownMap() {
    this.postShutdownMethods.clear();
  }

  public void clearSingletonBeansMap() {
    this.singletonBeans.clear();
  }

  public void clearPrototypeBeansMap() {
    this.prototypeBeans.clear();
  }
}

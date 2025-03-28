package com.br.oblivion.container;

import com.br.oblivion.annotations.OblivionConstructorInject;
import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.exception.OblivionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BeansContainer {
  private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
  private Map<String, PrototypeBeanMetadata> prototypeBeans = new ConcurrentHashMap<>();

  // lifecycle methods
  private List<Pair<Object, Method>> preDestroyMethods = new ArrayList<>();
  private List<Pair<Object, Method>> preShutdownMethods = new ArrayList<>();
  private List<Pair<Object, Method>> postShutdownMethods = new ArrayList<>();

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

  public void registerPreDestroyMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preDestroyMethod = new Pair<Object, Method>(instantiatedClass, method);
    preDestroyMethods.add(preDestroyMethod);
  }

  public void registerPreShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    preShutdownMethods.add(preShutdownMethod);
  }

  public void registerPostShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> postShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    postShutdownMethods.add(postShutdownMethod);
  }

  public Object resolveDependency(Class<?> clazzToResolve, String classIdentifier)
      throws Exception {
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

    // here i can handle circular deps
    // i can add the classToResolve to a "currentlyCreating" set, if its already present throw error

    if (!clazzToResolve.isAnnotationPresent(OblivionService.class)) {
      throw new OblivionException(
          String.format(
              "Cannot resolve class '%s'! Is this class annotated with '@OblivionService'?",
              clazzToResolve.getSimpleName()));
    }

    Constructor<?> constructorToUse = findInjectableConstructor(clazzToResolve);
    Parameter[] parameters = constructorToUse.getParameters();
    Object newlyCreatedInstance;

    if (parameters.length == 0) {
      newlyCreatedInstance = constructorToUse.newInstance();
    } else {
      List<Object> resolvedArguments = new ArrayList<>();

      for (Parameter parameter : parameters) {
        Class<?> dependencyClass = parameter.getType();
        Object dependencyInstance = resolveDependency(dependencyClass, classIdentifier);
        resolvedArguments.add(dependencyInstance);
      }

      newlyCreatedInstance = constructorToUse.newInstance(resolvedArguments.toArray());
    }

    if (isPrototypeBean == false) {
      registerSingletonBean(clazzToResolve.getSimpleName(), newlyCreatedInstance);
    }

    // i think i can handle lifecycle steps here
    // like those runPostConstructMethods, initializeFields, etc...

    // also if i add that currentlyCreating set, here i think i should remove it

    return newlyCreatedInstance;
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

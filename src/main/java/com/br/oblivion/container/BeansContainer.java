package com.br.oblivion.container;

import com.br.oblivion.annotations.OblivionConstructorInject;
import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionQualifier;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.interfaces.OblivionBeanPostProcessor;
import com.br.oblivion.util.ReflectionUtils;
import java.lang.annotation.Annotation;
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

  public final List<OblivionBeanPostProcessor> postProcessorBeans = new ArrayList<>();

  // lifecycle methods
  private static List<Pair<Object, Method>> preDestroyMethods = new ArrayList<>();
  private static List<Pair<Object, Method>> preShutdownMethods = new ArrayList<>();
  private static List<Pair<Object, Method>> postShutdownMethods = new ArrayList<>();

  // classes loaded from oblivion.config
  public static Map<String, Class<?>> configBeans = new HashMap<>();

  // AOP advices
  public static Map<String, List<Method>> beforeAdviceMap = new ConcurrentHashMap<>();
  public static Map<String, List<Method>> afterAdviceMap = new ConcurrentHashMap<>();
  public static Map<String, List<Method>> afterThrowingAdviceMap = new ConcurrentHashMap<>();

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
      Set<Class<?>> scannedClasses,
      String requestQualifier,
      Boolean isPostProcessor)
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

    String qualifierName = null;

    if (clazzToResolve.isInterface()) {
      Class<?> foundImplementation = null;
      int implementationsFound = 0;

      if (requestQualifier != null && !requestQualifier.trim().isEmpty()) {
        for (Class<?> clazz : scannedClasses) {
          if (clazzToResolve.isAssignableFrom(clazz)) {
            OblivionService serviceAnnotation = clazz.getAnnotation(OblivionService.class);
            String qualifyName = null;

            if (serviceAnnotation != null & serviceAnnotation.name() != null
                && !serviceAnnotation.name().trim().isEmpty()) {
              qualifyName = serviceAnnotation.name();
            }

            if (requestQualifier.equals(qualifyName)) {
              foundImplementation = clazz;
              implementationsFound++;
            }
          }
        }

        if (implementationsFound == 1) {
          return resolveDependency(
              foundImplementation,
              foundImplementation.getName(),
              threadPoolExecutor,
              scannedClasses,
              null,
              false);
        } else if (implementationsFound == 0) {
          throw new OblivionException(
              String.format(
                  "Dependency not satisfied for interface '%s'. Is there a bean with qualifier"
                      + " '%s'?",
                  clazzToResolve.getName(), requestQualifier));
        } else {
          throw new OblivionException(
              String.format(
                  "Ambiguos dependency for interface '%s'. Multiple implementations found with"
                      + " qualifier '%s' @OblivionPrimary found!",
                  clazzToResolve.getName(), requestQualifier));
        }

      } else {
        Class<?> primaryImplementation = null;
        int primaryCount = 0;

        for (Class<?> clazz : scannedClasses) {
          if (clazzToResolve.isAssignableFrom(clazz)) {
            if (clazzToResolve.isAnnotationPresent(OblivionQualifier.class)) {
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
              scannedClasses,
              qualifierName,
              false);
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

        for (Parameter p : parameters) {
          Annotation[] annotations = p.getAnnotations();
          for (Annotation annotation : annotations) {
            if (annotation instanceof OblivionQualifier) {
              OblivionQualifier qualifier = (OblivionQualifier) annotation;
              qualifierName = qualifier.name();
            }
          }
        }

        Object newlyCreatedInstance;

        if (parameters.length == 0) {
          newlyCreatedInstance = constructorToUse.newInstance();
        } else {
          List<Object> resolvedArguments = new ArrayList<>();

          for (Parameter parameter : parameters) {
            Class<?> dependencyClass = parameter.getType();
            Object dependencyInstance =
                resolveDependency(
                    dependencyClass,
                    classIdentifier,
                    threadPoolExecutor,
                    scannedClasses,
                    qualifierName,
                    false);

            resolvedArguments.add(dependencyInstance);
          }

          newlyCreatedInstance = constructorToUse.newInstance(resolvedArguments.toArray());
        }

        if (isPrototypeBean == false) {
          registerSingletonBean(clazzToResolve.getSimpleName(), newlyCreatedInstance);
        }

        Object currentBeanInstance = newlyCreatedInstance;

        if (isPostProcessor == true) {
          if (newlyCreatedInstance instanceof OblivionBeanPostProcessor) {
            postProcessorBeans.add((OblivionBeanPostProcessor) newlyCreatedInstance);
          }
        }

        // apply BEFORE initialization post processors
        for (OblivionBeanPostProcessor processor : postProcessorBeans) {
          currentBeanInstance =
              processor.postProcessorBeforeInitialization(
                  currentBeanInstance, currentBeanInstance.getClass().getName());
        }

        ReflectionUtils.runPostConstructMethods(
            clazzToResolve, currentBeanInstance, threadPoolExecutor);
        ReflectionUtils.runPostInitializationMethods(
            clazzToResolve, currentBeanInstance, threadPoolExecutor);

        Object finalBeanInstance = currentBeanInstance;

        // apply AFTER initialization post processors
        for (OblivionBeanPostProcessor processor : postProcessorBeans) {
          finalBeanInstance =
              processor.postProcessorAfterInitialization(
                  finalBeanInstance, finalBeanInstance.getClass().getName());
        }

        ReflectionUtils.registerPersistentBeanLifecycles(clazzToResolve, finalBeanInstance);

        return finalBeanInstance;
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
    if (singletonBeans.isEmpty()) return null;

    return singletonBeans;
  }

  public Map<String, PrototypeBeanMetadata> getAllPrototypenBeans() {
    if (prototypeBeans.isEmpty()) return null;

    return prototypeBeans;
  }

  public List<OblivionBeanPostProcessor> getAllPostProcessorBeans() {
    if (postProcessorBeans.isEmpty()) return null;

    return postProcessorBeans;
  }

  public List<Pair<Object, Method>> getPreDestroyMethods() {
    return preDestroyMethods;
  }

  public List<Pair<Object, Method>> getPreShutdownMethods() {
    return preShutdownMethods;
  }

  public List<Pair<Object, Method>> getPostShutdownMethods() {
    return postShutdownMethods;
  }

  public void clearPreDestroyMap() {
    preDestroyMethods.clear();
  }

  public void clearPreShutdownMap() {
    preShutdownMethods.clear();
  }

  public void clearPostShutdownMap() {
    postShutdownMethods.clear();
  }

  public void clearSingletonBeansMap() {
    this.singletonBeans.clear();
  }

  public void clearPrototypeBeansMap() {
    this.prototypeBeans.clear();
  }
}

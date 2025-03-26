package com.br.oblivion.bean;

import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.exception.OblivionException;

// TODO: gotta refactor the 'init inner deps' logic
public class SingletonBean {
  // public <T> void initializeSingletonBean(
  //     String identifier,
  //     Class<T> clazz,
  //     BeansContainer beansContainer,
  //     ThreadPoolExecutor threadPoolExecutor)
  //     throws Exception {
  //   if (clazz.isAnnotationPresent(OblivionService.class)) {
  //     Constructor<?>[] ctors = clazz.getDeclaredConstructors();
  //     try {
  //       ReflectionUtils.runPreInitializationMethods(clazz);
  //     } catch (Exception ex) {
  //       throw new OblivionException(
  //           String.format(
  //               "Error during pre-initialization of class '%s': %s",
  //               clazz.getSimpleName(), ex.getMessage()),
  //           ex);
  //     }
  //   }
  // }

  public <T> Object injectConstructor(
      Class<T> clazz, String classIdentifier, BeansContainer beansContainer) throws Exception {

    if (beansContainer.getSingletonBean(clazz.getSimpleName()) != null) {
      return null;
    }

    try {
      return beansContainer.resolveDependency(clazz, classIdentifier);

      // NOTE: registration and lifecycle calls should now happen *inside* resolveDependency

      // if resolveDependency doesn't register, i might register here, but it's
      // cleaner if resolveDependency handles its own registration upon successful
      // creation like i plan to do.
      // beansContainer.registerSingletonBean(identifier, instance);

      // Initial field injection and post-init might still happen here,
      // OR preferably inside resolveDependency after instantiation and registration.
      // ReflectionUtils.runPostInitializationMethods(clazz, instance, threadPoolExecutor);
      // ReflectionUtils.registerPersistentBeanLifecycles(clazz, instance, beansContainer);

      // NOTE: OLD CODE BELOW
      //
      // if (ctor.getParameterCount() == 0) {
      //   // NOTE: newInstance is deprecated, gotta see other way to do it
      //   T init = clazz.newInstance();
      //   ReflectionUtils.runPostConstructMethods(clazz, init, threadPoolExecutor);
      //   beansContainer.registerSingletonBean(identifier, init);
      //   ReflectionUtils.runPostInitializationMethods(clazz, init, threadPoolExecutor);
      //   ReflectionUtils.registerPersistentBeanLifecycles(clazz, init, beansContainer);
      //
      // } else {
      //   Parameter[] params = ctor.getParameters();
      //
      //   // required params to use inside getDeclaredConstructor
      //   List<Class<?>> requiredParams = new ArrayList<>();
      //   // required objects to use inside newInstance
      //   List<Object> requiredObjects = new ArrayList<>();
      //
      //   // required inner dependencies
      //   List<Class<?>> requiredInnerParams = new ArrayList<>();
      //   List<Object> requiredInnerObjects = new ArrayList<>();
      //
      //   Class<?> currInnerClass = null;
      //
      //   for (Parameter p : params) {
      //     try {
      //       Class<?> paramType = p.getType();
      //       String paramName = p.getType().getName();
      //
      //       currInnerClass = paramType;
      //
      //       Constructor<?>[] innerCtors = paramType.getDeclaredConstructors();
      //
      //       for (Constructor<?> c : innerCtors) {
      //         Parameter[] innerParams = c.getParameters();
      //
      //         for (Parameter innerP : innerParams) {
      //           Class<?> innerParamType = innerP.getType();
      //
      //           if (!innerParamType.isAnnotationPresent(OblivionService.class)) {
      //             throw new OblivionException(
      //                 String.format(
      //                     "Unsatisfied Dependency for class '%s'! Is '@OblivionService' present
      // in"
      //                         + " the '%s' class?",
      //                     innerParamType, innerParamType.getSimpleName()));
      //           }
      //
      //           String innerParamName = innerP.getType().getName();
      //           Object initInnerParam = beansContainer.getSingletonBean(innerParamName);
      //
      //           if (initInnerParam == null) {
      //             System.out.println("null");
      //             initInnerParam = innerParamType.newInstance();
      //             ReflectionUtils.runPostConstructMethods(
      //                 innerParamType, initInnerParam, threadPoolExecutor);
      //             beansContainer.registerSingletonBean(innerParamName, initInnerParam);
      //
      //             requiredInnerParams.add(innerParamType);
      //             requiredInnerObjects.add(beansContainer.getSingletonBean(innerParamName));
      //           } else {
      //             System.out.println("not null man");
      //             requiredInnerParams.add(innerParamType);
      //             requiredInnerObjects.add(beansContainer.getSingletonBean(innerParamName));
      //           }
      //         }
      //       }
      //
      //       Class<?>[] requiredInnerParamsArr = requiredInnerParams.toArray(new Class<?>[0]);
      //       Object[] requiredInnerObjectsArr = requiredInnerObjects.toArray(new Object[0]);
      //
      //       try {
      //         Object initInnerClass =
      //             currInnerClass
      //                 .getDeclaredConstructor(requiredInnerParamsArr)
      //                 .newInstance(requiredInnerObjectsArr);
      //
      //         String innerIdentifier = identifier + currInnerClass.toString();
      //         beansContainer.registerSingletonBean(innerIdentifier, initInnerClass);
      //         ReflectionUtils.runPostConstructMethods(
      //             currInnerClass, initInnerClass, threadPoolExecutor);
      //         ReflectionUtils.runPostInitializationMethods(
      //             currInnerClass, initInnerClass, threadPoolExecutor);
      //         ReflectionUtils.registerPersistentBeanLifecycles(
      //             currInnerClass, initInnerClass, beansContainer);
      //
      //       } catch (Exception ex) {
      //         throw new Exception("Failed to init inner deps: " + ex.getMessage());
      //       }
      //
      //       Class<?>[] requiredInnerParamsArr2 = requiredInnerParams.toArray(new Class<?>[0]);
      //       Object[] requiredInnerObjectsArr2 = requiredInnerObjects.toArray(new Object[0]);
      //
      //       Object initParam =
      //           paramType
      //               .getDeclaredConstructor(requiredInnerParamsArr2)
      //               .newInstance(requiredInnerObjectsArr2);
      //
      //       beansContainer.registerSingletonBean(paramName, initParam);
      //       requiredParams.add(paramType);
      //       requiredObjects.add(beansContainer.getSingletonBean(paramName));
      //
      //     } catch (InstantiationException | IllegalAccessException ex) {
      //       throw new OblivionException(
      //           String.format(
      //               "Error instantiating parameter '%s' for constructor of '%s': %s",
      //               p.getType().getName(), clazz.getSimpleName(), ex.getMessage()));
      //     }
      //   }
      //
      //   Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
      //   Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);
      //
      //   try {
      //     T initClass =
      //         clazz.getDeclaredConstructor(requiredParamsArr).newInstance(requiredObjectsArr);
      //     beansContainer.registerSingletonBean(identifier, initClass);
      //     ReflectionUtils.runPostConstructMethods(clazz, initClass, threadPoolExecutor);
      //     ReflectionUtils.runPostInitializationMethods(clazz, initClass, threadPoolExecutor);
      //     ReflectionUtils.registerPersistentBeanLifecycles(clazz, initClass, beansContainer);
      //   } catch (NoSuchMethodException
      //       | InstantiationException
      //       | IllegalAccessException
      //       | InvocationTargetException ex) {
      //     throw new OblivionException(
      //         String.format(
      //             "Error invoking constructor for class '%s': %s",
      //             clazz.getSimpleName(), ex.getMessage()));
      //   }
      // }
    } catch (Exception ex) {
      throw new OblivionException("Error during singleton bean initialization: " + ex.getMessage());
    }
  }
}

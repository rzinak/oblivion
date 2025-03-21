package com.br.oblivion.bean;

import com.br.oblivion.annotations.OblivionConstructorInject;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

// TODO: gotta refactor the 'init inner deps' logic
public class SingletonBean {
  public <T> void initializeSingletonBean(
      String identifier,
      Class<T> clazz,
      BeansContainer beansContainer,
      ThreadPoolExecutor threadPoolExecutor)
      throws Exception {
    if (clazz.isAnnotationPresent(OblivionService.class)) {
      Constructor<?>[] ctors = clazz.getDeclaredConstructors();
      try {
        ReflectionUtils.runPreInitializationMethods(clazz);
      } catch (Exception ex) {
        throw new OblivionException(
            String.format(
                "Error during pre-initialization of class '%s': %s",
                clazz.getSimpleName(), ex.getMessage()),
            ex);
      }

      Optional<Constructor<?>> constructorToInject =
          Arrays.stream(ctors)
              .filter(c -> c.isAnnotationPresent(OblivionConstructorInject.class))
              .findFirst();

      // NOTE: constructors are filtered to find the one with the @OblivionInject
      // annotation, if no one is found, it uses the first constructor inside the class
      if (constructorToInject.isEmpty()) {
        injectConstructor(ctors[0], identifier, clazz, beansContainer, threadPoolExecutor);
      } else {
        Constructor<?> ctor = constructorToInject.orElse(ctors[0]);
        injectConstructor(ctor, identifier, clazz, beansContainer, threadPoolExecutor);
      }
    }
  }

  private <T> void injectConstructor(
      Constructor<?> ctor,
      String identifier,
      Class<T> clazz,
      BeansContainer beansContainer,
      ThreadPoolExecutor threadPoolExecutor)
      throws Exception {
    try {
      if (ctor.getParameterCount() == 0) {
        // NOTE: newInstance is deprecated, gotta see other way to do it
        T init = clazz.newInstance();
        ReflectionUtils.runPostConstructMethods(clazz, init, threadPoolExecutor);
        beansContainer.registerSingletonBean(identifier, init);
        ReflectionUtils.initializeFields(beansContainer.getSingletonBean(identifier));
        ReflectionUtils.runPostInitializationMethods(clazz, init, threadPoolExecutor);
        ReflectionUtils.registerPersistentBeanLifecycles(clazz, init, beansContainer);

      } else {
        Parameter[] params = ctor.getParameters();

        // required params to use inside getDeclaredConstructor
        List<Class<?>> requiredParams = new ArrayList<>();
        // required objects to use inside newInstance
        List<Object> requiredObjects = new ArrayList<>();

        // required inner dependencies
        List<Class<?>> requiredInnerParams = new ArrayList<>();
        List<Object> requiredInnerObjects = new ArrayList<>();

        Class<?> currInnerClass = null;

        for (Parameter p : params) {
          try {
            Class<?> paramType = p.getType();
            String paramName = p.getType().getName();

            currInnerClass = paramType;

            Constructor<?>[] innerCtors = paramType.getDeclaredConstructors();

            for (Constructor<?> c : innerCtors) {
              Parameter[] innerParams = c.getParameters();

              for (Parameter innerP : innerParams) {
                Class<?> innerParamType = innerP.getType();
                String innerParamName = innerP.getType().getName();
                Object initInnerParam = innerParamType.newInstance();
                ReflectionUtils.runPostConstructMethods(
                    innerParamType, initInnerParam, threadPoolExecutor);
                beansContainer.registerSingletonBean(innerParamName, initInnerParam);
                ReflectionUtils.initializeFields(beansContainer.getSingletonBean(innerParamName));
                requiredInnerParams.add(innerParamType);
                requiredInnerObjects.add(beansContainer.getSingletonBean(innerParamName));
              }
            }

            Class<?>[] requiredInnerParamsArr = requiredInnerParams.toArray(new Class<?>[0]);
            Object[] requiredInnerObjectsArr = requiredInnerObjects.toArray(new Object[0]);

            try {

              Object initInnerClass =
                  currInnerClass
                      .getDeclaredConstructor(requiredInnerParamsArr)
                      .newInstance(requiredInnerObjectsArr);

              String innerIdentifier = identifier + currInnerClass.toString();
              beansContainer.registerSingletonBean(innerIdentifier, initInnerClass);
              ReflectionUtils.runPostConstructMethods(
                  currInnerClass, initInnerClass, threadPoolExecutor);
              ReflectionUtils.initializeFields(beansContainer.getSingletonBean(innerIdentifier));
              ReflectionUtils.runPostInitializationMethods(
                  currInnerClass, initInnerClass, threadPoolExecutor);
              ReflectionUtils.registerPersistentBeanLifecycles(
                  currInnerClass, initInnerClass, beansContainer);

            } catch (Exception ex) {
              throw new Exception("Failed to init inner deps: " + ex.getMessage());
            }

            Class<?>[] requiredInnerParamsArr2 = requiredInnerParams.toArray(new Class<?>[0]);
            Object[] requiredInnerObjectsArr2 = requiredInnerObjects.toArray(new Object[0]);

            Object initParam =
                paramType
                    .getDeclaredConstructor(requiredInnerParamsArr2)
                    .newInstance(requiredInnerObjectsArr2);

            ReflectionUtils.runPostConstructMethods(clazz, initParam, threadPoolExecutor);
            beansContainer.registerSingletonBean(paramName, initParam);
            ReflectionUtils.initializeFields(beansContainer.getSingletonBean(paramName));
            requiredParams.add(paramType);
            requiredObjects.add(beansContainer.getSingletonBean(paramName));

          } catch (InstantiationException | IllegalAccessException ex) {
            throw new OblivionException(
                String.format(
                    "Error instantiating parameter '%s' for constructor of '%s': %s",
                    p.getType().getName(), clazz.getSimpleName(), ex.getMessage()));
          }
        }

        Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
        Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

        try {
          T initClass =
              clazz.getDeclaredConstructor(requiredParamsArr).newInstance(requiredObjectsArr);
          beansContainer.registerSingletonBean(identifier, initClass);
          ReflectionUtils.runPostConstructMethods(clazz, initClass, threadPoolExecutor);
          ReflectionUtils.initializeFields(beansContainer.getSingletonBean(identifier));
          ReflectionUtils.runPostInitializationMethods(clazz, initClass, threadPoolExecutor);
          ReflectionUtils.registerPersistentBeanLifecycles(clazz, initClass, beansContainer);
        } catch (NoSuchMethodException
            | InstantiationException
            | IllegalAccessException
            | InvocationTargetException ex) {
          throw new OblivionException(
              String.format(
                  "Error invoking constructor for class '%s': %s",
                  clazz.getSimpleName(), ex.getMessage()));
        }
      }
    } catch (Exception ex) {
      throw new OblivionException("Error during singleton bean initialization: " + ex.getMessage());
    }
  }
}

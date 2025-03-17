package com.br.autowired.bean;

import com.br.autowired.annotations.OblivionConstructorInject;
import com.br.autowired.annotations.OblivionService;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.exception.OblivionException;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SingletonBean {
  public <T> void initializeSingletonBean(
      String identifier, Class<T> clazz, BeansContainer beansContainer) throws Exception {
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
        injectConstructor(ctors[0], identifier, clazz, beansContainer);
      } else {
        Constructor<?> ctor = constructorToInject.orElse(ctors[0]);
        injectConstructor(ctor, identifier, clazz, beansContainer);
      }
    }
  }

  private <T> void injectConstructor(
      Constructor<?> ctor, String identifier, Class<T> clazz, BeansContainer beansContainer)
      throws Exception {
    try {
      if (ctor.getParameterCount() == 0) {
        // NOTE: newInstance is deprecated, gotta see other way to do it
        T init = clazz.newInstance();
        ReflectionUtils.runPostConstructMethods(clazz, init, beansContainer.threadExecutor);
        beansContainer.registerSingletonBean(identifier, init);
        ReflectionUtils.initializeFields(beansContainer.getSingletonBean(identifier));
        ReflectionUtils.runPostInitializationMethods(clazz, init, beansContainer.threadExecutor);
        ReflectionUtils.registerPersistentBeanLifecycles(clazz, init, beansContainer);

      } else {
        Parameter[] params = ctor.getParameters();

        // required params to use inside getDeclaredConstructor
        List<Class<?>> requiredParams = new ArrayList<>();
        // required objects to use inside newInstance
        List<Object> requiredObjects = new ArrayList<>();

        for (Parameter p : params) {
          try {
            Class<?> paramType = p.getType();
            String paramName = p.getType().getName();
            Object initParam = paramType.newInstance();
            ReflectionUtils.runPostConstructMethods(
                clazz, initParam, beansContainer.threadExecutor);
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
          ReflectionUtils.runPostConstructMethods(clazz, initClass, beansContainer.threadExecutor);
          ReflectionUtils.initializeFields(beansContainer.getSingletonBean(identifier));
          ReflectionUtils.runPostInitializationMethods(
              clazz, initClass, beansContainer.threadExecutor);
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

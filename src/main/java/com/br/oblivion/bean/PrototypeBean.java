package com.br.oblivion.bean;

import com.br.oblivion.annotations.OblivionConstructorInject;
import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.container.PrototypeBeanMetadata;
import com.br.oblivion.exception.OblivionException;
import com.br.oblivion.util.ReflectionUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO: refactor this code
public class PrototypeBean {
  public <T> void registerPrototypeBean(
      String identifier,
      Class<T> clazz,
      BeansContainer beansContainer,
      String constructorIdentifier)
      throws Exception {
    if (clazz.isAnnotationPresent(OblivionPrototype.class)) {
      Constructor<?>[] ctors = clazz.getDeclaredConstructors();

      PrototypeBeanMetadata prototypeBeanMetadata = new PrototypeBeanMetadata();

      try {
        ReflectionUtils.runPreInitializationMethods(clazz);
      } catch (OblivionException ex) {
        throw new OblivionException(
            String.format(
                "Error during pre-initialization of class '%s': %s",
                clazz.getSimpleName(), ex.getMessage()),
            ex);
      }

      Constructor<?> constructorToInject = null;

      for (Constructor<?> ctor : ctors) {
        Annotation annotation = ctor.getAnnotation(OblivionConstructorInject.class);
        if (annotation != null) {
          Method methodName = OblivionConstructorInject.class.getMethod("name");
          String constructorName = (String) methodName.invoke(annotation);
          if (constructorName.equals(constructorIdentifier)) {
            constructorToInject = ctor;
          }
        }
      }

      if (constructorToInject != null) {
        injectConstructor(
            constructorToInject, identifier, clazz, beansContainer, prototypeBeanMetadata);
      } else {
        injectConstructor(ctors[0], identifier, clazz, beansContainer, prototypeBeanMetadata);
      }
    }
  }

  private <T> void injectConstructor(
      Constructor<?> ctor,
      String identifier,
      Class<T> clazz,
      BeansContainer beansContainer,
      PrototypeBeanMetadata prototypeBeanMetadata)
      throws Exception {
    try {
      if (ctor.getParameterCount() == 0) {
        prototypeBeanMetadata.setPrototypeClass(clazz);
        prototypeBeanMetadata.setRequiredParams(null);
        prototypeBeanMetadata.setRequiredObjects(null);
        beansContainer.registerPrototypeBean(identifier, prototypeBeanMetadata);
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

        // TODO: refactor this
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
                Object initInnerParam = innerParamType.newInstance();
                String innerIdentifier = identifier + currInnerClass.toString();
                beansContainer.registerSingletonBean(innerIdentifier, initInnerParam);
                requiredInnerParams.add(innerParamType);
                requiredInnerObjects.add(initInnerParam);
              }
            }

            Class<?>[] requiredInnerParamsArr = requiredInnerParams.toArray(new Class<?>[0]);
            Object[] requiredInnerObjectsArr = requiredInnerObjects.toArray(new Object[0]);

            Object initParam = null;

            try {
              initParam =
                  paramType
                      .getDeclaredConstructor(requiredInnerParamsArr)
                      .newInstance(requiredInnerObjectsArr);
            } catch (Exception ex) {
              throw new Exception(
                  String.format(
                      "Failed to initialize class '%s' -> '%s'", paramType, ex.getMessage()));
            }

            String customDependencyName =
                LocalDateTime.now() + identifier + paramName + clazz.getName();
            beansContainer.registerSingletonBean(customDependencyName, initParam);

            requiredParams.add(paramType);
            requiredObjects.add(beansContainer.getSingletonBean(customDependencyName));
            prototypeBeanMetadata.setPrototypeClass(clazz);

          } catch (InstantiationException | IllegalAccessException ex) {
            throw new OblivionException(
                String.format(
                    "Error instantiating parameter '%s' for constructor of '%s': %s",
                    p.getType().getName(), clazz.getSimpleName(), ex.getMessage()));
          }
        }

        Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
        Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

        prototypeBeanMetadata.setRequiredParams(requiredParamsArr);
        prototypeBeanMetadata.setRequiredObjects(requiredObjectsArr);

        try {
          beansContainer.registerPrototypeBean(identifier, prototypeBeanMetadata);
        } catch (Exception ex) {
          throw new OblivionException(
              String.format(
                  "Error when registering prototype bean '%s': %s",
                  prototypeBeanMetadata.getClass(), ex.getMessage()));
        }
      }
    } catch (Exception ex) {
      throw new OblivionException("Error during prototype bean initialization: " + ex.getMessage());
    }
  }
}

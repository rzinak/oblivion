package com.br.autowired.bean;

import com.br.autowired.annotations.OblivionService;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.exception.OblivionException;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class SingletonBean {
  // TODO: gotta add suport for instantiating multiple constructors here too
  public <T> void initializeSingletonBean(
      String identifier, Class<T> clazz, BeansContainer container) throws Exception {
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

      for (Constructor<?> ctor : ctors) {
        try {
          if (ctor.getParameterCount() == 0) {
            // NOTE: newInstance is deprecated, gotta see other way to do it
            T init = clazz.newInstance();
            ReflectionUtils.runPostConstructMethods(clazz, init);
            container.registerSingletonBean(identifier, init);
            ReflectionUtils.initializeFields(container.getSingletonBean(identifier));
            ReflectionUtils.runPostInitializationMethods(clazz, init);
            ReflectionUtils.registerPersistentBeanLifecycles(clazz, init, container);

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
                ReflectionUtils.runPostConstructMethods(clazz, initParam);
                container.registerSingletonBean(paramName, initParam);
                ReflectionUtils.initializeFields(container.getSingletonBean(paramName));
                requiredParams.add(paramType);
                requiredObjects.add(container.getSingletonBean(paramName));
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
              container.registerSingletonBean(identifier, initClass);
              ReflectionUtils.runPostConstructMethods(clazz, initClass);
              ReflectionUtils.initializeFields(container.getSingletonBean(identifier));
              ReflectionUtils.runPostInitializationMethods(clazz, initClass);
              ReflectionUtils.registerPersistentBeanLifecycles(clazz, initClass, container);
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
          throw new OblivionException(
              "Error during singleton bean initialization: " + ex.getMessage());
        }
      }
    }
  }
}

package com.br.autowired.bean;

import com.br.autowired.annotations.OblivionPrototype;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.container.PrototypeBeanMetadata;
import com.br.autowired.exception.OblivionException;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrototypeBean {
  // TODO: it needs improvement like when we have nested annotations, but i can do that later
  public <T> void registerPrototypeBean(String identifier, Class<T> clazz, BeansContainer container)
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

      for (Constructor<?> ctor : ctors) {
        try {
          if (ctor.getParameterCount() == 0) {
            prototypeBeanMetadata.setPrototypeClass(clazz);
            prototypeBeanMetadata.setRequiredParams(null);
            prototypeBeanMetadata.setRequiredObjects(null);
            container.registerPrototypeBean(identifier, prototypeBeanMetadata);
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
                // even though in this method we are registering prototype beans,
                // a dependency of a prototype bean is still a singleton
                String customDependencyName =
                    LocalDateTime.now() + identifier + paramName + clazz.getName();
                container.registerSingletonBean(customDependencyName, initParam);
                ReflectionUtils.initializeFields(container.getSingletonBean(customDependencyName));
                requiredParams.add(paramType);
                requiredObjects.add(container.getSingletonBean(customDependencyName));
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
              container.registerPrototypeBean(identifier, prototypeBeanMetadata);
            } catch (Exception ex) {
              throw new OblivionException(
                  String.format(
                      "Error when registering prototype bean '%s': %s",
                      prototypeBeanMetadata.getClass(), ex.getMessage()));
            }
          }
        } catch (Exception ex) {
          throw new OblivionException(
              "Error during prototype bean initialization: " + ex.getMessage());
        }
      }
    }
  }
}

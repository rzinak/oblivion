package com.br.autowired.bean;

import com.br.autowired.annotations.OblivionPrototype;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.container.PrototypeBeanMetadata;
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

      for (Constructor<?> ctor : ctors) {
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
            Class<?> paramType = p.getType();
            String paramName = p.getType().getName();
            Object initParam = paramType.newInstance();
            // even though in this method we are registering prototype beans,
            // a dependency of a prototype bean is still a singleton
            String customDependencyName =
                LocalDateTime.now() + identifier + paramName + clazz.getName();
            container.registerSingletonBean(customDependencyName, initParam);
            ReflectionUtils.initializeFieldsAndMethods(
                container.getSingletonBean(customDependencyName), container);
            requiredParams.add(paramType);
            requiredObjects.add(container.getSingletonBean(customDependencyName));
            prototypeBeanMetadata.setPrototypeClass(clazz);
          }

          Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
          Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

          prototypeBeanMetadata.setRequiredParams(requiredParamsArr);
          prototypeBeanMetadata.setRequiredObjects(requiredObjectsArr);

          container.registerPrototypeBean(identifier, prototypeBeanMetadata);
        }
      }
    }
  }
}

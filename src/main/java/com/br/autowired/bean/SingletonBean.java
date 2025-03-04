package com.br.autowired.bean;

import com.br.autowired.annotations.OblivionService;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class SingletonBean {
  // TODO: gotta add suport for instantiating multiple constructors here too
  public <T> void initializeSingletonBean(
      String identifier, Class<T> clazz, BeansContainer container) throws Exception {
    if (clazz.isAnnotationPresent(OblivionService.class)) {
      Constructor<?>[] ctors = clazz.getDeclaredConstructors();

      ReflectionUtils.runPreInitializationMethods(clazz);

      for (Constructor<?> ctor : ctors) {
        if (ctor.getParameterCount() == 0) {
          // NOTE: newInstance is deprecated, gotta see other way to do it
          T init = clazz.newInstance();
          ReflectionUtils.runPostConstrucMethods(clazz, init);
          container.registerSingletonBean(identifier, init);
          ReflectionUtils.initializeFields(container.getSingletonBean(identifier));
          ReflectionUtils.runPostInitializaionMethods(clazz, init);
          ReflectionUtils.registerPersistentBeanLifecycles(clazz, init, container);

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
            ReflectionUtils.runPostConstrucMethods(clazz, initParam);
            container.registerSingletonBean(paramName, initParam);
            ReflectionUtils.initializeFields(container.getSingletonBean(paramName));
            requiredParams.add(paramType);
            requiredObjects.add(container.getSingletonBean(paramName));
          }

          Class<?>[] requiredParamsArr = requiredParams.toArray(new Class<?>[0]);
          Object[] requiredObjectsArr = requiredObjects.toArray(new Object[0]);

          T initClass =
              clazz.getDeclaredConstructor(requiredParamsArr).newInstance(requiredObjectsArr);
          container.registerSingletonBean(identifier, initClass);
          ReflectionUtils.runPostConstrucMethods(clazz, initClass);
          ReflectionUtils.initializeFields(container.getSingletonBean(identifier));
          ReflectionUtils.runPostInitializaionMethods(clazz, initClass);
          ReflectionUtils.registerPersistentBeanLifecycles(clazz, initClass, container);
        }
      }
    }
  }
}

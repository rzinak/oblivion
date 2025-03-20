package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.annotations.OblivionWire;
import com.br.oblivion.bean.PrototypeBean;
import com.br.oblivion.bean.SingletonBean;
import com.br.oblivion.container.BeansContainer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ThreadPoolExecutor;

public class Inject {
  public static void inject(
      Object appInstance,
      SingletonBean singletonBean,
      PrototypeBean prototypeBean,
      BeansContainer beansContainer,
      ThreadPoolExecutor threadPoolExecutor)
      throws Exception {

    Field[] fields = appInstance.getClass().getDeclaredFields();

    for (Field f : fields) {
      if (f.isAnnotationPresent(OblivionWire.class)) {
        String beanType = getBeanType(f.getType(), singletonBean, beansContainer);
        Object beanObject = null;
        if ("singleton".equals(beanType)) {
          singletonBean.initializeSingletonBean(
              f.getName(), f.getType(), beansContainer, threadPoolExecutor);
          beanObject = beansContainer.getSingletonBean(f.getName());
        } else if ("prototype".equals(beanType)) {
          Annotation annotation = f.getAnnotation(OblivionWire.class);
          Method constructorToInjectMethod = OblivionWire.class.getMethod("constructorToInject");
          String constructorIdentifier = (String) constructorToInjectMethod.invoke(annotation);
          prototypeBean.registerPrototypeBean(
              f.getName(), f.getType(), beansContainer, constructorIdentifier);
          beanObject =
              beansContainer.getPrototypeBean(f.getName(), beansContainer, threadPoolExecutor);
        }

        if (beanObject != null) {
          f.setAccessible(true);
          f.set(appInstance, beanObject);
        }
      }
    }
  }

  private static String getBeanType(
      Class<?> clazz, SingletonBean singletonBean, BeansContainer beansContainer) {
    if (clazz.isAnnotationPresent(OblivionService.class)
        && !clazz.isAnnotationPresent(OblivionPrototype.class)) {
      return "singleton";
    }

    if (clazz.isAnnotationPresent(OblivionService.class)
        && clazz.isAnnotationPresent(OblivionPrototype.class)) {
      return "prototype";
    }

    return null;
  }
}

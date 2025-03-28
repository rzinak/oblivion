package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.annotations.OblivionWire;
import com.br.oblivion.bean.PrototypeBean;
import com.br.oblivion.bean.SingletonBean;
import com.br.oblivion.container.BeansContainer;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class Inject {
  public static void inject(
      Object appInstance,
      SingletonBean singletonBean,
      PrototypeBean prototypeBean,
      BeansContainer beansContainer,
      ThreadPoolExecutor threadPoolExecutor)
      throws Exception {

    // NOTE: fields here are the fields declared inside the code, beans referenced
    // in the oblivion.config file wont appear here
    Field[] fields = appInstance.getClass().getDeclaredFields();

    for (Field f : fields) {
      if (f.isAnnotationPresent(OblivionWire.class)) {
        String beanType = getBeanType(f.getType(), singletonBean, beansContainer);
        Object beanObject = null;
        if ("singleton".equals(beanType)) {
          beanObject = singletonBean.injectConstructor(f.getType(), f.getName(), beansContainer);
        } else if ("prototype".equals(beanType)) {
          beanObject = prototypeBean.injectConstructor(f.getType(), f.getName(), beansContainer);
        }
        if (beanObject != null) {
          f.setAccessible(true);
          f.set(appInstance, beanObject);
        }
      }
    }

    // NOTE: this is to inject beans referenced in the oblivion.config file, as they by
    // concept are not native fields in the main function
    for (Map.Entry<String, Class<?>> entry : BeansContainer.getConfigBeans().entrySet()) {
      String beanName = entry.getKey();
      Class<?> beanClass = entry.getValue();
      String beanType = getBeanType(beanClass, singletonBean, beansContainer);

      if ("singleton".equals(beanType)) {
        singletonBean.injectConstructor(beanClass, beanName, beansContainer);
        beansContainer.getSingletonBean(beanName);
      } else if ("prototype".equals(beanType)) {
        // NOTE: since im using a simple file, theres no way to use annotations, and other stuff
        // so later i intend to change to another file format to support all of these features.
        prototypeBean.injectConstructor(beanClass, beanName, beansContainer);
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

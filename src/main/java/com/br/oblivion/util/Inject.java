package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.annotations.OblivionWire;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.interfaces.OblivionBeanPostProcessor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public class Inject {
  public static void inject(
      Object appInstance,
      BeansContainer beansContainer,
      ThreadPoolExecutor threadPoolExecutor,
      String rootPackage)
      throws Exception {

    try {
      // NOTE: fields here are the fields declared inside the code, beans referenced
      // in the oblivion.config file wont appear here
      Field[] fields = appInstance.getClass().getDeclaredFields();

      Reflections generalScan = new Reflections(rootPackage);
      Set<Class<?>> scannedClasses = generalScan.getTypesAnnotatedWith(OblivionService.class);
      Set<Class<?>> postProcessorClasses =
          scannedClasses.stream()
              .filter(c -> OblivionBeanPostProcessor.class.isAssignableFrom(c))
              .collect(Collectors.toSet());

      OblivionAopProxyCreator aopProxyCreator = new OblivionAopProxyCreator();
      beansContainer.postProcessorBeans.add(aopProxyCreator);

      for (Class<?> postProcessorClass : postProcessorClasses) {
        beansContainer.resolveDependency(
            postProcessorClass,
            postProcessorClass.getName(),
            threadPoolExecutor,
            scannedClasses,
            null,
            true);
      }

      for (Field f : fields) {
        if (f.isAnnotationPresent(OblivionWire.class)) {
          String beanType = getBeanType(f.getType(), beansContainer);
          Object beanObject = null;
          if ("singleton".equals(beanType)) {
            beanObject =
                beansContainer.resolveDependency(
                    f.getType(), f.getName(), threadPoolExecutor, scannedClasses, null, false);
          } else if ("prototype".equals(beanType)) {
            beanObject =
                beansContainer.resolveDependency(
                    f.getType(), f.getName(), threadPoolExecutor, scannedClasses, null, false);
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
        String beanType = getBeanType(beanClass, beansContainer);

        if ("singleton".equals(beanType)) {
          beansContainer.resolveDependency(
              beanClass, beanName, threadPoolExecutor, scannedClasses, null, false);
          beansContainer.getSingletonBean(beanName);
        } else if ("prototype".equals(beanType)) {
          // NOTE: since im using a simple file, theres no way to use annotations, and other stuff
          // so later i intend to change to another file format to support all of these features.
          beansContainer.resolveDependency(
              beanClass, beanName, threadPoolExecutor, scannedClasses, null, false);
        }
      }
    } catch (Exception ex) {
      System.out.println("failed inside Inject.java");
      ex.printStackTrace();
      System.out.println("error: " + ex.getMessage());
    }
  }

  private static String getBeanType(Class<?> clazz, BeansContainer beansContainer) {
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

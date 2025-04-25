package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionAfter;
import com.br.oblivion.annotations.OblivionAfterReturning;
import com.br.oblivion.annotations.OblivionAfterThrowing;
import com.br.oblivion.annotations.OblivionAspect;
import com.br.oblivion.annotations.OblivionBefore;
import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.annotations.OblivionWire;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.container.Pair;
import com.br.oblivion.interfaces.OblivionBeanPostProcessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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

      Set<Class<?>> adviceClasses =
          scannedClasses.stream()
              .filter(c -> c.isAnnotationPresent(OblivionAspect.class))
              .collect(Collectors.toSet());

      String targetMethod = null;

      for (Class<?> ac : adviceClasses) {
        Method[] methods = ac.getDeclaredMethods();
        for (Method m : methods) {
          if (m.isAnnotationPresent(OblivionBefore.class)) {
            targetMethod = m.getAnnotation(OblivionBefore.class).target();

            if (BeansContainer.beforeAdviceMap.containsKey(targetMethod)) {
              List<Pair<Method, Object>> existingMethods =
                  BeansContainer.beforeAdviceMap.get(targetMethod);
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, scannedClasses, null, false);
              Pair<Method, Object> pair = new Pair<Method, Object>(m, aspectInstance);
              existingMethods.add(pair);
            } else {
              List<Pair<Method, Object>> pairList = new ArrayList<>();
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, scannedClasses, null, false);
              Pair<Method, Object> pairToAdd = new Pair<Method, Object>(m, aspectInstance);
              pairList.add(pairToAdd);
              BeansContainer.beforeAdviceMap.put(targetMethod, pairList);
            }
          }

          if (m.isAnnotationPresent(OblivionAfter.class)) {
            targetMethod = m.getAnnotation(OblivionAfter.class).target();

            if (BeansContainer.afterAdviceMap.containsKey(targetMethod)) {
              List<Pair<Method, Object>> existingMethods =
                  BeansContainer.afterAdviceMap.get(targetMethod);
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, scannedClasses, null, false);
              Pair<Method, Object> pair = new Pair<Method, Object>(m, aspectInstance);
              existingMethods.add(pair);
            } else {
              List<Pair<Method, Object>> pairList = new ArrayList<>();
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, scannedClasses, null, false);
              Pair<Method, Object> pairToAdd = new Pair<Method, Object>(m, aspectInstance);
              pairList.add(pairToAdd);
              BeansContainer.afterAdviceMap.put(targetMethod, pairList);
            }
          }

          if (m.isAnnotationPresent(OblivionAfterThrowing.class)) {
            targetMethod = m.getAnnotation(OblivionAfterThrowing.class).target();

            if (BeansContainer.afterThrowingAdviceMap.containsKey(targetMethod)) {
              List<Pair<Method, Object>> existingMethods =
                  BeansContainer.afterThrowingAdviceMap.get(targetMethod);
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, scannedClasses, null, false);
              Pair<Method, Object> pair = new Pair<Method, Object>(m, aspectInstance);
              existingMethods.add(pair);
            } else {
              List<Pair<Method, Object>> pairList = new ArrayList<>();
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, scannedClasses, null, false);
              Pair<Method, Object> pairToAdd = new Pair<Method, Object>(m, aspectInstance);
              pairList.add(pairToAdd);
              BeansContainer.afterThrowingAdviceMap.put(targetMethod, pairList);
            }
          }

          if (m.isAnnotationPresent(OblivionAfterReturning.class)) {
            targetMethod = m.getAnnotation(OblivionAfterReturning.class).target();

            if (BeansContainer.afterReturningAdviceMap.containsKey(targetMethod)) {
              List<Pair<Method, Object>> existingMethods =
                  BeansContainer.afterReturningAdviceMap.get(targetMethod);
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, scannedClasses, null, false);
              Pair<Method, Object> pair = new Pair<Method, Object>(m, aspectInstance);
              existingMethods.add(pair);
            } else {
              List<Pair<Method, Object>> pairList = new ArrayList<>();
              Object aspectInstance =
                  beansContainer.resolveDependency(
                      ac, ac.getName(), threadPoolExecutor, adviceClasses, null, false);
              Pair<Method, Object> pairToAdd = new Pair<Method, Object>(m, aspectInstance);
              pairList.add(pairToAdd);
              BeansContainer.afterReturningAdviceMap.put(targetMethod, pairList);
            }
          }
        }
      }

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

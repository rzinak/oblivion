package com.br.autowired.container;

import com.br.autowired.exception.OblivionException;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeansContainer {
  private final Map<String, Object> singletonBeans = new ConcurrentHashMap<>();
  private Map<String, PrototypeBeanMetadata> prototypeBeans = new ConcurrentHashMap<>();

  // lifecylce methods
  private List<Pair<Object, Method>> preDestroyMethods = new ArrayList<>();
  private List<Pair<Object, Method>> preShutdownMethods = new ArrayList<>();
  private List<Pair<Object, Method>> postShutdownMethods = new ArrayList<>();

  public <T> void registerSingletonBean(String identifier, T bean) {
    singletonBeans.put(identifier, bean);
  }

  public <T> void registerPrototypeBean(
      String identifier, PrototypeBeanMetadata prototypeBeanMetadata) {
    prototypeBeans.put(identifier, prototypeBeanMetadata);
  }

  public Object getSingletonBean(String identifier) {
    Object singletonBean = singletonBeans.get(identifier);
    if (singletonBean != null) {
      return singletonBean;
    }
    return null;
  }

  public Object getPrototypeBean(String identifier, BeansContainer container) throws Exception {
    try {
      Class<?> prototypeBeanClass = prototypeBeans.get(identifier).getPrototypeClass();
      Class<?>[] requiredParams = prototypeBeans.get(identifier).getRequiredParams();
      Object[] requiredObjects = prototypeBeans.get(identifier).getRequiredObjects();

      if (prototypeBeanClass != null) {

        Constructor<?>[] ctors = prototypeBeanClass.getDeclaredConstructors();

        for (Constructor<?> ctor : ctors) {
          if (ctor.getParameterCount() == 0) {
            Object initPrototypeBean = prototypeBeanClass.newInstance();
            ReflectionUtils.runPostConstructMethods(prototypeBeanClass, initPrototypeBean);
            ReflectionUtils.initializeFields(initPrototypeBean);
            ReflectionUtils.runPostInitializationMethods(prototypeBeanClass, initPrototypeBean);
            ReflectionUtils.registerPersistentBeanLifecycles(
                prototypeBeanClass, initPrototypeBean, container);
            return initPrototypeBean;
          } else {
            Object initPrototypeBean =
                prototypeBeanClass
                    .getDeclaredConstructor(requiredParams)
                    .newInstance(requiredObjects);
            ReflectionUtils.runPostConstructMethods(prototypeBeanClass, initPrototypeBean);
            ReflectionUtils.initializeFields(initPrototypeBean);
            ReflectionUtils.runPostInitializationMethods(prototypeBeanClass, initPrototypeBean);
            ReflectionUtils.registerPersistentBeanLifecycles(
                prototypeBeanClass, initPrototypeBean, container);
            return initPrototypeBean;
          }
        }
      }

      return null;
    } catch (NullPointerException
        | NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException ex) {
      throw new OblivionException("Error creating prototype bean: " + identifier, ex);
    }
  }

  public void registerPreDestroyMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preDestroyMethod = new Pair<Object, Method>(instantiatedClass, method);
    preDestroyMethods.add(preDestroyMethod);
  }

  public void registerPreShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> preShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    preShutdownMethods.add(preShutdownMethod);
  }

  public void registerPostShutdownMethods(Object instantiatedClass, Method method) {
    if (instantiatedClass == null || method == null) {
      throw new IllegalArgumentException("Instantiated class or method cannot be null");
    }

    Pair<Object, Method> postShutdownMethod = new Pair<Object, Method>(instantiatedClass, method);
    postShutdownMethods.add(postShutdownMethod);
  }

  public Map<?, Object> getAllSingletonBeans() {
    if (singletonBeans.isEmpty()) {
      return null;
    }
    return singletonBeans;
  }

  public List<Pair<Object, Method>> getPreDestroyMethods() {
    return this.preDestroyMethods;
  }

  public List<Pair<Object, Method>> getPreShutdownMethods() {
    return this.preShutdownMethods;
  }

  public List<Pair<Object, Method>> getPostShutdownMethods() {
    return this.postShutdownMethods;
  }

  public void clearPreDestroyMap() {
    this.preDestroyMethods.clear();
  }

  public void clearPreShutdownMap() {
    this.preShutdownMethods.clear();
  }

  public void clearPostShutdownMap() {
    this.postShutdownMethods.clear();
  }

  public void clearSingletonBeansMap() {
    this.singletonBeans.clear();
  }

  public void clearPrototypeBeansMap() {
    this.prototypeBeans.clear();
  }
}

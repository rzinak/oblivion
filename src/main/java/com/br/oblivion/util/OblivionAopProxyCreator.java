package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.interfaces.OblivionBeanPostProcessor;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class OblivionAopProxyCreator implements OblivionBeanPostProcessor {

  // NOTE: using override only in the 'after initialization' method to allow the
  // bean's own initialization methods (PostConstruct, PreInitialization, etc)
  // to run on the actual instance first, without any proxy interference
  @Override
  public Object postProcessorBeforeInitialization(Object bean, String beanName) {
    return bean;
  }

  // NOTE: and here, it starts acting after the bean has been instantiated, dependencies
  // got injected (in the constructor), and its own logic has run. and then i check
  // if it needs AOP behaviorand wrap it in a proxy before it's fully "ready" by the container
  @Override
  public Object postProcessorAfterInitialization(Object bean, String beanName) {
    if (bean.getClass().isAnnotationPresent(OblivionLoggable.class)) {
      OblivionSimpleInvocationHandler handler = new OblivionSimpleInvocationHandler(bean);
      Class<?>[] beanInterfaces = bean.getClass().getInterfaces();
      System.out.println("[PROXY] interfaces -> " + Arrays.toString(beanInterfaces));
      ClassLoader classLoader = bean.getClass().getClassLoader();
      Object proxyInstance = Proxy.newProxyInstance(classLoader, beanInterfaces, handler);
      System.out.println("[PROXY] Created proxy instance -> " + proxyInstance.getClass());
      return proxyInstance;
    }

    return bean;
  }
}

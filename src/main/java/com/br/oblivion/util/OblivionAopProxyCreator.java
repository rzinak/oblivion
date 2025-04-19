package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionAspect;
import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.interfaces.OblivionBeanPostProcessor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

public class OblivionAopProxyCreator implements OblivionBeanPostProcessor {

  // NOTE: using override only in the 'after initialization' method to allow the
  // bean's own initialization methods (PostConstruct, PreInitialization, etc)
  // to run on the actual instance first, without any proxy interference
  @Override
  public Object postProcessorBeforeInitialization(Object bean, String beanName) {
    return bean;
  }

  @Override
  public Object postProcessorAfterInitialization(Object bean, String beanName) {
    if (bean.getClass().isAnnotationPresent(OblivionAspect.class)) {
      Method[] beanMethods = bean.getClass().getDeclaredMethods();

      boolean isClassLoggable = false;
      boolean isMethodLoggable = false;

      if (bean.getClass().isAnnotationPresent(OblivionLoggable.class)) isClassLoggable = true;

      for (Method m : beanMethods) {
        if (m.isAnnotationPresent(OblivionLoggable.class)) {
          isMethodLoggable = true;
          break;
        }
      }

      if (isClassLoggable || isMethodLoggable) {
        Class<?>[] beanInterfaces = bean.getClass().getInterfaces();

        // CGLIB proxy for beans with no suitable interfaces
        if (beanInterfaces.length == 0 || beanInterfaces == null) {
          Enhancer enhancer = new Enhancer();
          enhancer.setSuperclass(bean.getClass());
          enhancer.setCallbackType(OblivionCglibInterceptor.class);
          Class<?> proxyClass = enhancer.createClass();
          Objenesis objenesis = new ObjenesisStd();
          ObjectInstantiator<?> instantiator = objenesis.getInstantiatorOf(proxyClass);
          Object proxyInstance = instantiator.newInstance();
          ((Factory) proxyInstance)
              .setCallback(0, new OblivionCglibInterceptor(bean, isClassLoggable));
          return proxyInstance;
        } else {
          // jdk dynamic proxy for beans with suitable interfaces
          OblivionInvocationHandler handler = new OblivionInvocationHandler(bean, isClassLoggable);
          ClassLoader classLoader = bean.getClass().getClassLoader();
          Object proxyInstance = Proxy.newProxyInstance(classLoader, beanInterfaces, handler);
          return proxyInstance;
        }
      }
    }

    return bean;
  }
}

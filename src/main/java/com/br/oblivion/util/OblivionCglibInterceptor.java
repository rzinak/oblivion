package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionLoggable;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class OblivionCglibInterceptor implements MethodInterceptor {
  private final Object originalTarget;
  private boolean isClassLoggable;

  public OblivionCglibInterceptor(Object originalTarget, boolean isClassLoggable) {
    this.originalTarget = originalTarget;
    this.isClassLoggable = isClassLoggable;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
      throws Throwable {
    try {
      if (method.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[GCLIB PROXY] intercepting method -> " + method.getName());
      }

      Object result = method.invoke(this.originalTarget, args);

      if (method.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[GCLIB PROXY] intercepting method -> " + method.getName());
        System.out.println("[CGLIB PROXY] finished method -> " + method.getName());
      }

      return result;
    } catch (Throwable t) {
      System.out.println("[CGLIB PROXY] exception in method -> " + method.getName());
      throw t;
    }
  }
}

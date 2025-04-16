package com.br.oblivion.util;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class OblivionCglibInterceptor implements MethodInterceptor {
  private final Object originalTarget;

  public OblivionCglibInterceptor(Object originalTarget) {
    this.originalTarget = originalTarget;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
      throws Throwable {
    System.out.println("[GCLIB PROXY] intercepting method -> " + method.getName());
    try {
      Object result = method.invoke(this.originalTarget, args);
      System.out.println("[CGLIB PROXY] finished method -> " + method.getName());
      return result;
    } catch (Throwable t) {
      System.out.println("[CGLIB PROXY] exception in method -> " + method.getName());
      throw t;
    }
  }
}

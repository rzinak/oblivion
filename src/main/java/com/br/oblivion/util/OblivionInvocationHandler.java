package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionLoggable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OblivionInvocationHandler implements InvocationHandler {
  private final Object originalTarget;
  private boolean isClassLoggable;

  public OblivionInvocationHandler(Object originalTarget, boolean isClassLoggable) {
    this.originalTarget = originalTarget;
    this.isClassLoggable = isClassLoggable;
  }

  @Override
  public Object invoke(Object obj, Method method, Object[] args)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    try {
      Method originalMethod =
          this.originalTarget.getClass().getMethod(method.getName(), method.getParameterTypes());

      if (originalMethod.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[PROXY] intercepting method -> " + method.getName());
      }

      Object result = method.invoke(this.originalTarget, args);

      if (originalMethod.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[PROXY] finished method -> " + method.getName());
      }

      return result;
    } catch (Throwable t) {
      System.out.println("[PROXY] exception in method -> " + method.getName());
      throw t;
    }
  }
}

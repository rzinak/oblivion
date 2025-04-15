package com.br.oblivion.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OblivionSimpleInvocationHandler implements InvocationHandler {
  private final Object originalTarget;

  public OblivionSimpleInvocationHandler(Object originalTarget) {
    this.originalTarget = originalTarget;
  }

  @Override
  public Object invoke(Object obj, Method method, Object[] args)
      throws IllegalAccessException, InvocationTargetException {
    System.out.println("[PROXY] intercepting method -> " + method.getName());
    Object result = method.invoke(this.originalTarget, args);
    System.out.println("[PROXY] finished method -> " + method.getName());
    return result;
  }
}

package com.br.oblivion.util;

import com.br.oblivion.interfaces.OblivionJoinPoint;
import java.lang.reflect.Method;
import java.util.Arrays;

// TODO: maybe rename this file later, also move to another package
// NOTE: this class is for holding the metadata of a method im
// intercepting either via JDK proxy or CGLIB

public class MethodExecutionJoinPoint implements OblivionJoinPoint {
  private final Object target;
  private final Object proxy;
  private final Method method;
  private final Object[] args;

  public MethodExecutionJoinPoint(Object target, Object proxy, Method method, Object[] args) {
    this.target = target;
    this.proxy = proxy;
    this.method = method;
    // idk about this, since i dont think i will override it
    // this.args = (args != null) ? args : new Object[0];
    this.args = args;
  }

  @Override
  public Object[] getArgs() {
    // maybe return a copy here?
    return this.args;
  }

  @Override
  public Object getTarget() {
    return this.target;
  }

  @Override
  public Object getProxy() {
    return this.proxy;
  }

  @Override
  public Method getMethod() {
    return this.method;
  }

  @Override
  public String toString() {
    return "JoinPoint(Method="
        + method.getName()
        + " on Target="
        + target.getClass().getSimpleName()
        + " with Args="
        + Arrays.toString(args)
        + ")";
  }
}

package com.br.oblivion.interfaces;

import java.lang.reflect.Method;

// TODO: add javadoc here later
// TODO: maybe move this to an 'AOP' package
/**
 * Represents the runtime join point (e.g., a method execution) being intercepted by AOP advice.
 * Provides reflective access to the state available at that join point.
 */
public interface OblivionJoinPoint {
  /**
   * Get the arguments passed to the intercepted method.
   *
   * @return An array of arguments. Returns an empty array if the method takes no arguments.
   */
  Object[] getArgs();

  /**
   * Get the object instance the intercepted method is being invoked on. This is the original target
   * bean instance, not the proxy.
   *
   * @return The target object.
   */
  Object getTarget();

  /**
   * Get the proxy object that the intercepted method was called on.
   *
   * @return The AOP proxy instance.
   */
  Object getProxy();

  /**
   * Get the reflective Method object for the intercepted method.
   *
   * @return The Method being executed.
   */
  Method getMethod();
}

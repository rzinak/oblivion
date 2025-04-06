package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed before the bean is destroyed.
 *
 * <p>This annotation supports the following optional properties:
 *
 * <ul>
 *   <li>{@code order} – controls the execution order of methods
 *   <li>{@code cond} – executes the method only if the condition matches
 * </ul>
 *
 * <p>Executes right before {@code @OblivionPreShutdown}.
 *
 * @see com.br.oblivion.annotations.OblivionPreShutdown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OblivionPreDestroy {
  /**
   * Optional execution order.
   *
   * <p>Defaults to {@code 0}. If multiple methods are annotated, the ones with lower order values
   * run first. If no {@code order} is set, methods execute in class-declaration order.
   */
  public int order() default 0;

  /**
   * Optional execution condition.
   *
   * <p>Conditions (for now) are set in the {@code oblivion.properties} file like this: {@code
   * KEY=VAL} or {@code ENV=PROD}, for example.
   *
   * <p>In the 'cond' field we would use it like this: {@code @OblivionPreDestroy(cond =
   * "ENV.PROD")}.
   *
   * <p>Defaults to {@code ""}. Before the method execution, this condition will be evaluated, and
   * the method will run only if it's true.
   */
  public String cond() default "";
}

package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be executed after a full bean initialization.
 *
 * <p>Useful for setups that require the whole DI context to be ready — perfect for spinning up
 * background threads, schedulers, etc.
 *
 * <p>This annotation supports the following optional properties:
 *
 * <ul>
 *   <li>{@code order} – controls the execution order of methods
 *   <li>{@code cond} – executes the method only if the condition matches
 *   <li>{@code async} – runs the method asynchronously
 * </ul>
 *
 * <p>Executes right after {@code @OblivionPostConstruct}.
 *
 * @see com.br.oblivion.annotations.OblivionPostConstruct
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OblivionPostInitialization {
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
   * <p>In the 'cond' field we would use it like this: {@code @OblivionPostInitialization(cond =
   * "ENV.PROD")}.
   *
   * <p>Defaults to {@code ""}. Before the method execution, this condition will be evaluated, and
   * the method will run only if it's true.
   */
  public String cond() default "";

  /**
   * Optional async method.
   *
   * <p>Defaults to {@code false}. If a method has this field with a {@code true} value, it will be
   * run asynchronously.
   *
   * <p>Usage: {@code @OblivionPostInitialization(async = true)}.
   *
   * <p>If you don't want, there's no need to use {@code async = false}, you can just omit the
   * 'async' property.
   */
  public boolean async() default false;
}

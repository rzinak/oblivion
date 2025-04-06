package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a top-level class injected.
 *
 * <p>This is the injection starting point, the dependency injection will happen inwards from here,
 * scanning the dependencies recursively until they are all in order, so this class can be executed.
 *
 * <p>This annotation supports the following optional property:
 *
 * <ul>
 *   <li>{@code constructorToInject} – the constructor identifier to inject if multiple constructors
 *       are found, this name must match the name given using the
 *       {@code @OblivionConstructorInject(name = "...")}
 * </ul>
 *
 * @see com.br.oblivion.annotations.OblivionConstructorInject
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OblivionWire {
  /**
   * Optional constructor to inject.
   *
   * <p>You can be specific and explicitly indicates a constructor to be injected if there's more
   * than one. The name given here must match the name you set in the constructor's class using the
   * {@code @OblivionConstructorInject(name = "...")}
   */
  public String constructorToInject() default "";
}

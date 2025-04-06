package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Explicitly marks a constructor to be injected.
 *
 * <p>This annotation goes above the constructor itself, and can be used to match the constructor
 * identifier in the {@code @OblivionWire}.
 *
 * <p>Usage example: {@code @OblivionConstructorInject(name = "NAME")} ...
 * {@code @OblivionWire(constructorToInject = "NAME")}
 *
 * <p>This annotation supports the following optional property:
 *
 * <ul>
 *   <li>{@code name} – Constructor identifier to be used in {@code @OblivionConstructorInject(name
 *       = "...")}. The same name you use here should also be used in
 *       {@code @OblivionWire(constructorToInject = "...")}, to refer to a constructor.
 * </ul>
 *
 * @see com.br.oblivion.annotations.OblivionWire
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface OblivionConstructorInject {
  /**
   * Optional name for the constructor. If left empty the DI container may use the first constructor
   * it finds inside a class.
   *
   * @return the constructor identifier
   */
  public String name() default "";
}

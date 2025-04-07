package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to explicitly mark a class to be injected when using an interface that is also implemented
 * by another class
 *
 * <p>This annotation goes in the parameters of a constructor where you have the INTERFACE, and the
 * name property you set here, must match the name property when using the
 * {@code @OblivionService(name = "...")} in the class that implements a given interface
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * public DefaultProductService(@OblivionQualifier(name = "DBREPO") ProductRepository repository) {
 *   this.repository = repository;
 * }
 *
 * @OblivionService(name = "DBREPO")
 * public class DatabaseProductRepository implements ProductRepository { }
 *
 * }</pre>
 *
 * <p>This annotation supports the following optional properties:
 *
 * <ul>
 *   <li>{@code name} – implementation identifier
 * </ul>
 *
 * @see com.br.oblivion.annotations.OblivionPrimary
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface OblivionQualifier {
  /**
   * Implementation identifier.
   *
   * <p>This name is passed in the arguments of a constructor, and must match the name passed in
   * {@code @OblivionService}.
   *
   * @return the implementation identifier
   */
  public String name() default "";
}

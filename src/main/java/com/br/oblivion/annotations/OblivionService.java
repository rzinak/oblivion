package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a service to be managed and injected by the Oblivion dependency injection
 * container.
 *
 * <p>Classes annotated with {@code @OblivionService} are automatically detected and instantiated
 * during the application's startup phase.
 *
 * <p>This annotation also supports an optional {@code name} parameter. If a name is provided, it
 * will be used by the DI container to identify the service. If not, the container may default to
 * using the class name.
 *
 * <p>The {@code name} is especially useful when working with interfaces that have multiple
 * implementations — use it in combination with {@code @OblivionQualifier(name = "...")} to select
 * the desired one during injection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OblivionService {
  /**
   * Optional name for the service. If left empty the DI container may use the class name.
   *
   * <p>This name is also used when specifying which implementation of an interface to use, so this
   * name must match the name passed in {@code @OblivionQualifier(name = "")}.
   *
   * @return the service name
   */
  public String name() default "";
}

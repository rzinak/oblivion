package com.br.autowired.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// NOTE: methods annotated with @OblivionPreInitialization should be static
// because they are called before the bean is fully initialized, so the class
// instantiation is not likely to be complete
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OblivionPreInitialization {
  public int order() default 0;

  public String cond() default "";
}

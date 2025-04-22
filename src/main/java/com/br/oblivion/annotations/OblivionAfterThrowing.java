// NOTE: PART OF AOP
package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// runs only if method throws
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface OblivionAfterThrowing {
  public String target() default "";
}

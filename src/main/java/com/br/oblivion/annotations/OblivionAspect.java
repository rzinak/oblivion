// NOTE: PART OF AOP
package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// marks a class containing advice
// by doing this i believe i 'force' the user
// to use this at the top of a class where a
// advice would go, it simplifies detection
// since i need to look for classes with this annotation
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface OblivionAspect {
  public String target() default "";
}

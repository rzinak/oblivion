// NOTE: PART OF AOP
package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// NOTE:  i believe i could rename this to OblivionAround, but
// i would also to implement the proceed() function for that, i
// already have a very simple way to actually run the methods,
// but i believe that with proceed it would be simpler to run
// or skip methods...
// it already acts like an around advice, only the proceed is pending
//
// used in proxy, intercept methods and just log stuff for now
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface OblivionLoggable {
  public String name() default "";
}

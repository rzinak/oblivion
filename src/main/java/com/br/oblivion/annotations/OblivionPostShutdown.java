package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OblivionPostShutdown {
  public int order() default 0;

  public String cond() default "";

  public boolean async() default false;
}

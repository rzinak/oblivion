package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bean to be instantiated as a prototype bean. By default in Oblivion, all beans are
 * singletons.
 *
 * <p>Despite the instantiation being different, all annotation work the same, so no tweak is
 * required
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OblivionPrototype {}

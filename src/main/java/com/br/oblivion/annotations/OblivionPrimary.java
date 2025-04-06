package com.br.oblivion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a class to be injected when using an interface that is also implemented by another
 * class
 *
 * <p>This annotation marks a default implementation to be injected, the opposite of
 * {@code @OblivionQualifier}, where you explicitly indicate an implementation to be injected.
 *
 * @see com.br.oblivion.annotations.OblivionQualifier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OblivionPrimary {}

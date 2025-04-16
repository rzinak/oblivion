package com.br.oblivion.interfaces;

// NOTE: OblivionBeanPostProcessor allows custom code to hook into and extend the bean
//  creation/initialization lifecycle.
//  To use it we simply implement this interface and override both {@code Object
//  postProcessorBeforeInitialization(Object bean, String beanName);} and {@code Object
//  postProcessorAfterInitialization(Object bean, String beanName);}
//  These methods are discovered upon Oblivion initialization, and are processed before the 'core'
// Oblivion lifecycle.
public interface OblivionBeanPostProcessor {
  Object postProcessorBeforeInitialization(Object bean, String beanName);

  Object postProcessorAfterInitialization(Object bean, String beanName);
}

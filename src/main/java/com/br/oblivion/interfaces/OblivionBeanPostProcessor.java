package com.br.oblivion.interfaces;

public interface OblivionBeanPostProcessor {
  Object postProcessorBeforeInitialization(Object bean, String beanName);

  Object postProcessorAfterInitialization(Object bean, String beanName);
}

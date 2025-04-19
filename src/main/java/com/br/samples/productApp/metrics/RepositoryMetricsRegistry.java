package com.br.samples.productApp.metrics;

import com.br.oblivion.annotations.OblivionService;
import com.br.oblivion.interfaces.OblivionBeanPostProcessor;
import com.br.samples.productApp.repository.TrackableRepository;

@OblivionService
public class RepositoryMetricsRegistry implements OblivionBeanPostProcessor {

  private final RepositoryMetricsService metricsService;

  public RepositoryMetricsRegistry(RepositoryMetricsService metricsService) {
    // System.out.println("[METRICS REGISTRY] -> BeanPostProcessor created");
    this.metricsService = metricsService;
  }

  @Override
  public Object postProcessorBeforeInitialization(Object bean, String beanName) {
    // for now aint doing nothing before initialization
    // System.out.println("[METRICS REGISTRY] Before init -> " + beanName + ", bean -> " + bean);
    return bean; // always returning the bean
  }

  @Override
  public Object postProcessorAfterInitialization(Object bean, String beanName) {
    // System.out.println(
    //     "[METRICS REGISTRY] After init -> " + beanName + " Type -> " +
    // bean.getClass().getName());
    // gotta check if the bean instance implements the marker interface
    // System.out.println(
    //     "POST PROCESSOR AFTER INITIALIZATION BEAN-> " + bean + " BEAN NAME -> " + beanName);
    if (bean instanceof TrackableRepository) {
      // System.out.println("[METRICS REGISTRY] Detected TrackableRepository -> " + beanName);
      // then we register in the metrics
      this.metricsService.registerRepository(beanName);
    }
    return bean;
  }
}

package com.br.samples.productApp.repository;

// its a marker interface for repositories who usage should be tracked
// BeanPostProcessor can look for this interface
public interface TrackableRepository {}
// no methods because its acting just as a label

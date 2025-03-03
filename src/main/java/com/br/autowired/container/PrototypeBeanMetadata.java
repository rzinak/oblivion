package com.br.autowired.container;

public class PrototypeBeanMetadata {
  private Class<?> prototypeClass;
  private Class<?>[] requiredParams;
  private Object[] requiredObjects;

  public PrototypeBeanMetadata() {}

  public PrototypeBeanMetadata(
      Class<?> prototypeClass, Class<?>[] requiredParams, Object[] requiredObjects) {
    this.prototypeClass = prototypeClass;
    this.requiredParams = requiredParams;
    this.requiredObjects = requiredObjects;
  }

  public Class<?> getPrototypeClass() {
    return prototypeClass;
  }

  public void setPrototypeClass(Class<?> prototypeClass) {
    this.prototypeClass = prototypeClass;
  }

  public Class<?>[] getRequiredParams() {
    return requiredParams;
  }

  public void setRequiredParams(Class<?>[] requiredParams) {
    this.requiredParams = requiredParams;
  }

  public Object[] getRequiredObjects() {
    return requiredObjects;
  }

  public void setRequiredObjects(Object[] requiredObjects) {
    this.requiredObjects = requiredObjects;
  }
}

package com.br.autowired.lifecycle;

import com.br.autowired.container.BeansContainer;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.Map;

public class Shutdown {
  public void attachShutdown(BeansContainer container) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                for (Map.Entry<Object, Method> entry : container.getPreDestroyMethods()) {
                  try {
                    ReflectionUtils.runPreDestroyMethod(entry.getKey(), entry.getValue());
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
                }

                for (Map.Entry<Object, Method> entry : container.getPreShutdownMethods()) {
                  try {
                    ReflectionUtils.runPreShutdownMethod(entry.getKey(), entry.getValue());
                  } catch (Exception ex) {
                    ex.printStackTrace();
                  }
                }

                container.clearPreDestroyMap();
                container.clearSingletonBeansMap();
                container.clearoPrototypeBeansMap();
              }
            });
  }
}

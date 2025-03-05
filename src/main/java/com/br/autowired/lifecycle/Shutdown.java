package com.br.autowired.lifecycle;

import com.br.autowired.annotations.OblivionPostShutdown;
import com.br.autowired.annotations.OblivionPreDestroy;
import com.br.autowired.annotations.OblivionPreShutdown;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.container.Pair;
import com.br.autowired.exception.OblivionException;
import com.br.autowired.util.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.Collections;

public class Shutdown {
  public void attachShutdown(BeansContainer container) {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {

                Collections.sort(
                    container.getPreDestroyMethods(),
                    (a, b) ->
                        Integer.compare(
                            a.getR().getAnnotation(OblivionPreDestroy.class).order(),
                            b.getR().getAnnotation(OblivionPreDestroy.class).order()));

                Collections.sort(
                    container.getPreShutdownMethods(),
                    (a, b) ->
                        Integer.compare(
                            a.getR().getAnnotation(OblivionPreShutdown.class).order(),
                            b.getR().getAnnotation(OblivionPreShutdown.class).order()));

                Collections.sort(
                    container.getPostShutdownMethods(),
                    (a, b) ->
                        Integer.compare(
                            a.getR().getAnnotation(OblivionPostShutdown.class).order(),
                            b.getR().getAnnotation(OblivionPostShutdown.class).order()));

                for (Pair<Object, Method> entry : container.getPreDestroyMethods()) {
                  try {
                    ReflectionUtils.runPreDestroyMethod(entry.getL(), entry.getR());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPreDestroy method '%s' in class"
                                + " '%s': %s",
                            entry.getR(), entry.getL(), ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                for (Pair<Object, Method> entry : container.getPreShutdownMethods()) {
                  try {
                    ReflectionUtils.runPreShutdownMethod(entry.getL(), entry.getR());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPreShutdown method '%s' in class"
                                + " '%s': %s",
                            entry.getR(), entry.getL(), ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                container.clearPreDestroyMap();
                container.clearSingletonBeansMap();
                container.clearPrototypeBeansMap();

                for (Pair<Object, Method> entry : container.getPostShutdownMethods()) {
                  try {
                    ReflectionUtils.runPostShutdownMethod(entry.getL(), entry.getR());
                  } catch (OblivionException ex) {
                    String errorMessage =
                        String.format(
                            "Error during execution of @OblivionPostShutdown method '%s' in class"
                                + " '%s': %s",
                            entry.getR(), entry.getL(), ex.getMessage());
                    System.err.println(errorMessage);
                  }
                }

                container.clearPostShutdownMap();
              }
            });
  }
}

package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.container.Pair;
import com.br.oblivion.interfaces.TargetAware;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class OblivionInvocationHandler implements InvocationHandler, TargetAware {
  private final Object originalTarget;
  private boolean isClassLoggable;

  public OblivionInvocationHandler(Object originalTarget, boolean isClassLoggable) {
    this.originalTarget = originalTarget;
    this.isClassLoggable = isClassLoggable;
  }

  @Override
  public Object invoke(Object obj, Method method, Object[] args)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    try {
      // if it starts throwing that illegal argument exception saying the object is wrong when
      // calling the method, despite the object being correct, its most likely because the
      // getOblivionTargetInstance is also being intercepted...
      // i was having this problem with CGLIB but never had it with JDK Proxies, but i'm leaving
      // this comment here in case i also have it here, so i dont have to waster 2 more hours trying
      // to figure it out
      // if (method.getName().equals("getOblivionTargetInstance") && method.getParameterCount() ==
      // 0) {
      //         return getOblivionTargetInstance();
      //       }

      String currentMethod = method.getDeclaringClass().getName() + "." + method.getName();

      if (BeansContainer.beforeAdviceMap.containsKey(currentMethod)) {
        List<Pair<Method, Object>> methodPair = BeansContainer.beforeAdviceMap.get(currentMethod);
        for (Pair<Method, Object> pair : methodPair) {
          Method adviceMethod = pair.getL();
          Object aspectInstance = pair.getR();
          Object targetToInvoke = null;

          if (aspectInstance instanceof TargetAware) {
            targetToInvoke = ((TargetAware) aspectInstance).getOblivionTargetInstance();
          } else {
            targetToInvoke = aspectInstance;
          }

          if (adviceMethod.getParameterCount() == 0) {
            adviceMethod.invoke(targetToInvoke);
          } else {
            adviceMethod.invoke(targetToInvoke, args);
          }
        }
      }

      Method originalMethod =
          this.originalTarget.getClass().getMethod(method.getName(), method.getParameterTypes());

      if (originalMethod.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[PROXY] intercepting method -> " + method.getName());
      }

      Object result = method.invoke(this.originalTarget, args);

      if (originalMethod.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[PROXY] finished method -> " + method.getName());
      }

      if (BeansContainer.afterReturningAdviceMap.containsKey(currentMethod)) {
        List<Pair<Method, Object>> methodsToCall =
            BeansContainer.afterReturningAdviceMap.get(currentMethod);
        for (Pair<Method, Object> pair : methodsToCall) {
          Method adviceMethod = pair.getL();
          Object aspectInstance = pair.getR();
          Object targetToInvoke = null;

          if (aspectInstance instanceof TargetAware) {
            targetToInvoke = ((TargetAware) aspectInstance).getOblivionTargetInstance();
          } else {
            targetToInvoke = aspectInstance;
          }

          if (adviceMethod.getParameterCount() == 0) {
            adviceMethod.invoke(targetToInvoke);
          } else {
            adviceMethod.invoke(targetToInvoke, args);
          }
        }
      }

      return result;
    } catch (Throwable t) {
      String currentMethod = method.getDeclaringClass().getName() + "." + method.getName();
      if (BeansContainer.afterThrowingAdviceMap.containsKey(currentMethod)) {
        List<Pair<Method, Object>> methodsToCall =
            BeansContainer.afterThrowingAdviceMap.get(currentMethod);
        for (Pair<Method, Object> pair : methodsToCall) {
          Method adviceMethod = pair.getL();
          Object aspectInstance = pair.getR();
          Object targetToInvoke = null;

          if (aspectInstance instanceof TargetAware) {
            targetToInvoke = ((TargetAware) aspectInstance).getOblivionTargetInstance();
          } else {
            targetToInvoke = aspectInstance;
          }

          if (adviceMethod.getParameterCount() == 0) {
            adviceMethod.invoke(targetToInvoke);
          } else {
            adviceMethod.invoke(targetToInvoke, args);
          }
        }
      }
      throw t;
    } finally {
      String currentMethod = method.getDeclaringClass().getName() + "." + method.getName();
      if (BeansContainer.afterAdviceMap.containsKey(currentMethod)) {
        List<Pair<Method, Object>> methodsToCall = BeansContainer.afterAdviceMap.get(currentMethod);

        for (Pair<Method, Object> pair : methodsToCall) {
          Method adviceMethod = pair.getL();
          Object aspectInstance = pair.getR();
          Object targetToInvoke = null;

          if (aspectInstance instanceof TargetAware) {
            targetToInvoke = ((TargetAware) aspectInstance).getOblivionTargetInstance();
          } else {
            targetToInvoke = aspectInstance;
          }

          if (adviceMethod.getParameterCount() == 0) {
            adviceMethod.invoke(targetToInvoke);
          } else {
            adviceMethod.invoke(targetToInvoke, args);
          }
        }
      }
    }
  }

  @Override
  public Object getOblivionTargetInstance() {
    return this.originalTarget;
  }
}

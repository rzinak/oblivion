package com.br.oblivion.util;

import com.br.oblivion.annotations.OblivionLoggable;
import com.br.oblivion.container.BeansContainer;
import com.br.oblivion.container.Pair;
import com.br.oblivion.interfaces.OblivionJoinPoint;
import com.br.oblivion.interfaces.TargetAware;
import java.lang.reflect.Method;
import java.util.List;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class OblivionCglibInterceptor implements MethodInterceptor, TargetAware {
  private final Object originalTarget;
  private boolean isClassLoggable;

  public OblivionCglibInterceptor(Object originalTarget, boolean isClassLoggable) {
    this.originalTarget = originalTarget;
    this.isClassLoggable = isClassLoggable;
  }

  @Override
  public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
      throws Throwable {
    try {
      if (method.getName().equals("getOblivionTargetInstance") && method.getParameterCount() == 0) {
        return getOblivionTargetInstance();
      }

      String currentMethod = method.getDeclaringClass().getName() + "." + method.getName();

      if (BeansContainer.beforeAdviceMap.containsKey(currentMethod)) {
        List<Pair<Method, Object>> methodPair = BeansContainer.beforeAdviceMap.get(currentMethod);
        for (Pair<Method, Object> pair : methodPair) {
          Method adviceMethod = pair.getL();
          Object aspectInstance = pair.getR();
          Object targetToInvoke = null;

          OblivionJoinPoint jp =
              new MethodExecutionJoinPoint(this.originalTarget, obj, method, args);

          if (aspectInstance instanceof TargetAware) {
            targetToInvoke = ((TargetAware) aspectInstance).getOblivionTargetInstance();
          } else {
            targetToInvoke = aspectInstance;
          }

          if (adviceMethod.getParameterCount() == 0) {
            adviceMethod.invoke(targetToInvoke, jp);
          } else {
            adviceMethod.invoke(targetToInvoke, jp);
          }
        }
      }

      if (method.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[GCLIB PROXY] intercepting method -> " + method.getName());
      }

      Object result = method.invoke(this.originalTarget, args);

      if (method.isAnnotationPresent(OblivionLoggable.class) || isClassLoggable) {
        System.out.println("[CGLIB PROXY] finished method -> " + method.getName());
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

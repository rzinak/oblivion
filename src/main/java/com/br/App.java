package com.br;

import com.br.autowired.annotations.OblivionWire;
import com.br.autowired.bean.PrototypeBean;
import com.br.autowired.bean.SingletonBean;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.lifecycle.Shutdown;
import com.br.autowired.util.Inject;
import com.br.samples.service.TaskService;
import com.br.samples.service.UserService;

public class App {

  @OblivionWire UserService userService;
  @OblivionWire TaskService taskService;

  public static void main(String[] args) throws Exception {
    Shutdown shutdownHook = new Shutdown();
    BeansContainer beansContainer = new BeansContainer();
    shutdownHook.attachShutdown(beansContainer);
    SingletonBean singletonBean = new SingletonBean();
    PrototypeBean prototypeBean = new PrototypeBean();

    try {

      App app = new App();

      Inject.inject(app, singletonBean, prototypeBean, beansContainer);

      app.testing();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  public void testing() {
    // System.out.println("TASK SERVICE: " + taskService);
    // taskService.setTaskName("Code PROTO 1");
    // taskService.setIsAvailable(false);
    // System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getTaskName());
    // System.out.println("CURRENT TASK SERVICE BOOL VAL: " + taskService.getIsAvailable());
    // System.out.println("CURRENT TASK SERVICE BOXED BOOL VAL: " + taskService.getIsRegistered());
    // System.out.println("CURRENT TASK SERVICE NORMAL INT VAL: " + taskService.getNormalInt());
    // System.out.println("CURRENT TASK SERVICE BOXED INT VAL: " + taskService.getBoxedInt());
    // System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getStringText());
    // System.out.println("CURRENT TASK SERVICE LIST VAL: " + taskService.getListString());
    // System.out.println("CURRENT TASK SERVICE MAP VAL: " + taskService.getMapStringString());
  }
}

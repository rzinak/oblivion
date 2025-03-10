package com.br;

import com.br.autowired.bean.PrototypeBean;
import com.br.autowired.bean.SingletonBean;
import com.br.autowired.container.BeansContainer;
import com.br.autowired.lifecycle.Shutdown;
import com.br.samples.model.User;
import com.br.samples.service.TaskService;

public class App {
  public static void main(String[] args) throws Exception {
    Shutdown shutdownHook = new Shutdown();
    BeansContainer container = new BeansContainer();
    shutdownHook.attachShutdown(container);
    try {
      // @OblivionSingleton
      // UserService testUser;
      SingletonBean singletonBean = new SingletonBean();
      PrototypeBean prototypeBean = new PrototypeBean();

      // singletonBean.initializeSingletonBean("testUser", UserService.class, container);
      singletonBean.initializeSingletonBean("taskService", TaskService.class, container);
      prototypeBean.registerPrototypeBean("taskProto", TaskService.class, container, "onearg");

      // Object testUserObj = container.getSingletonBean("testUser");
      Object taskServiceObj = container.getSingletonBean("taskService");
      Object taskProtoObj = container.getPrototypeBean("taskProto", container);

      // UserService testUser = UserService.class.cast(testUserObj);
      TaskService taskService = TaskService.class.cast(taskServiceObj);
      TaskService taskProto = TaskService.class.cast(taskProtoObj);

      // System.out.println("TEST USER: " + testUser);

      System.out.println("TASK SERVICE: " + taskService);
      System.out.println("TASK PROTO: " + taskProto);
      taskService.setTaskName("Code PROTO 1");
      taskService.setIsAvailable(false);
      System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getTaskName());
      System.out.println("CURRENT TASK SERVICE BOOL VAL: " + taskService.getIsAvailable());
      System.out.println("CURRENT TASK SERVICE BOXED BOOL VAL: " + taskService.getIsRegistered());
      System.out.println("CURRENT TASK SERVICE NORMAL INT VAL: " + taskService.getNormalInt());
      System.out.println("CURRENT TASK SERVICE BOXED INT VAL: " + taskService.getBoxedInt());
      System.out.println("CURRENT TASK SERVICE STRING VAL: " + taskService.getStringText());
      System.out.println("CURRENT TASK SERVICE LIST VAL: " + taskService.getListString());
      System.out.println("CURRENT TASK SERVICE MAP VAL: " + taskService.getMapStringString());

      User user1 = new User("renan", 24);
      // testUser.addUser(user1);

      taskProto.assignTaskToUser(user1);

      // System.out.println(testUser.getUsers());
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }
}

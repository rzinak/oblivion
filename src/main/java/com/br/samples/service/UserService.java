package com.br.samples.service;

import com.br.autowired.annotations.Oblivion;
import com.br.autowired.annotations.OblivionPostConstruct;
import com.br.autowired.annotations.OblivionPostInitialization;
import com.br.autowired.annotations.OblivionPreDestroy;
import com.br.autowired.annotations.OblivionPreInitialization;
import com.br.autowired.annotations.OblivionPreShutdown;
import com.br.autowired.annotations.OblivionService;
import com.br.samples.model.User;
import java.util.List;

@OblivionService
public class UserService {
  @Oblivion private List<User> users;

  // @Oblivion private String myFavColor;

  // @Oblivion private Integer myFavNumber;

  public void addUser(User user) {
    users.add(user);
  }

  @OblivionPostConstruct
  public void postConstruct() {
    System.out.println(
        "PostConstruct - running after bean is instantiated and its dependencies are injected");
  }

  @OblivionPreInitialization
  public static void preInit() {
    System.out.println(
        "PreInitialization - running before the bean is fully initialized, before DI and field init"
            + " as well");
  }

  @OblivionPostInitialization
  public static void postInit() {
    System.out.println(
        "PostInitialization - running after the bean in fully initialized and its fully ready");
  }

  @OblivionPreDestroy
  public void preDestroy() {
    System.out.println("PreDestroy - im running before the PreShutdown phase");
  }

  @OblivionPreShutdown
  public void preShutdown() {
    System.out.println("PreShutdown - im running before the container is shutdown");
  }

  public List<User> getUsers() {
    return users;
  }
}

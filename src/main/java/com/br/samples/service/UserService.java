package com.br.samples.service;

import com.br.oblivion.annotations.OblivionField;
import com.br.oblivion.annotations.OblivionPostConstruct;
import com.br.oblivion.annotations.OblivionPostInitialization;
import com.br.oblivion.annotations.OblivionPostShutdown;
import com.br.oblivion.annotations.OblivionPreDestroy;
import com.br.oblivion.annotations.OblivionPreInitialization;
import com.br.oblivion.annotations.OblivionPreShutdown;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.model.User;
import java.util.List;

@OblivionService
public class UserService {
  @OblivionField private List<User> users;

  public UserService() {}

  public void addUser(User user) {
    users.add(user);
  }

  @OblivionPreInitialization(order = 3)
  public static void thirdPreInit() {
    System.out.println(
        "THIRD | PreInitialization - running before the bean is fully initialized, before DI and"
            + " field init as well");
  }

  @OblivionPreInitialization(cond = "NAME.RENAN")
  public static void firstPreInit() {
    System.out.println(
        "FIRST | PreInitialization - running before the bean is fully initialized, before DI and"
            + " field init as well");
  }

  @OblivionPreInitialization(order = 2)
  public static void secondPreInit() {
    System.out.println(
        "SECOND | PreInitialization - running before the bean is fully initialized, before DI and "
            + " field init as well");
  }

  @OblivionPostConstruct(order = 2)
  public void secondPostConstruct() {
    System.out.println(
        "SECOND | PostConstruct - running after bean is instantiated and its dependencies are"
            + " injected");
  }

  @OblivionPostConstruct()
  public void firstPostConstruct() {
    System.out.println(
        "FIRST | PostConstruct - running after bean is instantiated and its dependencies are"
            + " injected");
  }

  @OblivionPostInitialization(order = 2)
  public static void secondPostInit() {
    System.out.println(
        "SECOND | PostInitialization - running after the bean in fully initialized and its fully"
            + " ready");
  }

  @OblivionPostInitialization
  public static void firsPostInit() {
    System.out.println(
        "FIRST | PostInitialization - running after the bean in fully initialized and its fully"
            + " ready");
  }

  @OblivionPreDestroy(order = 2)
  public void secondPreDestroy() {
    System.out.println("SECOND | PreDestroy - im running before the PreShutdown phase");
  }

  @OblivionPreDestroy
  public void firstPreDestroy() {
    System.out.println("FIRST | PreDestroy - im running before the PreShutdown phase");
  }

  @OblivionPreShutdown(order = 2)
  public void secondPreShutdown() {
    System.out.println("SECOND | PreShutdown - im running before the container is shutdown");
  }

  @OblivionPreShutdown
  public void firstPreShutdown() {
    System.out.println("FIRST | PreShutdown - im running before the container is shutdown");
  }

  @OblivionPostShutdown(order = 2)
  public void secondPostShutdown() {
    System.out.println("SECOND | PostShutdown - im running after the container is shutdown");
  }

  @OblivionPostShutdown(cond = "NAME.ASDASDASD")
  public void firstPostShutdown() {
    System.out.println("FIRST | PostShutdown - im running after the container is shutdown");
  }

  public List<User> getUsers() {
    return users;
  }
}

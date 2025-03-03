package com.br.samples.service;

import com.br.autowired.annotations.Oblivion;
import com.br.autowired.annotations.OblivionPostConstruct;
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
  public void hello() {
    System.out.println("hello");
  }

  @OblivionPreInitialization
  public static void preInit() {
    System.out.println("running before the bean in fully initialized and before DI and field init");
  }

  @OblivionPreDestroy
  public void preDestroy() {
    System.out.println("im running before the PreShutdown phase");
  }

  @OblivionPreShutdown
  public void preShutdown() {
    System.out.println("im running before the container is shutdown");
  }

  public List<User> getUsers() {
    return users;
  }
}

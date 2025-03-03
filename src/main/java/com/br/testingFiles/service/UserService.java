package com.br.testingFiles.service;

import com.br.annotations.Oblivion;
import com.br.annotations.OblivionPostConstruct;
import com.br.annotations.OblivionPreDestroy;
import com.br.annotations.OblivionService;
import com.br.testingFiles.model.User;
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

  @OblivionPreDestroy
  public void bye() {
    System.out.println("bye");
  }

  public List<User> getUsers() {
    return users;
  }
}

package com.br.testingFiles.service;

import com.br.annotations.Oblivion;
import com.br.annotations.OblivionPostConstruct;
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
  public void yo() {
    System.out.println("yo");
  }

  public List<User> getUsers() {
    return users;
  }
}

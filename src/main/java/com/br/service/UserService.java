package com.br.service;

import com.br.App.Oblivion;
import com.br.App.OblivionService;
import com.br.model.User;
import java.util.List;

@OblivionService
public class UserService {
  @Oblivion private List<User> users;

  // @Oblivion private String myFavColor;

  // @Oblivion private Integer myFavNumber;

  public void addUser(User user) {
    users.add(user);
  }

  public List<User> getUsers() {
    return users;
  }
}

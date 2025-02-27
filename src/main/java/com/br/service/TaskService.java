package com.br.service;

import com.br.App.OblivionService;
import com.br.model.User;

@OblivionService
public class TaskService {
  private final UserService userService;

  public TaskService(UserService userService) {
    this.userService = userService;
  }

  public void assignTaskToUser(String taskName, User user) {
    System.out.println("assigning task: " + taskName + " to user: " + user.getName());
    userService.addUser(user);
  }
}

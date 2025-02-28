package com.br.testingFiles.service;

import com.br.annotations.OblivionPrototype;
import com.br.annotations.OblivionService;
import com.br.testingFiles.model.User;

@OblivionPrototype
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

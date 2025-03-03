package com.br.samples.service;

import com.br.autowired.annotations.OblivionPrototype;
import com.br.autowired.annotations.OblivionService;
import com.br.samples.model.User;

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

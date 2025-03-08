package com.br.samples.service;

import com.br.autowired.annotations.OblivionField;
import com.br.autowired.annotations.OblivionPrototype;
import com.br.autowired.annotations.OblivionService;
import com.br.samples.model.User;

@OblivionService
@OblivionPrototype
public class TaskService {
  private final UserService userService;

  @OblivionField private String taskName;
  @OblivionField private boolean isAvailable;

  public TaskService(UserService userService) {
    this.userService = userService;
  }

  public TaskService(String taskName, UserService userService) {
    this.taskName = taskName;
    this.userService = userService;
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public boolean getIsAvailable() {
    return isAvailable;
  }

  public void setIsAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }

  public void assignTaskToUser(User user) {
    System.out.println(
        "assigning task: "
            + taskName
            + " to user: "
            + user.getName()
            + ". Available: "
            + isAvailable);
    userService.addUser(user);
  }
}

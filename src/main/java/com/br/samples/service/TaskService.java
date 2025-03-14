package com.br.samples.service;

import com.br.autowired.annotations.OblivionField;
import com.br.autowired.annotations.OblivionPrototype;
import com.br.autowired.annotations.OblivionService;
import com.br.samples.model.User;
import java.util.List;
import java.util.Map;

@OblivionService
@OblivionPrototype
public class TaskService {
  private final UserService userService;

  @OblivionField private String taskName;

  @OblivionField private String stringText;

  @OblivionField private boolean isAvailable;

  @OblivionField private Boolean isRegistered;

  @OblivionField private int normalInt;

  @OblivionField private Integer boxedInt;

  @OblivionField private List<String> listString;

  @OblivionField private Map<String, String> mapStringString;

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

  public Boolean getIsRegistered() {
    return isRegistered;
  }

  public void setIsRegistered(Boolean isRegistered) {
    this.isRegistered = isRegistered;
  }

  public void setIsAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }

  public int getNormalInt() {
    return normalInt;
  }

  public void setNormalInt(int normalInt) {
    this.normalInt = normalInt;
  }

  public Integer getBoxedInt() {
    return boxedInt;
  }

  public void setBoxedInt(Integer boxedInt) {
    this.boxedInt = boxedInt;
  }

  public String getStringText() {
    return stringText;
  }

  public void setStringText(String stringText) {
    this.stringText = stringText;
  }

  public List<String> getListString() {
    return listString;
  }

  public Map<String, String> getMapStringString() {
    return mapStringString;
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

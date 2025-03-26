package com.br.samples.testAppTaskManager.repository;

import com.br.oblivion.annotations.*;
import com.br.samples.testAppTaskManager.model.Task;
import java.util.ArrayList;
import java.util.List;

@OblivionService
public class TaskRepository {

  private List<Task> tasks = new ArrayList<>();

  @OblivionPostConstruct
  public void r1() {
    System.out.println("CLI | repository 1");
  }

  public void addTask(Task task) {
    tasks.add(task);
  }

  public List<Task> getAllTasks() {
    return tasks;
  }
}

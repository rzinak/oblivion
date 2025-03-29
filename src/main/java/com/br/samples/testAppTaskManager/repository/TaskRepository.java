package com.br.samples.testAppTaskManager.repository;

import com.br.oblivion.annotations.*;
import com.br.samples.testAppTaskManager.model.Task;
import java.util.ArrayList;
import java.util.List;

@OblivionService
public class TaskRepository {
  // private TaskService taskService;

  // public TaskRepository(TaskService taskService) {
  //   this.taskService = taskService;
  // }

  private List<Task> tasks = new ArrayList<>();

  public void addTask(Task task) {
    tasks.add(task);
  }

  public List<Task> getAllTasks() {
    return tasks;
  }
}

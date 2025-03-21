package com.br.samples.testAppTaskManager.service;

import com.br.oblivion.annotations.OblivionService;
import com.br.samples.testAppTaskManager.model.Task;
import com.br.samples.testAppTaskManager.repository.TaskRepository;

@OblivionService
public class TaskService {
  private TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public void addTask(String title, String description) {
    taskRepository.addTask(new Task(title, description));
  }

  public void showTasks() {
    taskRepository.getAllTasks().forEach(System.out::println);
  }
}

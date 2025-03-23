package com.br.samples.testAppTaskManager.service;

import com.br.oblivion.annotations.OblivionPostConstruct;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.testAppTaskManager.model.Task;
import com.br.samples.testAppTaskManager.repository.TaskRepository;

@OblivionService
public class TaskService {
  private TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @OblivionPostConstruct
  public void t1() {
    System.out.println("CLI | service 1");
  }

  @OblivionPostConstruct
  public void t2() {
    System.out.println("CLI | service 2");
  }

  @OblivionPostConstruct
  public void t3() {
    System.out.println("CLI | service 3");
  }

  public void addTask(String title, String description) {
    taskRepository.addTask(new Task(title, description));
  }

  public void showTasks() {
    taskRepository.getAllTasks().forEach(System.out::println);
  }
}

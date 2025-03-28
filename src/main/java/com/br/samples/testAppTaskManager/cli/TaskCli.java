package com.br.samples.testAppTaskManager.cli;

import com.br.oblivion.annotations.OblivionPostConstruct;
import com.br.oblivion.annotations.OblivionPostInitialization;
import com.br.oblivion.annotations.OblivionPostShutdown;
import com.br.oblivion.annotations.OblivionPreDestroy;
import com.br.oblivion.annotations.OblivionPreInitialization;
import com.br.oblivion.annotations.OblivionPreShutdown;
import com.br.oblivion.annotations.OblivionPrototype;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.testAppTaskManager.service.TaskService;
import java.util.Scanner;

@OblivionService
@OblivionPrototype
public class TaskCli {
  private TaskService taskService;

  public TaskCli(TaskService taskService) {
    this.taskService = taskService;
  }

  @OblivionPreInitialization
  public static void preInit() {
    System.out.println("TASK CLI | PreInitialization");
  }

  @OblivionPostConstruct
  public void postConstruct() {
    System.out.println("TASK CLI | PostConstruct");
  }

  @OblivionPostInitialization
  public void postInitialization() {
    System.out.println("TASK CLI | PostInitialization");
  }

  @OblivionPreDestroy
  public void preDestroy() {
    System.out.println("TASK CLI | PreDestroy");
  }

  @OblivionPreShutdown
  public void preShutdown() {
    System.out.println("TASK CLI | PreShutdown");
  }

  @OblivionPostShutdown
  public void postShutdown() {
    System.out.println("TASK CLI | PostShutdown");
  }

  public void run() {
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.println("Task Manager CLI");
      System.out.println("1. Add Task");
      System.out.println("2. Show Tasks");
      System.out.println("3. Exit");
      System.out.print("> ");
      int choice = scanner.nextInt();
      scanner.nextLine();

      switch (choice) {
        case 1 -> {
          System.out.print("Enter title: ");
          String title = scanner.nextLine();
          System.out.print("Enter description: ");
          String description = scanner.nextLine();
          taskService.addTask(title, description);
        }
        case 2 -> taskService.showTasks();
        case 3 -> {
          System.out.println("Exiting...");
          return;
        }
        default -> System.out.println("Invalid option, try again.");
      }
    }
  }
}

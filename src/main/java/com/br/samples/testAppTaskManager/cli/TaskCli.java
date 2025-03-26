package com.br.samples.testAppTaskManager.cli;

import com.br.oblivion.annotations.OblivionPostConstruct;
import com.br.oblivion.annotations.OblivionService;
import com.br.samples.testAppTaskManager.service.TaskService;
import java.util.Scanner;

@OblivionService
public class TaskCli {
  private TaskService taskService;

  public TaskCli(TaskService taskService) {
    this.taskService = taskService;
  }

  @OblivionPostConstruct(order = 0)
  public static void cli1() {
    System.out.println("CLI 1");
  }

  @OblivionPostConstruct(order = 1)
  public static void cli2() {
    System.out.println("CLI 2");
  }

  @OblivionPostConstruct(order = 3)
  public static void cli3() {
    System.out.println("CLI 3");
  }

  @OblivionPostConstruct
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

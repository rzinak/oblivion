# Oblivion - Lightweight Dependency Injection Framework

Oblivion is a custom, lightweight dependency injector framework built to help you manage dependencies in your Java applications without the overhead of frameworks like Spring. It's designed to keep things simple while giving you all the DI power you need for production-level apps.

### Features

- **Constructor Injection** *(Dependencies are injected through the constructor)*
- **Field Injection** *(For certain types, class dependencies should be injected via constructor)*
- **Lifecycle Callbacks** *(Manage the lifecycle of your beans through customized phases)*
- **Custom Annotations** *(For more control over the DI setup)*
- **Singleton Beans** *(Default bean type)*
- **Prototype Beans** *(Create a new instance each time)*
- **Unique Bean Naming** *(Name your beans to avoid confusion in large projects)*

---

### Annotations

- `@OblivionService`: Marks a class as a service to be injected.

- `@OblivionWire`: Injects a top-level class.

- `@OblivionConstructorInject`: Marks a specific constructor to inject (in case multiple constructors exist).

- `@OblivionPreInitialization`: Executes before field injections.

- `@OblivionPostConstruct`: Executes right after dependencies are injected.

- `@OblivionPostInitialization` - Executes a method after full initialization *(ideal for advanced setups like starting background tasks)*.

- `@OblivionPreDestroy`: Executes before the bean is destroyed.

- `@OblivionPostShutdown`: Executes after the container shuts down.

- `@OblivionPrototype`: Defines prototype beans (each request creates a new instance).

- `@OblivionPreShutdown`: Executes before the container shuts down.

- `@OblivionPostShutdown` - Executes a method after the container shuts down.

### Quick Start

*Note that this is now 100% the way I expect Oblivion to be used in the future, it's still in a very early development stage and implementation in external apps is very limited.*

1. **Add Dependencies**

First you gotta add Oblivion as a dependency in your `pom.xml` or `build.gradle`. *For now, you can install it as a local dependency.*

2. **Main File Setup**

Here's an example of how you can setup and initialize Oblivion in a simple application:

```java
public class Main {

  @OblivionWire
  private static TaskService taskService; // Injects the TaskService class

  public static void main(String[] args) throws Exception {
    Main app = new Main();
    OblivionSetup.init(app);  // Initializes the DI container and injects dependencies

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
          taskService.addTask(title, description); // Task added via DI
        }
        case 2 -> taskService.showTasks(); // Display tasks via DI
        case 3 -> {
          System.out.println("Exiting...");
          return;
        }
        default -> System.out.println("Invalid option, try again.");
      }
    }
  }
}
```

- `@OblivionWire` is used to inject the TaskService class.

*@OblivionWire* takes an optional `constructorToInject = STRING`, with the desired constructor to be injected:

```java
@OblivionWire(constructorToInject = "CONSTRUCTOR IDENTIFIER")
TaskService taskService;

```

The name of the constructor is defined inside it's class, with an annotation `@OblivionConstructorInject(name = STRING)` above the constructor. So the name you give it there, is the same you pass to *@OblivionWire*.

- `OblivionSetup.init(app)` initializes the DI container and wires up the necessary dependencies.

3. **Service Layer**

The service class uses constructor-based injection to manage dependencies:

```java
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
```

- `TaskService` requires `TaskRepository` to be injected via its constructor.

---

### Advanced Features

**Custom Lifecycle Annotations**

You can control how your beans behave during different phases:

- `@OblivionPreInitialization`: Run logic before the bean is fully initialized.
- `@OblivionPostConstruct`: Run logic right after the bean is injected.
- `@OblivionPostInitialization` - Run logic after full bean initialization.
- `@OblivionPreDestroy`: Cleanup before the bean is destroyed.
- `@OblivionPreShutdown`: Cleanup before the container shuts down.
- `@OblivionPostShutdown`: Cleanup after the container shuts down.

Additionally, you can control the order of lifecycle methods using the `order` attribute, like so:

```java
@OblivionPreInitialization(order = 1)
@OblivionPreInitialization(order = 2)
```

**Async Lifecycle Methods**

Run lifecycle methods asynchronously by adding `async = true`:

```java
@OblivionPostConstruct(async = true)
public void initializeTaskRepository() {
    // Initialization logic
}
```

If you don't want it, there's no need to use `async = false`, if you don't specify the async property, false is assumed by default.


Async methods are supported in the following annotations:

- @OblivionPostInitialization.
- @OblivionPostConstruct.
- @OblivionPostShutdown.

**Run lifecycle methods only under specific conditions**

Methods with the condition not matching the value in the `oblivion.properties` file are skipped and will not be executed.

Configurations are made in the `oblivion.properties` file like this: `KEY=VALUE`.

```java
@OblivionPostConstruct(cond = "ENV.PROD")
```

---

### Future work

- **Handle Circular Dependencies** (more scalable DI management).
- **Improve Resource Cleanup** (like closing DB connections).
- **Custom Lifecycle Phases** (let users define custom lifecycle annotations).
- **Improve app's entry point detection** (config files, manifest files... but for now it's alright).

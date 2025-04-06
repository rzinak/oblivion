# Oblivion - Lightweight Dependency Injection Framework

Oblivion is a custom, lightweight dependency injector framework built to help you manage dependencies in your Java applications without the overhead of frameworks like Spring. It's designed to keep things simple while giving you all the DI power you need for production-level apps.

### Features

- **Constructor Injection**
- **Field Injection**
- **Lifecycle Callbacks**
- **Custom Annotations**
- **Singleton Beans**
- **Prototype Beans**
- **Unique Bean Naming**
- **Wire Beans Using a Config File**

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

- `@OblivionPrimary` - Marks a default interface's implementation to be injected.

- `@OblivionQualifier` - Explicitly indicate an interface's implementation to be injected.

### Quick Start

*Note that this is not 100% the way I expect Oblivion to be used in the future, it's still in a very early development stage and implementation in external apps is very limited.*

1. **Add Dependencies**

First you gotta add Oblivion as a dependency in your `pom.xml` or `build.gradle`. *For now, you can install it as a local dependency.*

2. **Main File Setup**

Here's an example of how you can setup and initialize Oblivion in a simple application:

```java
public class Main {

  // Injects the TaskService class. This is one way we can inject a bean, the other one
  // is by using the 'oblivion.config' file. If using the config file, there's no need
  // to add the code below
  @OblivionWire
  private static TaskCli taskCli; 

  public static void main(String[] args) throws Exception {
    try {
      OblivionSetup.preLoadConfigFile(); // Load beans to wire from the 'oblivion.config' file 
      OblivionSetup.init(); // Initializes the DI container and injects dependencies

      // The method below is used in case we use @OblivionWire to wire the TaskCli class
      // directly in the main file, but if we are using the 'oblivion.config' file, there
      // won't be any reference to 'taskCli', for example. So we can a bean lifecycle annotation
      // like @OblivionPostConstruct for example, that can run the 'run' method automatically.
      taskCli.run();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
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

3. **TaskCli Class**

```java
@OblivionService // Necessary annotation to show Oblivion that we'll be working with this class
@OblivionPrototype // If this class is supposed to be a prototype bean, we use this. Otherwise we remove this annotation
public class TaskCli {
  private TaskService taskService;

  @OblivionConstructorInject // Optional annotation. Marks a constructor to be injected, otherwise, the first is injected
  public TaskCli(TaskService taskService) {
    this.taskService = taskService;
  }

  // This makes this method execute after all dependencies have been injected. Like I said in the main file snippet,
  // it can be used when wiring a bean from the oblivion.config file, if that's not the case, we can call run() normmally
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

4. **Service Class**

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

5. **Repository Class**

```java
@OblivionService
public class TaskRepository {

  private List<Task> tasks = new ArrayList<>(); 

  public void addTask(Task task) {
    tasks.add(task);
  }

  public List<Task> getAllTasks() {
    return tasks;
  }
}
```

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

**Wire Beans From External File**

We can also wire a bean using the `oblivion.config` file.

Very limited for now. Later I will add support for more annotations in the config file and also change the file type so we can use annotation when wiring
beans using it.

The `oblivion.config` expects the following structure: `Annotation:Class`.

So we can use it like this: `OblivionWire:com.br.samples.testAppTaskManager.cli.TaskCli`

---

### Future work

- **Improve Error Handling**
- **Improve Resource Cleanup** (like closing DB connections).
- **Custom Lifecycle Phases** (let users define custom lifecycle annotations).

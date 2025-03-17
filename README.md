Going in-depth on how Spring's `@Autowired` works, so I'm building a custom DI framework from scratch.

## Features Implemented

### Injection Mechanisms

- **Field Injection**
- **Constructor Injection**
- **Unique Bean Naming** *(assign unique names to instances)*
- **Prototype Beans** *(create independent instances even when using the same identifier)*
- **Basic Bean Lifecycle Management**

## Custom Annotations

- `@OblivionField` - Injects dependencies into fields *(support for more types pending, will do it eventually tho)*
- `@OblivionService` - Marks a class as a service; searches for constructors and injects dependencies
- `@OblivionPrototype` - Defines a prototype bean *(each request gets a new instance)*
- `@OblivionConstructorInject` - Defines a specific constructor to be injected. If none is specified, the first constructor is injected by default.
    It can receive an optional `name = String` property that can be used to instantiate a class with different constructors when defining a prototype bean.
- `@OblivionPreInitialization` - Executes a method before the bean is fully initialized *(before field injections)*
- `@OblivionPostConstruct` - Executes a method immediately after dependencies are injected *(for basic setup)*
- `@OblivionPostInitialization` - Executes a method after full initialization *(ideal for advanced setups like starting background tasks)*
- `@OblivionPreDestroy` - Executes a method before the bean is destroyed
- `@OblivionPreShutdown` - Executes a method before the container shuts down
- `@OblivionPostShutdown` - Executes a method after the container shuts down
- `@OblivionWire` - Initilize a bean

## Current Status

Beans are instantiated automatically and can be used normally.

Added a new annotation, `@OblivionWire`, to initialize beans. Instantiation was simplified to remove manual bean fetching, so we only need to do this:

```java
@OblivionWire
TaskService taskService;

```

The bean will be created using the identifier name to distinguish it from other beans, similar to the previous approach where we had to pass them manually. The way it works is the same, but now there's no need to do that manually.

*@OblivionWire* takes an optional `constructorToInject = STRING`, with the desired constructor to be injected:

```java
@OblivionWire(constructorToInject = "CONSTRUCTOR IDENTIFIER")
TaskService taskService;

```

The name of the constructor is defined inside it's class, with an annotation `@OblivionConstructorInject(name = STRING)` above the constructor. So the name you give it there, is the same you pass to *@OblivionWire*.

---

### Lifecycle Callbacks

Implemented an easy way to manage the lifecycle of a bean in phases.

### Implemented Lifecycle Phases

- `@OblivionPreInitialization`
- `@OblivionPostConstruct`
- `@OblivionPostInitialization`
- `@OblivionPreDestroy`
- `@OblivionPreShutdown`
- `@OblivionPostShutdown`

---

### Implemented Lifecycle extra features

- **Ordered Lifecycle methods using `order = N`. Here the second method will be executed first**:

```java
@OblivionPreInitialization(order = 2)
@OblivionPreInitialization(order = 1)
```

- **Add attributes to lifecycle annotations to run methods *only under specific conditions***.

```java
@OblivionPostConstruct(cond = "ENV.PROD")
```

Methods with the condition not matching the value in the `oblivion.properties` file are skipped and will not be executed.

Configurations are made in the `oblivion.properties` file like this: `KEY=VALUE`.

- **Async lifecycle methods by using `async = BOOL`. Later will use a config file for configuring the thread pool**.

Not all lifecycle phases will have async functionality, because it would make some of them very easy to cause a mess during execution of methods.

These are the ones that can use *@async*:

- @OblivonPostInitialization

- @OblivonPostConstruct

- @OblivonPostShutdown

To use it, it's very simple. We just need to pass an `@async` flag like this: `@OblivionPostInitialization(async = true)`. If you don't want a method to run asynchronously, you can just omit the *async* property, `@OblivionPostInitialization`. A *false* value is assumed in this case.

---

## Cool Things to Implement Later

### Custom Lifecycle Phases

Let users define **custom lifecycle annotations**.

Haven't figured out an approach yet, but will do it eventually because it looks cool.

---

## Other Things To Do

- **Improve this readme**
- **Better error handling**
- **Improve resource cleanup** for `@OblivionPreDestroy` (e.g., closing DB connections)
- **Handle circular dependencies** (maybe add a custom cleanup phase for scalability)
- **Make threads config dynamic**: move ThreadPoolExecutor values to a config file

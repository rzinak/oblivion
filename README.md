Going in-depth on how Spring's `@Autowired` works, so I'm building a custom DI framework from scratch.

## Features Implemented

### Injection Mechanisms

- **Field Injection**
- **Constructor Injection**
- **Unique Bean Naming** *(assign unique names to instances)*
- **Prototype Beans** *(create independent instances even when using the same identifier)*
- **Basic Bean Lifecycle Management**

## Custom Annotations

- `@Oblivion` - Injects dependencies into fields *(support for more types pending, will do it eventually tho)*
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

## Current Status

Beans are instantiated automatically and can be used normally.

---

### Lifecycle Callbacks

Manage the lifecycle of a bean in phases.

### Implemented Lifecycle Phases

- `@OblivionPreInitialization`
- `@OblivionPostConstruct`
- `@OblivionPostInitialization`
- `@OblivionPreDestroy`
- `@OblivionPreShutdown`
- `@OblivionPostShutdown`

---

### Implemented Lifecycle extra features

- Ordered Lifecycle methods using `order = N`. Here the second method will be executed first:

```java
@OblivionPreInitialization(order = 2)
@OblivionPreInitialization(order = 1)
```

- Add attributes to lifecycle annotations to run methods **only under specific conditions**.

```java
@OblivionPostConstruct(cond = "ENV.PROD")
```

Methods with the condition not matching the value in the `oblivion.properties` file are skipped and will not be executed.

Configurations are made in the `oblivion.properties` file like this: `KEY=VALUE`.

## Cool Features to Implement Later

### Async Lifecycle Methods

Allow lifecycle methods to run asynchronously for **better performance** (e.g., long-running setups).

```java
@OblivionPostConstruct(async = true)
```

This can be done using a thread pool.

### Custom Lifecycle Phases

Let users define **custom lifecycle annotations**.

Haven't figured out an approach yet, but will do it eventually because it looks cool.

## Other Things To Do

- **Better error handling**
- **Improve resource cleanup** for `@OblivionPreDestroy` (e.g., closing DB connections)
- **Handle circular dependencies** (maybe add a custom cleanup phase for scalability)

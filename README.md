going in-depth on how @Autowired works, so im building one myself

### what i have done so far

- field injection
- constructor injection (aint tweaked for multiple constructors with args yet)
- you can give an unique name to your instance
- prototype beans (gives us independent instances even when using the same identifier)
- some bean lifecycle features

also created some custom annotations to use:

@Oblivion: to inject fields (need to add support for more types tho)

@OblivionService: to inject the class itself. also searches for constructors inside it, then inject them

@OblivionPrototype: to create a prototype bean

@OblivionPreInitialization: marks a method to be executed before the bean is fully initialized, it's ran even before field initializations

@OblivionPostConstruct: marks a method to be executed immediately after its dependencies are injected.
good for basic setup that depend on injected dependencies

@OblivionPostInitialization: marks a method to be executed after the bean is fully initialized.
good for advanced setups that require the bean to be fully ready, like starting a background thread for example.

@OblivionPreDestroy: marks a method to be executed before the bean is destroyed

@OblivionPreShutdown: marks a method to be executed before the container is shutdown 

rn classes are instantiated automatically and we can use normally

### next goal

- implement lifecycle callbacks *(current doing this, doing independent phases one at a time...)*

basically i must find a way to manage the lifecycle of a bean, lol, simple to explain but idk about doing it

and for obvious reasons, for now im not going crazy like the way Spring Bean works, cuz it has many steps, both
during creation and destruction of a bean

so im thinking of simple operations for now, and making it more complex over time

**lifecycle phases i added**

@OblivionPostConstruct
@OblivionPreDestroy
@OblivionPreShutdown
@OblivionPreInitialization
@OblivionPostInitialization
@OblivionPostShutdown

**other cool things to implement**

*conditional lifecycle methods*: i belive this involves adding attributes to lifecycle annotations,
like running a method only if a property is set, or only if a particular bean is present in the container

`@OblivionPostConstruct(cond = "env.prod")`

basically before invoking the method i must evaluate the condition

*async lifecycle methods*: it would be cool to allow methods to run asynchronously, its good for performing
long-running initializations or cleanup tasks without blocking the main thread. and it would also improve performance
by parallelizing lifecycle tasks (i believe this feature is a must in case this project grows considerably)

i believe that it can be implement similar to the idea above, by adding an attribute to the lifecycle, since adding
a custom @Async annotation will probably make it look like its related to the DI itself and not the lifecycle.

`@OblivionPostConstruct(async = true)`

can use a thread pool to invoke them

*ordered lifecycle methods*: this is some cool thing i was thinking, like ordering lifecycle calls

lets say i have these two annotations:

`@OblivionPostConstruct(order = 1)`
`@OblivionPostConstruct(async = 2)`

this way we have more control on when things are running

to implement this gotta sort them by their order before invoking

*custom lifecycles*: maybe i can allow users to define their own custom lifecycle phases, by allowing them to
register custom lifecycle annotations.

aint think of a way to do this yet

### other things to do

- better error handling

- better resource cleanup for @OblivionPreDestroy like if the beans are holding resources (db connections for example), but thats some veeeery "late game" thing

- for scalability, i will prob have to enhance the clean up logic for @PreDestroy, and other similar annotations, for example handling circular dependencies, and maybe
(a big maybe) add support to a custom cleanup phase

- redo this readme

Going in-depth on how Spring's `@Autowired` works, so I'm building a custom DI framework from scratch.

## Features Implemented

### Injection Mechanisms

- **Field Injection**
- **Constructor Injection** *(not yet tweaked for multiple constructors with arguments)*
- **Unique Bean Naming** *(assign unique names to instances)*
- **Prototype Beans** *(create independent instances even when using the same identifier)*
- **Basic Bean Lifecycle Management*

## Custom Annotations

- `@Oblivion` - Injects dependencies into fields *(support for more types pending, will do it eventually tho)*
- `@OblivionService` - Marks a class as a service; searches for constructors and injects dependencies
- `@OblivionPrototype` - Defines a prototype bean *(each request gets a new instance)*
- `@OblivionPreInitialization` - Executes a method before the bean is fully initialized *(before field injections)*
- `@OblivionPostConstruct` - Executes a method immediately after dependencies are injected *(for basic setup)*
- `@OblivionPostInitialization` - Executes a method after full initialization *(ideal for advanced setups like starting background tasks)*
- `@OblivionPreDestroy` - Executes a method before the bean is destroyed
- `@OblivionPreShutdown` - Executes a method before the container shuts down
- `@OblivionPostShutdown` - Executes a method after the container shuts down

## Current Status

Beans are instantiated automatically and can be used normally.

---

## Next Steps

### Lifecycle Callbacks *(In Progress)*  

Managing the lifecycle of a bean in phases, starting simple and adding complexity over time.

### Implemented Lifecycle Phases

- `@OblivionPreInitialization`
- `@OblivionPostConstruct`
- `@OblivionPostInitialization`
- `@OblivionPreDestroy`
- `@OblivionPreShutdown`
- `@OblivionPostShutdown`

---

## Cool Features to Implement 

### Conditional Lifecycle Methods  

Add attributes to lifecycle annotations to run methods **only under specific conditions**.

```java
@OblivionPostConstruct(cond = "env.prod")
```

Before invoking the method, the condition must be evaluated.

### Async Lifecycle Methods

Allow lifecycle methods to run asynchronously for **better performance** (e.g., long-running setups).

```java
@OblivionPostConstruct(async = true)
```

This can be done using a thread pool.

### Ordered Lifecycle Methods

Control the **execution order** of lifecycle callbacks:

```java
@OblivionPostConstruct(order = 1)
@OblivionPostConstruct(order = 2)
```

Before invocation, methods must be sorted by order.

### Custom Lifecycle Phases

Let users define **custom lifecycle annotations**.

Haven't figured out an approach yet, but will do it eventually because it seems cool.

## Other Things To Do

- **Better error handling**
- **Improve resource cleanup** for `@OblivionPreDestroy` (e.g., closing DB connections)
- **Handle circular dependencies** (maybe add a custom cleanup phase for scalability)

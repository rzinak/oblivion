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

@OblivionPostConstruct: marks a method to be executed after the bean is fully constructed

@OblivionPreDestroy: marks a method to be executed before the bean is destroyed

rn classes are instantiated automatically and we can use normally

### next goal

- implement lifecycle callbacks *(current doing this, doing independent phases one at a time...)*

this look complex...

basically i must find a way to manage the lifecycle of a bean, lol, simple to explain but idk about doing it

for obvious reasons, for now im not going crazy like the way Spring Bean works, cuz it has many steps, both
during creation and destruction of a bean

so im thinking of simple operations for now, and making it more complex over time

### other things

- better error handling

- better resource cleanup for @OblivionPreDestroy like if the beans are holding resources (db connections for example), but thats some veeeery "late game" thing

- for scalability, i will prob have to enhance the clean up logic for @PreDestroy, and other similar annotations, for example handling circular dependencies, and maybe
(a big maybe) add support to a custom cleanup phase

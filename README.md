# DI from scratch

going in-depth on how @Autowired works, so im building one myself

### what i have done so far

- field injection
- constructor injection (aint tweaked for multiple constructors with args yet)
- you can give an unique name to your instance
- prototype beans (gives us independent instances even when using the same identifier)

also created some custom annotations to use:

@Oblivion: to inject fields (need to add support for more types tho)

@OblivionService: to inject the class itself. also searches for constructors inside it, then inject them

@OblivionPrototype: to create a prototype bean

rn classes are instantiated automatically and we can use normally

### next goal

- implement lifecycle callbacks

this look complex...

basically i must find a way to manage the lifecycle of a bean, lol, simple to explain but idk about doing it

for obvious reasons, for now im not going crazy like the way Spring Bean works, cuz it has many steps, both
during creation and destruction of a bean

so im thinking of simple operations for now, and making it more complex over time

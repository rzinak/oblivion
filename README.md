# DI from scratch

going in-depth on how @Autowired works, so im building one myself

### what i have done so far

- field injection
- constructor injection (aint tweaked for multiple constructors with args yet)
- you can give an unique name to your instance

also created two annotations to use: @Oblivion and @OblivionService.

@Oblivion: to inject fields (need to add support for more types tho)

@OblivionService: to inject the class itself. also searches for constructors inside it, then inject them

rn classes are instantiated automatically and we can use normally

### next goal

- implement prototype beans

rn beans are singleton, so i wanna add prototype beans, meaning i prob have to
instantiate them when i retrieve them from the container.

this will allow me to use more instances of the same instance.

example:

lets say i have a "userService" bean, if i need to use this class in other two different classes,
i would have to create a "userService2" bean for example.

so the goal here is to be able to use "userService" in class X, and also "userService" in class Y.

### future things i might consider doing

- field injection for dependencies

this one is kinda shady, cuz it might introduce hidden dependencies that could screw things up

- lifecycle callbacks

this i think is a must, idk

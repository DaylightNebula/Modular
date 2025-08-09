# Modular
The goal of this framework is to allow users to easily implement and execute listener functions using their own
annotations.  This allows users to quickly call events or execute functions without needing direct references everywhere.
This is useful in servers to avoid boilerplate when defining endpoints or in Minecraft servers to listen for events without
needing to implicitly define listeners.  This is also useful as it allows libraries, APIs and frameworks to automatically
set themselves up and tie themselves into libraries like Minestom without any intervention needed from the user.

While this project is written in Kotlin, it is designed to work with any JVM language that supports annotations. 

### An example
You can write your annotations like this.
```
// In Java
@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public @interface CustomStartAnnotation() {}

// In Kotlin
@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CustomStartAnnotation()
```

And to call all functions marked with `CustomStartAnnotation`, you can run the following:
```
// In Java
Modular.INSTANCE.execute(CustomStartAnnotation.class);

// In Kotlin
Modular.execute(CustomStartAnnotation::class.java)
```

### What about function arguments?
Sometimes we need to be able to call these annotated functions with arguments, which can be done by adding arguments
onto the end of the Modular execute call like this:
```kotlin
Modular.execute(CustomStartAnnotation::class.java, arrayOf("Test String", 123, false))
```
These arguments will be type tested by the `execute` call before pasting to functions so any marked functions that has
a different number of arguments or arguments of differing types than those passed to the `execute` function will fail
silently.  This is to allow for users to define more generic annotations like `@Event` that the `execute` function may call
with any event, but only marked functions with the event of the same type as what was passed to the `execute` function will
be run.

### How do I use this?
To use this library, you only need to define your own annotations and include the following annotation processor and executor
using these two lines in your gradle dependencies:
```kotlin
dependencies {
    annotationProcessor("io.github.DaylightNebula:modular-processor:0.1")
}
```

### Executor
To execute and use your annotations, you will need to write code to execute those functions.  Here's an example from the
Minestom executor of how to initialize `Modular` and then run the `@Enable` annotations.

```kotlin
fun startMinestomServer() {
    // ... Initialize Minestom server
    Modular.init()
    Modular.execute(Enable::class.java, arrayOf())
}
```

Note: The `Modular.init()` function must be called before any `execute` calls are made as this is what loads all of your
listeners and prepares the information that Modular may need for executing your annotated functions.

## Dependencies
All projects using this library must implement the annotation processor to load the annotation and listener lists required
for a functioning system.

```kts
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // For java projects
    annotationProcessor("com.github.DaylightNebula:modular-processor:0.1")
    
    // For kotlin projects (will require the kapt plugin)
    kapt("com.github.DaylightNebula:modular-processor:0.1")
}
```

If you are writing your own executor, you will need to implement the executor library module to have access to the 
`Modular` singleton.

```kts
dependencies {
    implementation("com.github.DaylightNebula:modular-processor:0.1")
}
```

If you want to use the Minestom library module to ease your Minestom experience, you will need to implement the following.

```kts
dependencies {
    implementation("com.github.DaylightNebula:modular-minestom:0.1")
}
```

## Minestom
This tool was originally meant to add annotations to make working with the [Minestom](https://github.com/Minestom/Minestom)
library easier and therefore annotations and executor have been made for Minestom.  This module allows you to easily create
libraries that will automatically be initialized exactly how your libraries needs without relying on users implementing
your specific startup routine themselves, allowing for an easier development experience when working with large projects.

This module of the library adds the following annotations:  `@Enable` to run functions on startup, `@Event` to run when an event is called that matches the
one any only parameter of the function, and `@Command` which creates a command that matches the command pattern given as 
an argument to the annotation and the annotated function has a `CommandSender` and `Array<String>` parameters.  This also
adds the `startMinestomServer` function to start the Minestom server as well as initialize Modular without needing to
depend on the `modular-executor` library module.

Here is an example of the above annotations and functions:
```kotlin
lateinit var instance: InstanceContainer

@Enable
fun setupInstance() {
    instance = MinecraftServer.getInstanceManager().createInstanceContainer()
    instance.setGenerator { unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK) }
    instance.setChunkSupplier { instance, cx, cz -> LightingChunk(instance, cx, cz) }
}

@Event
fun configurePlayerInstance(event: AsyncPlayerConfigurationEvent) {
    event.spawningInstance = instance
    event.player.respawnPoint = Pos(0.0, 42.0, 0.0)
}

@Command("test add <num1> <num2>")
fun testCommand(
    sender: CommandSender,
    args: Array<String>
) {
    val num1 = args[1].toIntOrNull() ?: return
    val num2 = args[2].toIntOrNull() ?: return
    sender.sendMessage("The resulting value is ${num1 + num2}")
}

fun main() = startMinestomServer()
```

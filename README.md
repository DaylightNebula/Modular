# Modular
The goal of this framework is to allow users to easily implement and execute listener functions using their own
annotations.  This allows users to quickly call events or execute functions without needing direct references everywhere.
This is useful in servers to avoid boilerplate when defining endpoints or in Minecraft servers to listen for events without
needing to implicitly define listeners.

While this project is written in Kotlin, it is designed to work with any JVM language that supports annotations.

## An example
You can write your annotations like this.
```
// In Java
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
public @interface CustomStartAnnotation() {}

// In Kotlin
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

## What about function arguments?
Sometimes we need to be able to call these annotated functions with arguments, which can be done by adding arguments
onto the end of the Modular execute call like this:
```kotlin
Modular.execute(CustomStartAnnotation::class.java, "Test String", 123, false)
```
These arguments will be type tested by the `execute` call before pasting to functions so any marked functions that has
a different number of arguments or arguments of differing types than those passed to the `execute` function will fail
silently.  This is to allow for users to define more generic annotations like `@Event` that the `execute` function may call
with any event, but only marked functions with the event of the same type as what was passed to the `execute` function will
be run.

## How do I use this?
To use this library, you only need to define your own annotations and include the following annotation processor and executor
using these two lines in your gradle dependencies:
```kotlin
dependencies {
    annotationProcessor("io.github.DaylightNebula:Modular-Processor:<VERSION>")
    implementation("io.github.DaylightNebula:Modular-Executor:<VERSION>")
}
```

package daylightnebula.modular.minestom

import daylightnebula.modular.annotations.ModularAnnotation
import daylightnebula.modular.executor.Modular
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventListener
import net.minestom.server.event.EventNode.event
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import org.reflections.Reflections
import java.lang.reflect.Modifier

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Enable()

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Event()

@Enable
fun setupMinestomListeners() {
    println("Setting up Minestom listener")

    val eventClasses = Reflections("net.minestom.server.event")
        .getSubTypesOf(Event::class.java)
        .filter { !Modifier.isAbstract(it.modifiers) && !it.isInterface }
        .toSet()

    for (eventClass in eventClasses) {
        MinecraftServer.getGlobalEventHandler()
            .addListener(eventClass) { event ->
                Modular.execute(daylightnebula.modular.minestom.Event::class.java, arrayOf(event))
            }
    }
}

fun startMinestomServer() {
    MinecraftServer.init()
        .start("0.0.0.0", 25565)
    Modular.init()
    Modular.execute(Enable::class.java, arrayOf())
}

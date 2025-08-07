package daylightnebula.modular.minestom

import daylightnebula.modular.annotations.ModularAnnotation
import daylightnebula.modular.executor.Modular
import net.minestom.server.MinecraftServer
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Enable()

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AsyncPlayerConfiguration()

@Enable
fun setupMinestomListeners() {
    println("Setting up Minestom listener")
    MinecraftServer.getGlobalEventHandler()
        .addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            Modular.execute(AsyncPlayerConfiguration::class.java, arrayOf(event))
        }
}

fun startMinestomServer() {
    MinecraftServer.init()
        .start("0.0.0.0", 25565)
    Modular.init()
    Modular.execute(Enable::class.java, arrayOf())
}

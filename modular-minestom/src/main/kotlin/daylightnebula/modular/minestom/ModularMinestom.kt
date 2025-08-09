package daylightnebula.modular.minestom

import daylightnebula.modular.annotations.ModularAnnotation
import daylightnebula.modular.executor.Modular
import daylightnebula.modular.executor.ResourceLoader
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.SimpleCommand
import net.minestom.server.event.Event
import org.reflections.Reflections
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.collections.filter

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Enable()

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Event()

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val command: String
)

data class CommandInfo(
    val args: String,
    val instance: Any?,
    val method: Method
)

@Enable
fun setupMinestomListeners() {
    // get all event class
    val eventClasses = Reflections("net.minestom.server.event")
        .getSubTypesOf(Event::class.java)
        .filter { !Modifier.isAbstract(it.modifiers) && !it.isInterface }
        .toSet()

    // register listener for every event
    for (eventClass in eventClasses) {
        MinecraftServer.getGlobalEventHandler()
            .addListener(eventClass) { event ->
                Modular.execute(daylightnebula.modular.minestom.Event::class.java, arrayOf(event))
            }
    }

    // pull list commands
    val commands = mutableMapOf<String, MutableList<CommandInfo>>()
    ResourceLoader.getConfigs().forEach { (_, config) ->
        config.listeners["daylightnebula.modular.minestom.Command"]?.forEach { function ->
            // get method and annotation of each command
            val (instance, methodName, clazz) = Modular.reflectFunction(function) ?: return@forEach
            val method = clazz.methods.firstOrNull { it.name == methodName } ?: return@forEach
            val annotation = method.annotations.firstOrNull { it.annotationClass == Command::class } as? Command ?: return@forEach

            // save commands method, instance, and command information to the above map
            val tokens = annotation.command.split(" ", limit = 2)
            val list = commands.computeIfAbsent(tokens.first()) { mutableListOf() }
            list.add(CommandInfo(if(tokens.size < 2) "" else tokens.last(), instance, method))
        }
    }

    // register each command as a simple command
    commands.forEach { (command, list) ->
        MinecraftServer.getCommandManager().register(object: SimpleCommand(command) {
            override fun process(
                sender: CommandSender,
                command: String,
                args: Array<out String>
            ): Boolean {
                // match to the "best" matching command info
                val info = list
                    .filter { matchCommandArgs(it, args) }
                    .maxByOrNull { it.args.length }

                // call command if one was found, otherwise, report options
                if (info != null) {
                    try {
                        info.method.invoke(info.instance, sender, args)
                    } catch (ex: Exception) {
                        sender.sendMessage("Failed to execute command function: ${ex.message}")
                        throw RuntimeException(ex)
                    }
                } else {
                    sender.sendMessage("Failed to find matching option for command \"$command\", options are:")
                    list.forEach { sender.sendMessage(" - /$command ${it.args}") }
                }

                return true
            }

            override fun hasAccess(sender: CommandSender, p1: String?) = true
        })
    }
}

private fun matchCommandArgs(info: CommandInfo, args: Array<out String>): Boolean {
    if (args.isEmpty() && info.args.isEmpty()) return true
    val tokens = info.args.split(" ")
    return (0 until tokens.size).all { idx ->
        // get token and make sure it's not ignorable
        val token = tokens[idx]
        if (token.startsWith("[")) return@all true

        // get matching argument, stop if out of range
        if (idx >= args.size) return@all false
        val arg = args[idx]

        if (token.startsWith("<")) return@all true
        return@all token == arg
    }
}

fun startMinestomServer() {
    MinecraftServer.init()
        .start("0.0.0.0", 25565)
    Modular.init()
    Modular.execute(Enable::class.java, arrayOf())
}

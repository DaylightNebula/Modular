package daylightnebula.modular.test.minestom

import daylightnebula.modular.minestom.Command
import daylightnebula.modular.minestom.Enable
import daylightnebula.modular.minestom.Event
import daylightnebula.modular.minestom.startMinestomServer
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block

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
    val num1 = args[1].toIntOrNull()
    val num2 = args[2].toIntOrNull()

    if (num1 == null) {
        sender.sendMessage("Failed to decode the first number.")
        return
    }

    if (num2 == null) {
        sender.sendMessage("Failed to decode the second number.")
        return
    }

    sender.sendMessage("The resulting value is ${num1 + num2}")
}

fun main() = startMinestomServer()

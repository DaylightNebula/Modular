package daylightnebula.modular.test.minestom

import daylightnebula.modular.minestom.AsyncPlayerConfiguration
import daylightnebula.modular.minestom.Enable
import daylightnebula.modular.minestom.startMinestomServer
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
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

@AsyncPlayerConfiguration
fun configurePlayerInstance(event: AsyncPlayerConfigurationEvent) {
    println("Player configuration")

    event.spawningInstance = instance
    event.player.respawnPoint = Pos(0.0, 42.0, 0.0)
}

fun main() = startMinestomServer()

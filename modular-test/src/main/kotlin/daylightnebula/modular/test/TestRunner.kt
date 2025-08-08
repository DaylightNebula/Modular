package daylightnebula.modular.test

import daylightnebula.modular.executor.Modular
import kotlin.jvm.java

fun main() {
    Modular.init()
    Modular.execute(OnStartup::class.java)
    Modular.execute(OnReload::class.java, arrayOf("Test String"))
    Modular.execute(OnShutdown::class.java)
    println("Finished")
}
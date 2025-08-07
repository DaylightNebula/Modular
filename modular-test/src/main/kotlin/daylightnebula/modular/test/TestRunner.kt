package daylightnebula.modular.test

import daylightnebula.modular.executor.Modular

fun main() {
    Modular.execute(OnStartup::class.java)
    println("Finished")
}
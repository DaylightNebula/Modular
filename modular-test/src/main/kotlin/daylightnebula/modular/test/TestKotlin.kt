package daylightnebula.modular.test

import daylightnebula.modular.annotations.OnReload
import daylightnebula.modular.annotations.OnShutdown
import daylightnebula.modular.annotations.OnStartup

object TestKotlin {
    @OnStartup
    fun onStartup() = println("TestKotlin onStartup")

    @OnShutdown
    fun onShutdown() = println("TestKotlin onShutdown")

    @OnReload
    fun onReload() = println("TestKotlin onReload")
}
package daylightnebula.modular.test

object TestKotlin {
    @OnStartup
    fun onStartup() = println("TestKotlin onStartup")

    @OnShutdown
    fun onShutdown() = println("TestKotlin onShutdown")

    @OnReload
    fun onReload(test: String) = println("TestKotlin onReload: $test")
}
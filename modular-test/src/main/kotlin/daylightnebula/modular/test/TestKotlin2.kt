package daylightnebula.modular.test

class TestKotlin2 {
    companion object {
        @OnStartup
        fun onStartup() = println("Startup from companion")

        @OnStartup
        fun onStartup2() = println("Startup from companion 2")
    }
}
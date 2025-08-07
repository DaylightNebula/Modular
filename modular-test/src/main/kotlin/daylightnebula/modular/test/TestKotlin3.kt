package daylightnebula.modular.test

import daylightnebula.modular.annotations.OnStartup

@OnStartup
fun testTopLevelFunction() = println("Top-level function invocation")
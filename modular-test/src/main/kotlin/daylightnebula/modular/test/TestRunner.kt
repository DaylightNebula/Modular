package daylightnebula.modular.test

import daylightnebula.modular.annotations.AnnotationOption
import daylightnebula.modular.executor.Modular

fun main() {
    Modular.execute(AnnotationOption.STARTUP)
    println("Finished")
}
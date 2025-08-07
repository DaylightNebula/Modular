package daylightnebula.modular.test

import daylightnebula.modular.annotations.ModularAnnotation

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class OnStartup()

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class OnShutdown()

@ModularAnnotation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class OnReload()

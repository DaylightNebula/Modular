package daylightnebula.modular.annotations

import kotlinx.serialization.Serializable

@Serializable
enum class AnnotationOption(
    val annotationClass: Class<out Annotation>,
) {
    STARTUP(OnStartup::class.java),
    SHUTDOWN(OnShutdown::class.java),
    RELOAD(OnReload::class.java);
}
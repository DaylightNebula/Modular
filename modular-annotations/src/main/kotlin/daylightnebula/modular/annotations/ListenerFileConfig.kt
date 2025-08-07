package daylightnebula.modular.annotations

import kotlinx.serialization.Serializable

@Serializable
data class ListenerFileConfig(
    // map of annotation class -> list of functions to execute
    val listeners: MutableMap<String, MutableList<FunctionInfo>>
) {
    constructor(): this(mutableMapOf())

    fun append(annotationClass: String, function: FunctionInfo) =
        listeners.computeIfAbsent(annotationClass) { mutableListOf() }.add(function)

    fun append(other: ListenerFileConfig) =
        other.listeners.forEach { (option, functions) ->
            listeners.computeIfAbsent(option) { mutableListOf() }.addAll(functions)
        }
}
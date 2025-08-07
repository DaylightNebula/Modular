package daylightnebula.modular.annotations

import kotlinx.serialization.Serializable

@Serializable
data class ListenerFileConfig(
    val listeners: MutableMap<AnnotationOption, MutableList<FunctionInfo>>
) {
    constructor(): this(mutableMapOf())

    fun append(option: AnnotationOption, function: FunctionInfo) =
        listeners.computeIfAbsent(option) { mutableListOf() }.add(function)

    fun append(other: ListenerFileConfig) =
        other.listeners.forEach { (option, functions) ->
            listeners.computeIfAbsent(option) { mutableListOf() }.addAll(functions)
        }
}
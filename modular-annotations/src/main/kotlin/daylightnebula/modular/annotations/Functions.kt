package daylightnebula.modular.annotations

import kotlinx.serialization.Serializable

@Serializable
enum class FunctionType {
    JAVA_STATIC,
    KOTLIN_COMPANION_OBJECT,
    KOTLIN_OBJECT,
    INSTANCE_METHOD
}

@Serializable
data class FunctionInfo(
    val functionType: FunctionType,
    val className: String,
    val methodName: String,
    val fullPath: String,
    val isValid: Boolean,
    val validationMessage: String = ""
)

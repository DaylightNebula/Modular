package daylightnebula.modular.executor

import daylightnebula.modular.annotations.FunctionInfo
import daylightnebula.modular.annotations.FunctionType
import daylightnebula.modular.annotations.ModularAnnotation

object Modular {
    val listeners: Map<String, List<FunctionInfo>>

    init {
        // initialize resources
        ResourceLoader.init(System.getProperty("java.class.path").split(":"))

        // load listeners
        val output = mutableMapOf<String, MutableList<FunctionInfo>>()
        ResourceLoader.getConfigs().forEach { (fileName, config) ->
            config.listeners.forEach { (option, functions) ->
                output
                    .computeIfAbsent(option) { mutableListOf() }
                    .addAll(functions)
            }
        }
        listeners = output
    }

    fun execute(clazz: Class<out Annotation>) = execute("${clazz.packageName}.${clazz.simpleName}")
    fun execute(packagePath: String) = listeners[packagePath]?.forEach(this::executeFunction)

    private fun executeFunction(info: FunctionInfo) = invokeMethod(info.functionType, info.fullPath)

    fun invokeMethod(functionType: FunctionType, fullPath: String) {
        println("Calling $functionType -> $fullPath")
        when (functionType) {
            FunctionType.JAVA_STATIC -> invokeJavaStatic(fullPath)
            FunctionType.KOTLIN_COMPANION_OBJECT -> invokeCompanionMethod(fullPath)
            FunctionType.KOTLIN_OBJECT -> invokeObjectMethod(fullPath)
            FunctionType.INSTANCE_METHOD -> throw UnsupportedOperationException("Instance methods are not supported.")
        }
    }

    private fun invokeJavaStatic(fullPath: String) {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }
        val clazz = Class.forName(className)
        val method = clazz.getMethod(methodName)
        method.invoke(null)
    }

    private fun invokeCompanionMethod(fullPath: String) {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }

        val outerClass = Class.forName(className)
        val companionField = outerClass.getDeclaredField("Companion")
        companionField.isAccessible = true
        val companionInstance = companionField.get(null)
        val method = companionInstance.javaClass.getMethod(methodName)
        method.invoke(companionInstance)
    }

    private fun invokeObjectMethod(fullPath: String) {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }
        val objectClass = Class.forName(className)
        val objectInstance = objectClass.getDeclaredField("INSTANCE").get(null)
        val method = objectClass.getMethod(methodName)
        method.invoke(objectInstance)
    }
}
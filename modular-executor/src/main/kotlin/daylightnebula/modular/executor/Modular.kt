package daylightnebula.modular.executor

import daylightnebula.modular.annotations.FunctionInfo
import daylightnebula.modular.annotations.FunctionType

object Modular {
    lateinit var listeners: Map<String, List<FunctionInfo>>

    fun init() {
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

//    fun execute(clazz: Class<out Annotation>, parameters: List<Any?>) =
//        execute("${clazz.packageName}.${clazz.simpleName}", *parameters.toTypedArray())
    fun execute(clazz: Class<out Annotation>, parameters: Array<Any?> = arrayOf()) =
        execute("${clazz.packageName}.${clazz.simpleName}", parameters)

//    fun execute(packagePath: String, parameters: List<Any?>) =
//        listeners[packagePath]?.forEach { executeFunction(it, parameters.toTypedArray()) }
    fun execute(packagePath: String, parameters: Array<Any?> = arrayOf()) =
        listeners[packagePath]?.forEach { executeFunction(it, parameters) }

    private fun executeFunction(info: FunctionInfo, parameters: Array<Any?>) {
        // get input parameter types
        val parameterTypes: Array<Class<*>?> = parameters.map { it?.javaClass }.toTypedArray()

        // get class and an instance to call the functions from
        val (instance, methodName, clazz) = when (info.functionType) {
            FunctionType.JAVA_STATIC -> invokeJavaStatic(info.fullPath) ?: return
            FunctionType.KOTLIN_COMPANION_OBJECT -> invokeCompanionMethod(info.fullPath)
            FunctionType.KOTLIN_OBJECT -> invokeObjectMethod(info.fullPath)
            FunctionType.INSTANCE_METHOD -> throw UnsupportedOperationException("Instance methods are not supported.")
        }

        // find and call method
        val method = try { clazz.getMethod(methodName, *parameterTypes) } catch (_: Exception) { null }
        method?.invoke(instance, *parameters)
    }

    private fun invokeJavaStatic(fullPath: String): Triple<Any?, String, Class<*>>? {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }
        val clazz = Class.forName(className) ?: return null
        return Triple(null, methodName, clazz)
    }

    private fun invokeCompanionMethod(fullPath: String): Triple<Any?, String, Class<*>> {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }
        val outerClass = Class.forName(className)
        val companionField = outerClass.getDeclaredField("Companion")
        companionField.isAccessible = true
        val companionInstance = companionField.get(null)
        return Triple(companionInstance, methodName, companionInstance.javaClass)
    }

    private fun invokeObjectMethod(fullPath: String): Triple<Any?, String, Class<*>> {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }
        val objectClass = Class.forName(className)
        val objectInstance = objectClass.getDeclaredField("INSTANCE").get(null)
        return Triple(objectInstance, methodName, objectClass)
    }
}
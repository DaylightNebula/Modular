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
        val parameterTypes: Array<Class<*>?> = parameters.map { it?.javaClass }.toTypedArray()
        when (info.functionType) {
            FunctionType.JAVA_STATIC -> invokeJavaStatic(info.fullPath, parameterTypes, parameters)
            FunctionType.KOTLIN_COMPANION_OBJECT -> invokeCompanionMethod(info.fullPath, parameterTypes, parameters)
            FunctionType.KOTLIN_OBJECT -> invokeObjectMethod(info.fullPath, parameterTypes, parameters)
            FunctionType.INSTANCE_METHOD -> throw UnsupportedOperationException("Instance methods are not supported.")
        }
    }

    private fun invokeJavaStatic(
        fullPath: String,
        parameterTypes: Array<Class<*>?>,
        parameters: Array<Any?>
    ) {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }
        val clazz = Class.forName(className)
        val method = try { clazz.getMethod(methodName, *parameterTypes) } catch (e: Exception) { null }
        method?.invoke(null, *parameters)
    }

    private fun invokeCompanionMethod(
        fullPath: String,
        parameterTypes: Array<Class<*>?>,
        parameters: Array<Any?>
    ) {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }

        val outerClass = Class.forName(className)
        val companionField = outerClass.getDeclaredField("Companion")
        companionField.isAccessible = true
        val companionInstance = companionField.get(null)
        val method = try { companionInstance.javaClass.getMethod(methodName, *parameterTypes) } catch (e: NoSuchMethodException) { null }
        method?.invoke(companionInstance, *parameters)
    }

    private fun invokeObjectMethod(
        fullPath: String,
        parameterTypes: Array<Class<*>?>,
        parameters: Array<Any?>
    ) {
        val (className, methodName) = fullPath.split(".").let {
            it.dropLast(1).joinToString(".") to it.last()
        }
        val objectClass = Class.forName(className)
        val objectInstance = objectClass.getDeclaredField("INSTANCE").get(null)
        val method = try { objectClass.getMethod(methodName, *parameterTypes) } catch (e: NoSuchMethodException) { null }
        method?.invoke(objectInstance, *parameters)
    }
}
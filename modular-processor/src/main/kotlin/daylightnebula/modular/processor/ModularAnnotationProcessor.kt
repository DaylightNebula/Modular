package daylightnebula.modular.processor

import daylightnebula.modular.annotations.FunctionInfo
import daylightnebula.modular.annotations.FunctionType
import daylightnebula.modular.annotations.ListenerFileConfig
import daylightnebula.modular.annotations.ModularAnnotation
import kotlinx.serialization.json.Json
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@SupportedAnnotationTypes("daylightnebula.modular.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_24)
class ModularAnnotationProcessor: AbstractProcessor() {
    companion object {
        val json = Json {
            prettyPrint = true
        }
    }

    override fun process(
        annotations: Set<TypeElement>,
        environment: RoundEnvironment
    ): Boolean {
        // get our annotations
        val elements = environment.getElementsAnnotatedWith(ModularAnnotation::class.java)
            .mapNotNull { it as? TypeElement }

        // fill write queue via annotation options and elements annotated with our annotation options
        val writeQueue = mutableMapOf<String, ListenerFileConfig>()
        for (option in elements) {
            for (element in environment.getElementsAnnotatedWith(option)) {
                val element = element as? ExecutableElement ?: continue
                val parentClass = element.enclosingElement as? TypeElement ?: continue

                val fileQueue = writeQueue
                    .computeIfAbsent(parentClass.qualifiedName.toString()) { ListenerFileConfig() }
                fileQueue.append(
                    option.qualifiedName.toString(),
                    analyzeFunctionType(element)
                )
            }
        }

        // empty write queue
        writeQueue.forEach { (fileName, config) ->
            val path = "META-INF/listeners/$fileName"

            try {
                processingEnv.filer
                    .createResource(StandardLocation.CLASS_OUTPUT, "", path)
                    .openWriter()
                    .use { writer ->
                        writer.write(json.encodeToString(config))
                    }

                processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "Generated listener config at: $path")
            } catch (ex: Exception) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Failed to create resource at $path: ${ex.message}")
            }
        }

        return true
    }

    private fun analyzeFunctionType(method: ExecutableElement): FunctionInfo {
        val methodName = method.simpleName.toString()
        val enclosingElement = method.enclosingElement
        val methodModifiers = method.modifiers

        return when (enclosingElement.kind) {
            ElementKind.CLASS -> {
                val classElement = enclosingElement as TypeElement
                val className = classElement.qualifiedName.toString()
                val classModifiers = classElement.modifiers

                return when {
                    // Check for Kotlin object (has FINAL and no constructors typically)
                    isKotlinObject(classElement) -> {
                        FunctionInfo(
                            functionType = FunctionType.KOTLIN_OBJECT,
                            className = className,
                            methodName = methodName,
                            fullPath = "$className.$methodName",
                            isValid = true,
                            validationMessage = "Kotlin object method - can be called directly"
                        )
                    }

                    // Check for Kotlin companion object
                    isKotlinCompanionObject(classElement) -> {
                        val parentClass = getParentClassName(classElement)
                        FunctionInfo(
                            functionType = FunctionType.KOTLIN_COMPANION_OBJECT,
                            className = parentClass ?: className,
                            methodName = methodName,
                            fullPath = "${parentClass ?: className}.$methodName",
                            isValid = true,
                            validationMessage = "Kotlin companion object method"
                        )
                    }

                    // Java static method
                    methodModifiers.contains(Modifier.STATIC) -> {
                        FunctionInfo(
                            functionType = FunctionType.JAVA_STATIC,
                            className = className,
                            methodName = methodName,
                            fullPath = "$className.$methodName",
                            isValid = true,
                            validationMessage = "Java static method"
                        )
                    }

                    // Instance method (not supported)
                    else -> {
                        FunctionInfo(
                            functionType = FunctionType.INSTANCE_METHOD,
                            className = className,
                            methodName = methodName,
                            fullPath = "$className.$methodName",
                            isValid = false,
                            validationMessage = "Instance methods are not supported - use static, companion object, or object methods"
                        )
                    }
                }
            }

            else -> {
                FunctionInfo(
                    functionType = FunctionType.INSTANCE_METHOD,
                    className = "Unknown",
                    methodName = methodName,
                    fullPath = "Unknown.$methodName",
                    isValid = false,
                    validationMessage = "Unknown enclosing element type: ${enclosingElement.kind}"
                )
            }
        }
    }

    private fun isKotlinObject(classElement: TypeElement): Boolean {
        // ignore companions
        val className = classElement.simpleName.toString()
        if (className == "Companion") return false

        // check if the class has no public constructors and final
        val modifiers = classElement.modifiers
        val isFinal = modifiers.contains(Modifier.FINAL)
        val hasNoPublicConstructors = classElement.enclosedElements
            .filterIsInstance<ExecutableElement>()
            .filter { it.kind == ElementKind.CONSTRUCTOR }
            .none { it.modifiers.contains(Modifier.PUBLIC) }

        // make sure INSTANCE field exists
        val hasInstanceField = classElement.enclosedElements
            .filterIsInstance<VariableElement>()
            .any { it.simpleName.toString() == "INSTANCE" &&
                    it.modifiers.contains(Modifier.STATIC) &&
                    it.modifiers.contains(Modifier.FINAL) }

        // if all these are true, then this is a Kotlin object
        return isFinal && (hasNoPublicConstructors || hasInstanceField)
    }

    private fun isKotlinCompanionObject(classElement: TypeElement): Boolean {
        val className = classElement.simpleName.toString()

        // Kotlin companion objects are typically named "Companion"
        // and are nested inside another class
        return className == "Companion" &&
                classElement.enclosingElement.kind == ElementKind.CLASS
    }

    private fun getParentClassName(companionClass: TypeElement): String? {
        val parent = companionClass.enclosingElement
        return if (parent is TypeElement) {
            parent.qualifiedName.toString()
        } else null
    }
}
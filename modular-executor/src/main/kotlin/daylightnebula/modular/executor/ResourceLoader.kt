package daylightnebula.modular.executor

import daylightnebula.modular.annotations.ListenerFileConfig
import kotlinx.serialization.json.Json
import java.io.File
import java.util.jar.JarFile

object ResourceLoader {
    val json = Json {
        prettyPrint = true
    }

    lateinit var paths: List<String>
    val regex = Regex("META-INF/listeners/.*")
    val configFiles = mutableMapOf<String, ListenerFileConfig>()

    fun init(paths: List<String>) { this.paths = paths; init() }

    fun reload() { configFiles.clear(); init() }

    private fun init() {
        paths.forEach { classpath ->
            // if jar file, load as jar file
            if (classpath.endsWith(".jar")) {
                JarFile(classpath).use { jar ->
                    for (entry in jar.entries()) {
                        if (!entry.name.matches(regex)) continue
                        val fullName = entry.name

                        jar.getInputStream(entry).use { stream ->
                            stream.reader().use { reader ->
                                val text = reader.readText()
                                try {
                                    val config = json.decodeFromString<ListenerFileConfig>(text)
                                    configFiles[fullName] = config
                                } catch (ex: Exception) {}
                            }
                        }
                    }
                }
            }
            // otherwise, check root for files
            else {
                val file = File(classpath)
                file.listFiles()?.forEach { file ->
                    walkFileTree(file) { path, file ->
                        if (!path.matches(regex)) return@walkFileTree

                        val text = file.readText()
                        val config = json.decodeFromString<ListenerFileConfig>(text)
                        configFiles[path] = config
                    }
                }
            }
        }
    }

    private fun walkFileTree(from: File, parentPath: String = "", callback: (String, File) -> Unit) {
        val path = if (parentPath.isEmpty()) from.name else "$parentPath/${from.name}"
        if (from.isDirectory) from.listFiles()?.forEach { walkFileTree(it, path, callback) }
        else callback(path, from)
    }

    fun getConfigs(): Map<String, ListenerFileConfig> = configFiles
}
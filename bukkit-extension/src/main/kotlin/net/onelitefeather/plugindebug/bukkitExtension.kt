@file:JvmName("BukkitExtension")
package net.onelitefeather.plugindebug

import io.papermc.lib.PaperLib
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.readLines
import kotlin.io.path.writeLines
import kotlin.io.path.writeText

fun DebugBuilder.enableLatestLogSpigot(): DebugBuilder {
    val latestLogFile = Path.of("logs", "latest.log")
    val tempPrefixField = DebugBuilder::class.java.getDeclaredField("tempPrefix")
    tempPrefixField.isAccessible = true
    val maxPartFileSizeField = DebugBuilder::class.java.getDeclaredField("maxPartFileSize")
    maxPartFileSizeField.isAccessible = true
    val fileToUploadExtension = DebugBuilder::class.java.getDeclaredField("fileToUpload")
    fileToUploadExtension.isAccessible = true
    if (latestLogFile.fileSize() >= maxPartFileSizeField.getLong(this))
        throw IllegalStateException("Latest log is to big only {} bytes allowed".format( maxPartFileSizeField.getLong(this)))
    val logFile = Files.createTempFile(tempPrefixField.get(this) as String, ".log")
    logFile.writeLines(latestLogFile.readLines().map { it.replace(PRIVACY_REGEX, "*") })

    (fileToUploadExtension.get(this) as MutableList<DebugFile>).add(DebugFile(logFile, FileType.LOG, "Latest Log"))
    return this
}

fun DebugBuilder.defaultPaperDebugInformation(): DebugBuilder {
    val tempPrefixField = DebugBuilder::class.java.getDeclaredField("tempPrefix")
    tempPrefixField.isAccessible = true
    val fileToUploadExtension = DebugBuilder::class.java.getDeclaredField("fileToUpload")
    fileToUploadExtension.isAccessible = true
    val debugYaml = Files.createTempFile(tempPrefixField.get(this) as String, ".yaml")
    val plugins = Bukkit.getServer().pluginManager.plugins.toMutableList()
    plugins.sortWith(Comparator.comparing { obj -> obj.name })

    val debugInformation = YamlConfiguration()
    debugInformation.set("debugVersion", 1)
    debugInformation.set("server.version", Bukkit.getVersion())
    debugInformation.set("server.plugins", plugins.count())
    plugins.forEach {
        debugInformation.set("server.plugin.${it.name}.version", it.description.version)
        debugInformation.set("server.plugin.${it.name}.enabled", it.isEnabled)
        debugInformation.set("server.plugin.${it.name}.main", it.description.main)
        debugInformation.set("server.plugin.${it.name}.authors", it.description.authors)
        debugInformation.set("server.plugin.${it.name}.load-before", it.description.loadBefore)
        debugInformation.set("server.plugin.${it.name}.dependencies", it.description.depend)
        debugInformation.set("server.plugin.${it.name}.soft-dependencies", it.description.softDepend)
        debugInformation.set("server.plugin.${it.name}.provides", it.description.provides)
    }
    if (PaperLib.isPaper()) {
        val datapacks = Bukkit.getServer().datapackManager.enabledPacks
        debugInformation.set("server.datapacks.count", datapacks.count())
        debugInformation.set("server.datapacks.packs", datapacks.map { it.name }.toList())
    }
    val runtime = Runtime.getRuntime()
    val rb = ManagementFactory.getRuntimeMXBean()
    debugInformation.set("uptime", rb.uptime)
    debugInformation.set("jvm-flags", rb.inputArguments)
    debugInformation.set("free-memory", runtime.freeMemory())
    debugInformation.set("max-memory", runtime.maxMemory())
    debugInformation.set("total-memory", runtime.totalMemory())
    debugInformation.set("available-processors", runtime.availableProcessors())
    debugInformation.set("java-name", rb.vmName)
    debugInformation.set("java-version", System.getProperty("java.version"))
    debugInformation.set("java-vendor", System.getProperty("java.vendor"))
    debugInformation.set("operating-system", System.getProperty("os.name"))
    debugInformation.set("os-version", System.getProperty("os.version"))
    debugInformation.set("os-arch", System.getProperty("os.arch"))
    debugYaml.writeText(debugInformation.saveToString())
    (fileToUploadExtension.get(this) as MutableList<DebugFile>).add(DebugFile(debugYaml, FileType.YAML, "Default Paper Debug Information"))
    return this
}
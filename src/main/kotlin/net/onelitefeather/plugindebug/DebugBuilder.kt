package net.onelitefeather.plugindebug

import com.google.gson.Gson
import com.google.gson.JsonArray
import net.lingala.zip4j.ZipFile
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.fileSize
import kotlin.io.path.readLines
import kotlin.io.path.writeLines
import kotlin.io.path.writeText
import kotlin.jvm.optionals.getOrNull

class DebugBuilder private constructor(private val uploadServer: String) {
    private val fileToUpload: MutableList<DebugFile> = mutableListOf()
    private var maxPartFileSize: Long = 2_097_152
    private var tempPrefix: String = "plugin-debug"
    private val cleanIpRegex = PRIVACY_REGEX
    private var maxZipFileSize: Long = 2_097_152
    private var userAgent = "plugin-debug"
    private val gson: Gson = Gson()
        .newBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(DebugFile::class.java, DebugFileAdapter())
        .setLenient()
        .create()

    @Throws(IllegalStateException::class)
    fun enableLatestLogSpigot(): DebugBuilder {
        val latestLogFile = Path.of("logs", "latest.log")
        if (latestLogFile.fileSize() >= maxPartFileSize)
            throw IllegalStateException("Latest log is to big only {} bytes allowed".format(maxPartFileSize))
        val logFile = Files.createTempFile(tempPrefix, ".log")
        logFile.writeLines(latestLogFile.readLines().map { it.replace(cleanIpRegex, "*") })
        fileToUpload.add(DebugFile(logFile, FileType.LOG, "Latest Log"))
        return this
    }

    fun addFile(file: DebugFile): DebugBuilder {
        fileToUpload.add(file)
        return this
    }

    fun addFile(
        filePath: Path,
        fileType: FileType,
        uiTabName: String,
    ): DebugBuilder {
        fileToUpload.add(DebugFile(filePath, fileType, uiTabName))
        return this
    }


    fun addText(text: String, uiTabName: String): DebugBuilder {
        val textFile = Files.createTempFile(tempPrefix, ".txt")
        textFile.writeText(text)
        fileToUpload.add(DebugFile(textFile, FileType.TEXT, uiTabName))
        return this
    }

    fun addJson(json: String, uiTabName: String): DebugBuilder {
        val textFile = Files.createTempFile(tempPrefix, ".json")
        textFile.writeText(json)
        fileToUpload.add(DebugFile(textFile, FileType.JSON, uiTabName))
        return this
    }

    fun addYAML(json: String, uiTabName: String): DebugBuilder {
        val textFile = Files.createTempFile(tempPrefix, ".yml")
        textFile.writeText(json)
        fileToUpload.add(DebugFile(textFile, FileType.YAML, uiTabName))
        return this
    }

    @Throws(IllegalStateException::class)
    fun upload(): DebugUploadResult {
        val tempFolder = Files.createTempDirectory(tempPrefix)
        val zipFile = tempFolder.resolve("debug.zip")
        val zipContainer = ZipFile(zipFile.toFile())
        val jsonArray = JsonArray()
        fileToUpload.forEach {
            zipContainer.addFile(it.filePath.toFile())
            jsonArray.add(gson.toJsonTree(it))
        }
        val debugJson = tempFolder.resolve("debug.json")
        debugJson.writeText(gson.toJson(jsonArray))
        zipContainer.addFile(debugJson.toFile())
        if (zipFile.fileSize() >= maxZipFileSize)
            throw IllegalStateException("Zip file is to big only {} bytes allowed".format(maxZipFileSize))
        val client = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$uploadServer/post"))
            .timeout(Duration.ofMinutes(2))
            .header("User-Agent", userAgent)
            .header("Content-Type", "application/zip").POST(HttpRequest.BodyPublishers.ofFile(zipFile)).build()
        val response: HttpResponse<String> = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join()
        val code = response.headers().firstValue("Location").getOrNull()
                ?: throw IllegalStateException("Key not returned")
        return DebugUploadResult(
            code,
            uploadServer
        )
    }

    companion object {
        @JvmStatic
        fun builder(uploadServer: String): DebugBuilder = DebugBuilder(uploadServer)
    }
}
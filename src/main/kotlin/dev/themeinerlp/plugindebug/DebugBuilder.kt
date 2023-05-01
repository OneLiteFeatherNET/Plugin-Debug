package dev.themeinerlp.plugindebug

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
import kotlin.io.path.writeText
import kotlin.jvm.optionals.getOrNull

/**
 * Simple builder to collect debug files and upload them to a custom bytebin server
 * Ex:
 * <pre class="prettyprint">
 *     DebugBuilder.builder("https://bytebin.lucko.me/")...
 * </pre>
 */
class DebugBuilder private constructor(private val uploadServer: String) {
    private val fileToUpload: MutableList<DebugFile> = mutableListOf()
    private var maxPartFileSize: Long = 2_097_152
    private var tempPrefix: String = "plugin-debug"
    private var maxZipFileSize: Long = 2_097_152
    private var userAgent = "plugin-debug"
    private val gson: Gson = Gson()
        .newBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(DebugFile::class.java, DebugFileAdapter())
        .setLenient()
        .create()

    /**
     * Add a debug file RAW into the debug zip
     */
    fun addFile(file: DebugFile): DebugBuilder {
        fileToUpload.add(file)
        return this
    }
    /**
     * Add a debug file RAW into the debug zip
     * @param filePath to collect into the zip
     * @param fileType to display in the frontend
     * @param uiTabName to display tn the frontend
     */
    fun addFile(
        filePath: Path,
        fileType: FileType,
        uiTabName: String,
    ): DebugBuilder {
        fileToUpload.add(DebugFile(filePath, fileType, uiTabName))
        return this
    }

    /**
     * Add a text as a file into the debug zip
     * @param text to collect into the zip
     * @param uiTabName to display tn the frontend
     */
    fun addText(text: String, uiTabName: String): DebugBuilder {
        val textFile = Files.createTempFile(tempPrefix, ".txt")
        textFile.writeText(text)
        fileToUpload.add(DebugFile(textFile, FileType.TEXT, uiTabName))
        return this
    }
    /**
     * Add a json string as a file into the debug zip
     * @param json to collect into the zip
     * @param uiTabName to display tn the frontend
     */
    fun addJson(json: String, uiTabName: String): DebugBuilder {
        val textFile = Files.createTempFile(tempPrefix, ".json")
        textFile.writeText(json)
        fileToUpload.add(DebugFile(textFile, FileType.JSON, uiTabName))
        return this
    }
    /**
     * Add a yaml string as a file into the debug zip
     * @param yaml to collect into the zip
     * @param uiTabName to display tn the frontend
     */
    fun addYAML(yaml: String, uiTabName: String): DebugBuilder {
        val textFile = Files.createTempFile(tempPrefix, ".yml")
        textFile.writeText(yaml)
        fileToUpload.add(DebugFile(textFile, FileType.YAML, uiTabName))
        return this
    }
    /**
     * Upload all collected files in a zip with a debug.json to a custom bytebin server.
     * @throws IllegalStateException when the zip file is bigger than 2mb(binary)
     * @throws IllegalStateException when no code was returned
     * @return an object with the given server and the uploaded code
     */
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
        /**
         * Creates a builder instance with the given bytebin server
         * @param uploadServer bytebin server base url
         */
        @JvmStatic
        fun builder(uploadServer: String): DebugBuilder = DebugBuilder(uploadServer)
    }
}
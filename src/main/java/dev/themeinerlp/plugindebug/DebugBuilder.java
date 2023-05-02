package dev.themeinerlp.plugindebug;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.lingala.zip4j.ZipFile;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DebugBuilder<T extends DebugBuilder<T>> {
    private final String uploadServer;
    protected final List<DebugFile> fileToUpload = new ArrayList<>();
    protected final String tempFile = "plugin-debug";

    protected Gson gson = new Gson()
            .newBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(DebugFile.class, new DebugFileAdapter())
            .setLenient()
            .create();
    private final String userAgent = "plugin-debug";
    protected final long maxZipFileSize = 2_097_152;

    protected DebugBuilder(String uploadServer) {
        this.uploadServer = uploadServer;
    }

    /**
     * Add a debug file RAW into the debug zip
     */
    public T addFile(DebugFile file) {
        fileToUpload.add(file);
        return (T) this;
    }

    /**
     * Add a debug file RAW into the debug zip
     *
     * @param filePath  to collect into the zip
     * @param fileType  to display in the frontend
     * @param uiTabName to display tn the frontend
     */
    public T addFile(Path filePath, FileType fileType, String uiTabName) {
        fileToUpload.add(new DebugFile(filePath, fileType, uiTabName));
        return (T) this;
    }

    /**
     * Add a text as a file into the debug zip
     *
     * @param text      to collect into the zip
     * @param uiTabName to display tn the frontend
     */
    public T addText(String text, String uiTabName) throws IOException {
        var textFile = Files.createTempFile(tempFile, ".txt");
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(textFile))) {
            writer.append(text);
        }
        addFile(textFile, FileType.TEXT, uiTabName);
        return (T) this;
    }

    /**
     * Add a json string as a file into the debug zip
     *
     * @param json      to collect into the zip
     * @param uiTabName to display tn the frontend
     */
    public T addJson(String json, String uiTabName) throws IOException {
        var textFile = Files.createTempFile(tempFile, ".json");
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(textFile))) {
            writer.append(json);
        }
        addFile(textFile, FileType.JSON, uiTabName);
        return (T) this;
    }

    /**
     * Add a json string as a file into the debug zip
     *
     * @param yaml      to collect into the zip
     * @param uiTabName to display tn the frontend
     */
    public T addYAML(String yaml, String uiTabName) throws IOException {
        var textFile = Files.createTempFile(tempFile, ".json");
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(textFile))) {
            writer.append(yaml);
        }
        addFile(textFile, FileType.YAML, uiTabName);
        return (T) this;
    }

    public DebugUploadResult upload() throws IOException {
        var tempFolder = Files.createTempDirectory(tempFile);
        var zipFile = tempFolder.resolve("debug.zip");
        try (var zipContainer = new ZipFile(zipFile.toFile())) {
            var jsonArray = new JsonArray();
            for (DebugFile debugFile : fileToUpload) {
                zipContainer.addFile(debugFile.filePath().toFile());
                jsonArray.add(gson.toJsonTree(debugFile));
            }
            var debugJson = tempFolder.resolve("debug.json");
            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(debugJson))) {
                writer.append(gson.toJson(jsonArray));
            }
            zipContainer.addFile(debugJson.toFile());
            if (Files.size(zipFile) >= maxZipFileSize) throw new IllegalStateException(
                    String.format("Zip file is to big only %d bytes allowed", maxZipFileSize)
            );
        }
        var client = HttpClient.newBuilder().build();
        var request = HttpRequest
                .newBuilder()
                .uri(URI.create(String.format("%s/post", this.uploadServer)))
                .timeout(Duration.ofMinutes(2))
                .header("User-Agent", this.userAgent)
                .POST(HttpRequest.BodyPublishers.ofFile(zipFile)).build();
        var response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).join();
        var code = response.headers().firstValue("Location").orElseThrow();
        return new DebugUploadResult(code, this.uploadServer);

    }

    /**
     * Creates a builder instance with the given bytebin server
     *
     * @param uploadServer bytebin server base url
     */
    public static DebugBuilder<?> builder(String uploadServer) {
        return new DebugBuilder<>(uploadServer);
    }
}

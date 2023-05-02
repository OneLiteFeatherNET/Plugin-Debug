package dev.themeinerlp.plugindebug;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;

final class DebugFileAdapter extends TypeAdapter<DebugFile> {
    @Override
    public void write(JsonWriter out, DebugFile value) throws IOException {
        out.beginObject()
                .name("filePath").value(value.filePath().getFileName().toString())
                .name("fileType").value(value.fileType().name())
                .name("uiTabName").value(value.uiTabName())
                .endObject();
    }

    @Override
    public DebugFile read(JsonReader in) throws IOException {
        in.beginObject();
        var filePath = in.nextString();
        var fileType = FileType.valueOf(in.nextString());
        var uiTabName = in.nextString();
        return new DebugFile(Path.of(filePath), fileType, uiTabName);
    }
}

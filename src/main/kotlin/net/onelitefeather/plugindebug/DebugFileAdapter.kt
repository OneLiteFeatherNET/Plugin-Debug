package net.onelitefeather.plugindebug

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.nio.file.Paths
import kotlin.io.path.name

internal class DebugFileAdapter : TypeAdapter<DebugFile>() {
    override fun write(out: JsonWriter, value: DebugFile) {
        out
            .beginObject()
            .name("filePath").value(value.filePath.name)
            .name("fileType").value(value.fileType.name)
            .name("uiTabName").value(value.uiTabName)
            .endObject()
    }

    override fun read(`in`: JsonReader): DebugFile {
        `in`.beginObject()
        val filePath = `in`.nextString()
        val fileType = FileType.valueOf(`in`.nextString())
        val uiTabName = `in`.nextString()
        `in`.endObject()
        return DebugFile(Paths.get(filePath), fileType, uiTabName)
    }
}
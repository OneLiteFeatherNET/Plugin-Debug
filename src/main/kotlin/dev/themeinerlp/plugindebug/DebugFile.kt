package dev.themeinerlp.plugindebug

import java.nio.file.Path

class DebugFile(
    val filePath: Path,
    val fileType: FileType,
    val uiTabName: String
)
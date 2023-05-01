package dev.themeinerlp.plugindebug

import java.nio.file.Path

/**
 * Just a simple placeholder to referenced to the file and there to displaying file type and also the menu tab on den ui
 */
class DebugFile(
    val filePath: Path,
    val fileType: FileType,
    val uiTabName: String
)
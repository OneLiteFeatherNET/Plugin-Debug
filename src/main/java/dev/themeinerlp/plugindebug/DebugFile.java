package dev.themeinerlp.plugindebug;

import java.nio.file.Path;

public record DebugFile(Path filePath, FileType fileType, String uiTabName) {
}

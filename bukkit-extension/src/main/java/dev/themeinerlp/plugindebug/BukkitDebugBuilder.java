package dev.themeinerlp.plugindebug;

import io.papermc.lib.PaperLib;
import io.papermc.paper.datapack.Datapack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Bukkit Debug builder
 */
public final class BukkitDebugBuilder extends DebugBuilder<BukkitDebugBuilder> {

    private final Pattern privacyRegex = Pattern.compile("\\b(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\b");
    private BukkitDebugBuilder(String uploadServer) {
        super(uploadServer);
    }

    /**
     * Collects the latest log file from bukkit
     * @return the builder
     * @throws IOException if the file empty/null
     */
    public BukkitDebugBuilder collectLatestSpigotLog() throws IOException {
        var latestLogFile = Path.of("logs", "latest.log");
        if (Files.size(latestLogFile) >= maxZipFileSize) throw new IllegalStateException(
                String.format("Latest log file is to big only %d bytes allowed", maxZipFileSize)
        );
        var tempLogFile = Files.createTempFile(tempFile, ".log");
        var cleanedList = Files.readAllLines(latestLogFile).stream().map(s -> s.replaceAll(privacyRegex.pattern(), "*")).toList();
        Files.write(tempLogFile, cleanedList);
        addFile(tempLogFile, FileType.LOG, "Latest Log");
        return this;
    }

    /**
     * Collects default paper debug information
     * @return the bukkit builder
     * @throws IOException if the file empty/null
     */

    public BukkitDebugBuilder defaultPaperDebugInformation() throws IOException {
        var tempLogFile = Files.createTempFile(tempFile, ".yaml");
        var plugins = Arrays.asList(Bukkit.getServer().getPluginManager().getPlugins());
        plugins.sort(this::sortPluginByName);
        var debugInformation = new YamlConfiguration();
        debugInformation.set("server.version", Bukkit.getVersion());
        debugInformation.set("server.plugins", plugins.size());
        for (Plugin plugin : plugins) {
            var name = plugin.getName();
            debugInformation.set(String.format("server.plugin.%s.version", name), plugin.getDescription().getVersion());
            debugInformation.set(String.format("server.plugin.%s.main", name), plugin.getDescription().getMain());
            debugInformation.set(String.format("server.plugin.%s.authors", name), plugin.getDescription().getAuthors());
            debugInformation.set(String.format("server.plugin.%s.load-before", name), plugin.getDescription().getLoadBefore());
            debugInformation.set(String.format("server.plugin.%s.dependencies", name), plugin.getDescription().getDepend());
            debugInformation.set(String.format("server.plugin.%s.soft-dependencies", name), plugin.getDescription().getSoftDepend());
            debugInformation.set(String.format("server.plugin.%s.provides", name), plugin.getDescription().getProvides());
            debugInformation.set(String.format("server.plugin.%s.enabled", name), plugin.isEnabled());
        }
        if (PaperLib.isPaper()) {
            var dataPacks = Bukkit.getServer().getDatapackManager().getEnabledPacks();
            debugInformation.set("server.datapacks.count", dataPacks.size());
            debugInformation.set("server.datapacks.packs", dataPacks.stream().map(Datapack::getName).toList());
        }
        var runtime = Runtime.getRuntime();
        var rb = ManagementFactory.getRuntimeMXBean();
        debugInformation.set("uptime", rb.getUptime());
        debugInformation.set("jvm-flags", rb.getInputArguments());
        debugInformation.set("free-memory", runtime.freeMemory());
        debugInformation.set("max-memory", runtime.maxMemory());
        debugInformation.set("total-memory", runtime.totalMemory());
        debugInformation.set("available-processors", runtime.availableProcessors());
        debugInformation.set("java-name", rb.getVmName());
        debugInformation.set("java-version", System.getProperty("java.version"));
        debugInformation.set("java-vendor", System.getProperty("java.vendor"));
        debugInformation.set("operating-system", System.getProperty("os.name"));
        debugInformation.set("os-version", System.getProperty("os.version"));
        debugInformation.set("os-arch", System.getProperty("os.arch"));
        addYAML(debugInformation.saveToString(), "Default Paper Debug Information");
        return this;
    }

    private int sortPluginByName(@NotNull Plugin A, @NotNull Plugin B) {
        return A.getName().compareTo(B.getName());
    }

    /**
     * Creates a bukkit builder instance with the given bytebin server
     *
     * @param uploadServer bytebin server base url
     */
    public static BukkitDebugBuilder builder(String uploadServer) {
        return new BukkitDebugBuilder(uploadServer);
    }

}

package dev.themeinerlp.plugindebug.example;

import dev.themeinerlp.plugindebug.BukkitDebugBuilder;
import dev.themeinerlp.plugindebug.DebugUploadResult;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ExampleUploadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("debugpaste")) {
            try {
                DebugUploadResult result = BukkitDebugBuilder.builder("https://bytebin.lucko.me").defaultPaperDebugInformation().collectLatestSpigotLog().upload();
                var byteBinServer = URLEncoder.encode(result.uploadServer(), StandardCharsets.UTF_8);
                // https://debugpaste.onelitefeather.net is our free hosted UI
                var openUrl = String.format("https://debugpaste.onelitefeather.net/#/%s/%s/", result.code(), byteBinServer);
                sender.sendMessage(
                        MiniMessage.miniMessage()
                                .deserialize("<#05b9ff>[Example] <yellow><click:OPEN_URL:'$openUrl'>Debug paste: <url></click>",  Placeholder.parsed("url", openUrl))
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return true;
    }
}

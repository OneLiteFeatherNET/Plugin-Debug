# Plugin Debug

[![javadoc](https://javadoc.io/badge2/dev.themeinerlp/plugin-debug/javadoc.svg)](https://javadoc.io/doc/dev.themeinerlp/plugin-debug)
![GitHub issues](https://img.shields.io/github/issues/OneLiteFeatherNET/Plugin-Debug)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/OneLiteFeatherNET/Plugin-Debug)

## Description
Plugin Debug is a simple and modern library for collect some debug information of your plugin.  
This library is inspired by Debug Paste of @IntellectualSites

## Motivation
We maintain now more than one plugin/software plugin in java. Now it's time to build an api/library to help us to provide better support for users there use our plugins. 

## Focus
Our focus lies within a basic implementation of a generic api for collect server information and upload to a (custom) [bytebin](https://github.com/lucko/bytebin) server .

## More information / external links

Discord: https://discord.onelitefeather.de

## Get started
<details>
<summary><strong>Groovy</strong></summary>

```groovy
dependencies {
    // Core
    implementation 'dev.themeinerlp:plugin-debug:1.0.0'
    // Bukkit Extension
    implementation 'dev.themeinerlp.plugin-debug:bukkit-extension:1.0.0'
}
```
</details>
<details>
<summary><strong>Gradle</strong></summary>

```kt
dependencies {
    // Core
    implementation("dev.themeinerlp:plugin-debug:1.0.0")
    // Bukkit Extension
    implementation("dev.themeinerlp.plugin-debug:bukkit-extension:1.0.0")
}
```
</details>

Example code(Kotlin):
```kt
// Example json object
val obj = JsonObject()
obj.addProperty("Test", "Test")

val result =
    DebugBuilder.builder(BYTEBIN_BASE_URL)
        // For this is the bukkit extension required 
        .enableLatestLogSpigot() // Adds the paper last log
        .defaultPaperDebugInformation() // Adds as a parseable yaml format some system relevant information from bukkit
        // Adds a file from path wrapped in a placeholder object
        .addFile(DebugFile(attollo.dataFolder.toPath().resolve("config.yml"),FileType.YAML,"Config as file object"))
        // Adds a file from path
        .addFile(attollo.dataFolder.toPath().resolve("config.yml"),FileType.YAML,"Config as file")
        // Add yaml formatted string to the debug
        .addYAML(attollo.config.saveToString(), "Config")
        // Add a simple text to the debug
        .addText("Text test", "Text test")
        // Add json formatted string to the debug
        .addJson(Gson().toJson(obj), "Json test")
        // Upload to a bytebin server
        .upload()
// Encodes the URL as string
val encodedUrl = URLEncoder.encode(
    result.uploadServer,
    StandardCharsets.UTF_8
)
// ByteBin Code from Server
val code = result.code
// Prettified url 
val openUrl = "https://debugpaste.onelitefeather.net/#/$code/$encodedUrl/"
val component = MiniMessage.miniMessage().deserialize("<#05b9ff>[Attollo] <yellow><click:OPEN_URL:'$openUrl'>Click <u>here</u> to open the debug paste</click>")
```
**For better formatting we used mini message also the two lines `enableLatestLogSpigot()` and `defaultPaperDebugInformation()` requires the Bukkit Extension.**

Example for java 
```java
var obj = new JsonObject();
obj.addProperty("Test", "Test");

var builder = DebugBuilder.builder(BYTEBIN_BASE_URL);
builder = enableLatestLogSpigot(builder);
builder = defaultPaperDebugInformation(builder);
builder.addFile(new DebugFile(Path.of(""), FileType.YAML, "Config as file object"))
        .addFile(Path.of(""), FileType.YAML, "Config as file");
var result = builder.upload();
```

# Plugin Debug

[![javadoc](https://javadoc.io/badge2/dev.themeinerlp/plugin-debug/javadoc.svg)](https://javadoc.io/doc/dev.themeinerlp/plugin-debug)
![GitHub issues](https://img.shields.io/github/issues/OneLiteFeatherNET/Plugin-Debug)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/OneLiteFeatherNET/Plugin-Debug)

## Description
Plugin Debug is a simple and modern library for collect some debug information of your plugin.  
This library is inspired by Debug Paste of @IntellectualSites [IntellectualSites](https://github.com/IntellectualSites)  
As a backend we use from lucko [bytebin](https://github.com/lucko/bytebin) service to host/upload the debug files

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
    implementation("dev.themeinerlp:plugin-debug:1.1.0")
    // Bukkit Extension
    implementation("dev.themeinerlp.plugin-debug:bukkit-extension:1.1.0")
}
```
</details>

### Example

We have an example [gradle module](example) with an example bukkit command to retrieve a debug log.
Also, we provide already a pre-hosted web ui for view your logs.

The hosted ui follows this structure:
```
https://debugpaste.onelitefeather.net/#/BYTEBIN_CODE/ENCODED_URL/
```
* BYTEBIN_CODE
  * The returned code from ByteBin Server when the upload is successfully
* ENCODED_URL
  * The ByteBin Server URL encoded in a friendly style

---
### Example code(Kotlin):
```kt
// Example json object from GSON LIB
val obj = JsonObject()
obj.addProperty("Test", "Test")

val result =
    DebugBuilder.builder(BYTEBIN_BASE_URL)
        // Adds a file from path wrapped in a placeholder object
        .addFile(DebugFile(Path.of("config", "config.yml"), FileType.YAML, "Config as file object"))
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
val component = MiniMessage.miniMessage().deserialize("<#05b9ff>[Example] <yellow><click:OPEN_URL:'$openUrl'>Click <u>here</u> to open the debug paste</click>")
```

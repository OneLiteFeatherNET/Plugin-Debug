package net.onelitefeather.plugindebug

internal val PRIVACY_REGEX =
    Regex(pattern = "\\b(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.(1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\b")
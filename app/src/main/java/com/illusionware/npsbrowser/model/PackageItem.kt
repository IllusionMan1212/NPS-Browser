package com.illusionware.npsbrowser.model

data class PackageItem(
    val titleId: String,
    val region: String,
    val name: String,
    val pkgUrl: String,
    val contentId: String,
    val modificationDate: String,
    val pkgSize: String,
    val sha256: String,
    val zRif: String,
    val rap: String,
    val minFW: String,

    val consoleType: ConsoleType,
    val dataType: PackageItemType,
)
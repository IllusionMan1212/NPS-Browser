package com.illusionware.npsbrowser

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter

data class AppData(
    val titleID: String?, val region: String?, val title: String?, val link: String?,
    val license: String?, val contentID: String?, val lastDateTime: String?,
    val fileSize: Number?, val sha256: String?, val minFW: String?
) : SortedListAdapter.ViewModel {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppData

        if (titleID != other.titleID) return false
        if (region != other.region) return false
        if (title != other.title) return false
        if (link != other.link) return false
        if (license != other.license) return false
        if (contentID != other.contentID) return false
        if (lastDateTime != other.lastDateTime) return false
        if (fileSize != other.fileSize) return false
        if (sha256 != other.sha256) return false
        if (minFW != other.minFW) return false

        return true
    }

    override fun hashCode(): Int {
        var result = titleID?.hashCode() ?: 0
        result = 31 * result + (region?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (link?.hashCode() ?: 0)
        result = 31 * result + (license?.hashCode() ?: 0)
        result = 31 * result + (contentID?.hashCode() ?: 0)
        result = 31 * result + (lastDateTime?.hashCode() ?: 0)
        result = 31 * result + (fileSize?.hashCode() ?: 0)
        result = 31 * result + (sha256?.hashCode() ?: 0)
        result = 31 * result + (minFW?.hashCode() ?: 0)
        return result
    }

    override fun <T : Any?> isSameModelAs(model: T): Boolean {
        if (model is AppData) {
            val other: AppData = model as AppData
            return "${other.titleID} ${other.title}" == "$titleID $title"
        }
        return false
    }

    override fun <T : Any?> isContentTheSameAs(model: T): Boolean {
        if (model is AppData) {
            val other: AppData = model as AppData
            return title?.equals(other.title) ?: (other.title == null)
        }
        return false
    }
}
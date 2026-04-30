package com.illusionware.npsbrowser.data.network

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}
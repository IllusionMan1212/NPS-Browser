package com.illusionware.npsbrowser.util.system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import okio.BufferedSink
import okio.BufferedSource

/**
 * Saves the given source to a file and closes it. Directories will be created if needed.
 *
 * @param file the file where the source is copied.
 */
//fun BufferedSource.saveTo(file: File) {
//    try {
//        // Create parent dirs if needed
//        file.parentFile?.mkdirs()
//
//        // Copy to destination
//        saveTo(file.outputStream())
//    } catch (e: Exception) {
//        close()
//        file.delete()
//        throw e
//    }
//}

fun BufferedSource.saveTo(sink: BufferedSink, totalBytes: Long, bytesRead: Long, scope: CoroutineScope): Boolean {
    var downloaded = bytesRead

    use { input ->
        sink.use { out ->
            while (true) {
                scope.ensureActive()
                val read = input.read(out.buffer, DEFAULT_BUFFER_SIZE.toLong())
                if (read == -1L) break
                downloaded += read
                out.emitCompleteSegments()
            }
            out.flush()
        }
    }

    if (totalBytes <= 0L) return true
    val expected = bytesRead + totalBytes
    return downloaded >= expected
}

/**
 * Saves the given source to an output stream and closes both resources.
 *
 * @param stream the stream where the source is copied.
 */
//fun BufferedSource.saveTo(stream: OutputStream, scope: CoroutineScope) {
//    use { input ->
//        stream.sink().buffer().use {
//            if (scope.isActive) {
//                it.writeAll(input)
//                it.flush()
//            }
//        }
//    }
//}

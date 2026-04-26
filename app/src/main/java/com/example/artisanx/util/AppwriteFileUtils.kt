package com.example.artisanx.util

import android.content.Context
import android.net.Uri
import com.example.artisanx.BuildConfig
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import java.io.File

object AppwriteFileUtils {

    fun fileViewUrl(fileId: String, bucketId: String = Constants.BUCKET_ARTISANSX_FILES): String =
        "${BuildConfig.APPWRITE_ENDPOINT}/storage/buckets/$bucketId/files/$fileId/view" +
                "?project=${BuildConfig.APPWRITE_PROJECT_ID}"

    suspend fun uploadFromUri(
        context: Context,
        storage: Storage,
        uri: Uri,
        prefix: String
    ): String? {
        return try {
            val cacheFile = copyUriToCache(context, uri, prefix)
            val result = storage.createFile(
                bucketId = Constants.BUCKET_ARTISANSX_FILES,
                fileId = ID.unique(),
                file = InputFile.fromPath(cacheFile.absolutePath)
            )
            cacheFile.delete()
            result.id
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun copyUriToCache(context: Context, uri: Uri, prefix: String): File {
        val ext = context.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
        val cacheFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.$ext")
        context.contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        }
        return cacheFile
    }
}

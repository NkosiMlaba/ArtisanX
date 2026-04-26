package com.example.artisanx.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.artisanx.BuildConfig
import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.Role
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import java.io.File

object AppwriteFileUtils {

    private const val TAG = "ArtisanXUpload"

    fun fileViewUrl(fileId: String, bucketId: String = Constants.BUCKET_ARTISANSX_FILES): String =
        "${BuildConfig.APPWRITE_ENDPOINT}/storage/buckets/$bucketId/files/$fileId/view" +
                "?project=${BuildConfig.APPWRITE_PROJECT_ID}"

    /**
     * Uploads a file to Appwrite Storage and returns the file ID, or null on failure.
     * Pass [userId] so the file gets owner-write + all-users-read permissions.
     * Without permissions, Appwrite's File Security rejects the upload with 401.
     */
    suspend fun uploadFromUri(
        context: Context,
        storage: Storage,
        uri: Uri,
        prefix: String,
        userId: String = ""
    ): String? {
        return try {
            val cacheFile = copyUriToCache(context, uri, prefix)
            Log.d(TAG, "Uploading $prefix: path=${cacheFile.absolutePath} size=${cacheFile.length()} bytes ext=${cacheFile.extension}")
            val writeRole = if (userId.isNotBlank()) Role.user(userId) else Role.users()
            val permissions = listOf(
                Permission.read(Role.users()),
                Permission.write(writeRole),
                Permission.delete(writeRole)
            )
            Log.d(TAG, "Permissions for $prefix: userId='$userId' count=${permissions.size}")
            val result = storage.createFile(
                bucketId = Constants.BUCKET_ARTISANSX_FILES,
                fileId = ID.unique(),
                file = InputFile.fromPath(cacheFile.absolutePath),
                permissions = permissions
            )
            cacheFile.delete()
            Log.d(TAG, "Upload success: fileId=${result.id}")
            result.id
        } catch (e: Exception) {
            Log.e(TAG, "Upload FAILED for $prefix: ${e.javaClass.simpleName}: ${e.message}", e)
            null
        }
    }

    private fun copyUriToCache(context: Context, uri: Uri, prefix: String): File {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        val ext = mimeType.substringAfterLast("/").replace("jpeg", "jpg").take(10)
        val cacheFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.$ext")
        Log.d(TAG, "Copying URI to cache: uri=$uri mimeType=$mimeType cachePath=${cacheFile.absolutePath}")
        val bytesWritten = context.contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        } ?: 0L
        Log.d(TAG, "Cache file written: ${cacheFile.length()} bytes")
        return cacheFile
    }
}

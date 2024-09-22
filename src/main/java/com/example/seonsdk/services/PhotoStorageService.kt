package com.example.seonsdk.services

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * PhotoStorageService is responsible for managing the storage of photos within the SeonSDK.
 * It handles loading, deleting, and accessing the photos directory, ensuring that photos are
 * organized and maintained within the app's private storage.
 *
 * This service does not handle the actual saving of photos; instead, it focuses on managing
 * stored files. The saving of photos is managed through CameraService, which directly handles
 * the capture and storage processes as part of the photo-taking operation, maintaining a clean
 * separation of concerns.
 *
 * @param context The application context used to initialize the photos directory.
 */
class PhotoStorageService(context: Context) {

    // Directory where photos are stored within the app's private file space.
    private val photosDirectory: File = File(context.filesDir, "photos")

    init {
        // Create the photos directory if it does not exist.
        if (!photosDirectory.exists()) {
            photosDirectory.mkdirs() // Ensure the directory is available for storing photos.
        }
    }

    /**
     * Loads all photos from the storage directory as a list of files.
     * This method returns a Flow to handle the asynchronous nature of file I/O operations.
     *
     * @return A Flow emitting a Result containing a list of files or an error.
     */
    fun loadPhotos(): Flow<Result<List<File>>> = flow {
        try {
            // List all files in the photos directory, returning an empty list if none found.
            val files = photosDirectory.listFiles()?.toList() ?: emptyList()
            emit(Result.success(files)) // Emit the successful list of photo files.
        } catch (e: Exception) {
            // Emit an error result if there is an exception during loading.
            emit(Result.failure(Error.LoadingError(e)))
        }
    }

    /**
     * Deletes a specified photo file from the storage directory.
     * This method returns a Flow to handle the deletion process asynchronously.
     *
     * @param photoFile The file object representing the photo to be deleted.
     * @return A Flow emitting a Result indicating success or failure of the deletion.
     */
    fun deletePhoto(photoFile: File): Flow<Result<Boolean>> = flow {
        try {
            when {
                // Emit an error if the file does not exist.
                !photoFile.exists() -> emit(Result.failure(Error.FileNotFound))

                // Emit an error if the file cannot be deleted, likely due to permissions.
                !photoFile.delete() -> emit(Result.failure(Error.PermissionDenied))

                // Emit success if the file was successfully deleted.
                else -> emit(Result.success(true))
            }
        } catch (e: Exception) {
            // Emit a deletion error if an exception occurs during the process.
            emit(Result.failure(Error.DeletionError(e)))
        }
    }

    /**
     * Provides the directory where photos are stored. This is used by other services to manage file paths.
     *
     * @return The directory file object where photos are stored.
     */
    fun getPhotosDirectory(): File = photosDirectory

    /**
     * Sealed class defining various error types related to file management within the service.
     * - FileNotFound: The specified file was not found.
     * - PermissionDenied: Lack of permission to delete the file.
     * - LoadingError: Errors occurring during loading of photos.
     * - DeletionError: Errors occurring during the deletion of a photo.
     * - UnknownError: A fallback error for unexpected issues.
     */
    sealed class Error(e: Exception) : Throwable(e.message) {
        // Error indicating that the specified file could not be found.
        data object FileNotFound : Error(Exception("File not found!"))

        // Error indicating permission issues while trying to delete the file.
        data object PermissionDenied : Error(Exception("Permission denied to delete the photo!"))

        // Error encapsulating issues that occur while loading photos from storage.
        data class LoadingError(val e: Exception) : Error(e)

        // Error encapsulating issues that occur during the deletion of a photo.
        data class DeletionError(val e: Exception) : Error(e)

        // A general error for any unknown issues that may arise.
        data object UnknownError : Error(Exception("An unknown error occurred!"))
    }
}

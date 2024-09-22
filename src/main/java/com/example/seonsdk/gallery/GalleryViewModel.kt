// GalleryViewModel.kt
package com.example.seonsdk.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seonsdk.services.PhotoStorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * GalleryViewModel manages the photo gallery within the SeonSDK. It handles loading and deleting photos
 * stored in the device, providing a reactive state for the UI to observe and respond to changes in the gallery.
 *
 * The ViewModel interacts with PhotoStorageService to manage photo files and uses Kotlin Flows to emit
 * the current state of the photos and any errors that occur during operations.
 *
 * @param photoStorageService Service responsible for handling photo file management operations like loading and deleting.
 */
class GalleryViewModel(private val photoStorageService: PhotoStorageService) : ViewModel() {

    // StateFlow to hold and emit the list of photos currently available in the gallery.
    private val _photos = MutableStateFlow<List<File>>(emptyList())

    // Publicly exposed StateFlow for observing the list of photos from the UI layer.
    val photos: StateFlow<List<File>> = _photos

    // StateFlow to hold and emit errors encountered during photo operations.
    private val _error = MutableStateFlow<PhotoStorageService.Error?>(null)

    // Publicly exposed StateFlow for observing errors from the UI layer.
    val error: StateFlow<PhotoStorageService.Error?> = _error

    init {
        loadPhotos() // Initial loading of photos when the ViewModel is created.
    }

    /**
     * Loads the list of photos from the storage directory. This method uses the PhotoStorageService
     * to retrieve the files asynchronously and updates the photos StateFlow accordingly.
     */
    private fun loadPhotos() {
        viewModelScope.launch {
            // Collect the flow emitted by loadPhotos() from PhotoStorageService.
            photoStorageService.loadPhotos().collect { result ->
                // Handle the result of the photo loading operation.
                result.fold(
                    onSuccess = { _photos.value = it }, // Update the photos StateFlow with the loaded photos.
                    onFailure = { error -> handleError(error) } // Handle errors if the operation fails.
                )
            }
        }
    }

    /**
     * Deletes a specified photo from the storage directory and reloads the photos to update the state.
     * Uses PhotoStorageService to perform the deletion operation asynchronously.
     *
     * @param photo The file object representing the photo to be deleted.
     */
    fun deletePhoto(photo: File) {
        viewModelScope.launch {
            // Collect the flow emitted by deletePhoto() from PhotoStorageService.
            photoStorageService.deletePhoto(photo).collect { result ->
                // Handle the result of the deletion operation.
                result.fold(
                    onSuccess = { if (it) loadPhotos() }, // Reload photos on successful deletion to update the gallery state.
                    onFailure = { error -> handleError(error) } // Handle errors if the deletion operation fails.
                )
            }
        }
    }

    /**
     * Handles errors encountered during photo loading or deletion operations.
     * Updates the error StateFlow to notify the UI of the encountered error.
     *
     * @param error The throwable error encountered during photo operations.
     */
    private fun handleError(error: Throwable) {
        when (error) {
            is PhotoStorageService.Error -> _error.value = error // Specific errors related to file operations.
            else -> _error.value = PhotoStorageService.Error.UnknownError // Fallback error for unknown issues.
        }
    }
}

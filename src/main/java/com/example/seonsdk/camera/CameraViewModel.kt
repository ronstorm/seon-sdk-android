package com.example.seonsdk.camera

import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.seonsdk.services.PhotoStorageService
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.viewModelScope
import com.example.seonsdk.services.CameraService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraViewModel manages the camera operations and photo storage using the CameraService and PhotoStorageService.
 * This ViewModel follows the MVVM (Model-View-ViewModel) pattern, providing a clear separation of concerns
 * between the UI and the logic handling camera functionality.
 *
 * @param cameraService Service responsible for camera operations like initialization and capturing photos.
 * @param photoStorageService Service responsible for managing the storage of captured photos.
 */
class CameraViewModel(
    private val cameraService: CameraService, // CameraService for managing camera operations.
    private val photoStorageService: PhotoStorageService // PhotoStorageService for handling photo storage.
) : ViewModel() {

    // Mutable state flow to track errors during camera operations, allowing the UI to react to issues.
    private val _error = MutableStateFlow<CameraService.Error?>(null)

    // Publicly exposed state flow for observing errors from the UI layer.
    val error: StateFlow<CameraService.Error?> = _error

    /**
     * Initializes the camera by setting up the preview and capture use cases with the CameraService.
     *
     * @param lifecycleOwner The lifecycle owner (Activity or Fragment) that controls the camera's lifecycle.
     * @param previewView The view where the camera preview will be displayed.
     * @param cameraSelector Specifies the camera (front or back) to be used.
     */
    fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        cameraSelector: CameraSelector
    ) {
        // Initialize the camera through CameraService, handling any errors that occur during setup.
        cameraService.initializeCamera(lifecycleOwner, previewView, cameraSelector) { error ->
            // Update the error state, allowing the UI to observe and respond to errors.
            _error.value = when (error) {
                is CameraService.Error -> error // Specific camera-related errors.
                else -> CameraService.Error.UnknownError // General fallback error.
            }
        }
    }

    /**
     * Captures a photo using the CameraService and saves it to the specified directory managed by PhotoStorageService.
     * The function is executed asynchronously to ensure smooth operation without blocking the UI.
     *
     * @return A Result containing the captured File on success or an error on failure.
     */
    suspend fun capturePhoto(): Result<File> = suspendCancellableCoroutine { continuation ->
        // Launch the capture process in the ViewModel's coroutine scope.
        viewModelScope.launch {
            try {
                // Attempt to capture the photo using the CameraService.
                val result = cameraService.capturePhoto(photoStorageService.getPhotosDirectory())
                // Resume the coroutine with the captured photo result.
                continuation.resume(result)
            } catch (exception: Exception) {
                // Resume the coroutine with an exception in case of failure.
                continuation.resumeWithException(exception)
            }
        }
    }

    /**
     * Called when the ViewModel is cleared (i.e., when the lifecycle owner is destroyed).
     * This method ensures that camera resources are properly released.
     */
    override fun onCleared() {
        super.onCleared()
        // Shutdown the camera executor to free up resources and prevent memory leaks.
        cameraService.shutdown()
    }
}

package com.example.seonsdk.services

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * CameraService is responsible for managing the device's camera functionality within the SeonSDK.
 * It initializes the camera, sets up the preview, captures photos, and handles camera lifecycle management.
 *
 * The service utilizes CameraX, a Jetpack library that simplifies camera integrations in Android.
 * It follows an asynchronous approach using Kotlin Coroutines to handle tasks such as capturing photos.
 *
 * @param context The application context, used for camera provider operations and threading.
 */
class CameraService(private val context: Context) {

    // Holds the ImageCapture use case, responsible for capturing photos.
    private lateinit var imageCapture: ImageCapture

    // Executor service to handle background tasks related to the camera.
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Initializes the camera with the provided lifecycle owner, preview view, and camera selector.
     * Sets up the camera preview and image capture use cases.
     *
     * @param lifecycleOwner The lifecycle owner that controls the camera lifecycle (e.g., Activity or Fragment).
     * @param previewView The view used to display the camera preview to the user.
     * @param cameraSelector Selector specifying which camera (front or back) to use.
     * @param onError Callback for handling errors during camera initialization and binding.
     */
    fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        cameraSelector: CameraSelector,
        onError: (Throwable) -> Unit
    ) {
        // Retrieve the CameraProvider asynchronously to set up camera use cases.
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                // Obtain the CameraProvider instance.
                val cameraProvider = cameraProviderFuture.get()

                // Set up the Preview use case to show the camera feed in the previewView.
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider) // Bind the preview to the view.
                }

                // Initialize the ImageCapture use case for capturing photos.
                imageCapture = ImageCapture.Builder().build()

                try {
                    // Unbind all previously bound use cases before binding new ones.
                    cameraProvider.unbindAll()

                    // Bind the camera lifecycle to the provided lifecycle owner, along with preview and capture use cases.
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc) // Log any errors during binding.
                    onError(Error.CameraAccessFailure(exc)) // Pass the error back via callback.
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Failed to initialize camera", exc) // Log initialization failure.
                onError(Error.CameraAccessFailure(exc)) // Pass the initialization error back via callback.
            }
        }, ContextCompat.getMainExecutor(context)) // Ensure operations run on the main thread for UI safety.
    }

    /**
     * Captures a photo and saves it to the specified output directory. This operation is performed asynchronously.
     *
     * @param outputDirectory The directory where the captured photo will be saved.
     * @return A Result containing the File object of the saved photo on success, or an error on failure.
     */
    suspend fun capturePhoto(outputDirectory: File): Result<File> = suspendCancellableCoroutine { continuation ->
        try {
            // Create a file with a timestamp-based name to save the captured photo.
            val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
            )

            // Set up output options to specify where the captured photo will be saved.
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // Invoke the takePicture method to capture the image and save it using the provided options.
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context), // Ensure callbacks run on the main thread.
                object : ImageCapture.OnImageSavedCallback {
                    // Called when there is an error during photo capture.
                    override fun onError(exc: ImageCaptureException) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(exc) // Resume coroutine with an exception if active.
                        }
                    }

                    // Called when the photo has been successfully saved.
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(photoFile)) // Resume coroutine with the saved file.
                        }
                    }
                }
            )

            // Handle coroutine cancellation by cleaning up resources if necessary.
            continuation.invokeOnCancellation {
                // Placeholder for potential cleanup operations, such as stopping ongoing capture requests.
            }
        } catch (e: Exception) {
            // Immediately handle any exceptions that occur during the capture setup.
            if (continuation.isActive) {
                continuation.resume(Result.failure(e)) // Resume coroutine with failure if still active.
            }
        }
    }

    /**
     * Shuts down the camera executor service to release resources. This should be called when the camera is no longer needed.
     */
    fun shutdown() {
        cameraExecutor.shutdown() // Gracefully shutdown the executor service.
    }

    companion object {
        private const val TAG = "CameraService" // Tag for logging purposes.
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS" // Date format for naming captured photo files.
    }

    /**
     * Represents various errors that can occur within CameraService. Each error type extends Throwable
     * to provide more specific error handling and messaging.
     * - CameraAccessFailure: Indicates a failure when accessing or binding to the camera.
     * - PhotoCaptureFailure: Indicates a failure during the photo capture process.
     * - UnknownError: A general error for unexpected issues.
     */
    sealed class Error(e: Exception) : Throwable(e.message) {
        // Error for camera access issues, such as initialization or binding failures.
        data class CameraAccessFailure(val e: Exception) : Error(e)

        // Error for issues specifically related to photo capture operations.
        data class PhotoCaptureFailure(val e: Exception) : Error(e)

        // A fallback error type for any other unclassified errors.
        data object UnknownError : Error(Exception("An unknown error occurred while using camera!"))
    }
}

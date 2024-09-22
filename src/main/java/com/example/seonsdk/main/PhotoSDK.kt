package com.example.seonsdk.main

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.seonsdk.camera.CameraFragment
import com.example.seonsdk.services.CameraService
import com.example.seonsdk.gallery.GalleryFragment
import com.example.seonsdk.services.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * PhotoSDK is the main entry point of the SeonSDK. It provides methods for taking photos,
 * accessing photos, and authenticating the user. The SDK uses a reactive programming approach
 * with Kotlin Coroutines and Flows for asynchronous operations, ensuring responsive user experiences.
 *
 * The SDK follows the Facade Design Pattern, exposing only essential methods to the users:
 * - takePhoto()
 * - accessPhotos()
 * - authenticateUser() (implicitly used within accessPhotos())
 */
object PhotoSDK {

    // Application context used by services within the SDK.
    // Using appContext prevents memory leaks by not holding onto the Activity or Fragment context.
    private lateinit var appContext: Context

    /**
     * Initializes the SDK with the application context.
     *
     * This method should be called once, ideally from the Application class of the host app
     * to set up necessary resources for the SDK.
     *
     * @param context Application context from the host app.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext // Use application context to avoid memory leaks
    }

    /**
     * Takes a photo by returning a CameraFragment configured to handle the capture process.
     *
     * This method provides a CameraFragment instance that can be used within the host app's UI.
     * It handles the photo capture, storage, and error reporting through provided callbacks.
     *
     * @param onError Callback invoked when an error occurs during the photo capture or saving process.
     * @param onSuccess Callback invoked with the captured photo file upon successful capture.
     * @return A Fragment (CameraFragment) that handles the camera functionality.
     */
    fun takePhoto(onError: (CameraService.Error) -> Unit, onSuccess: (File) -> Unit): Fragment {
        // Create the CameraFragment instance configured with error handling callbacks
        return CameraFragment().apply {
            setOnCaptureErrorListener { error ->
                Log.e("PhotoSDK", "Photo cepturing failed: ${error.message}")
                onError(error) // Delegate error handling back to the caller
            }
            setOnCaptureSuccessListener { photoFile ->
                // If needed, handle additional actions after successful capture
                Log.d("PhotoSDK", "Photo captured")
                onSuccess(photoFile)
            }
        }
    }

    /**
     * Authenticates the user using biometric authentication and grants access to the photo gallery.
     *
     * This method first performs user authentication through the AuthService.
     * If authentication is successful, a GalleryFragment is returned to allow access to photos.
     * If authentication fails, an error callback is triggered.
     *
     * @param activity The FragmentActivity required for fragment transactions and authentication context.
     * @param onAuthenticated Callback invoked with the GalleryFragment if authentication succeeds.
     * @param onError Callback invoked with an AuthService.Error if authentication fails.
     */
    fun accessPhotos(activity: FragmentActivity, onAuthenticated: (Fragment) -> Unit, onError: (AuthService.Error) -> Unit) {
        val authService = AuthService(activity) // Pass the activity context here

        // Launch a coroutine on the Main dispatcher to handle authentication asynchronously.
        CoroutineScope(Dispatchers.Main).launch {
            authService.authenticate().collect { result ->

                when(result) {
                    is AuthService.Result.Error -> onError(result.e)
                    AuthService.Result.Failed -> onError(AuthService.Error.AuthFailure(Exception("Authentication Failed!")))
                    AuthService.Result.Success -> onAuthenticated(GalleryFragment())
                }
            }
        }
    }

}

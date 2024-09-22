package com.example.seonsdk.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.seonsdk.R
import com.example.seonsdk.services.CameraService
import com.example.seonsdk.services.PhotoStorageService
import kotlinx.coroutines.launch
import java.io.File

/**
 * CameraFragment provides the UI and camera functionalities for capturing photos.
 * This fragment integrates the CameraX library to display the camera preview, handle permissions,
 * and capture photos using the ViewModel for managing camera and storage services.
 *
 * It uses a reactive approach to handle user interactions and camera operations, providing callbacks
 * for capture success and error events that can be set externally to respond to capture results.
 */
class CameraFragment : Fragment() {

    // ViewModel handling camera operations and photo storage.
    private val cameraViewModel: CameraViewModel by viewModels {
        CameraViewModelFactory(CameraService(requireContext()), PhotoStorageService(requireContext()))
    }

    // Listeners for handling capture success and error events, can be set externally.
    private var onCaptureErrorListener: ((CameraService.Error) -> Unit)? = null
    private var onCaptureSuccessListener: ((File) -> Unit)? = null

    // Flag to keep track of which camera is currently selected (front or back).
    private var isFrontCamera = true

    // Register the permission request launcher to handle camera permissions dynamically.
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle the result of permission requests.
        permissions.entries.forEach { permission ->
            val isGranted = permission.value
            when (permission.key) {
                Manifest.permission.CAMERA -> {
                    if (isGranted) {
                        // If permission is granted, initialize the camera.
                        setupCamera()
                    } else {
                        // If permission is denied, show a message to the user.
                        Toast.makeText(requireContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and return the view.
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize the UI components and set up the camera after the view has been created.
        setupUI(view)
        observeViewModel()
    }

    /**
     * Sets up the UI components, including buttons for capturing photos and toggling cameras.
     * Checks and requests necessary permissions before setting up the camera.
     *
     * @param view The root view of the fragment.
     */
    private fun setupUI(view: View) {
        // Set up the capture button and its click listener to trigger photo capture.
        val captureButton: ImageButton = view.findViewById(R.id.button_capture)
        captureButton.setOnClickListener {
            capturePhoto()
        }

        // Set up the toggle button to switch between front and back cameras.
        val toggleButton: ImageButton = view.findViewById(R.id.button_toggle_camera)
        toggleButton.setOnClickListener {
            // Toggle between front and back cameras.
            isFrontCamera = !isFrontCamera
            setupCamera()
        }

        // Check if all required permissions are granted, otherwise request them.
        if (allPermissionsGranted()) {
            setupCamera()
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    /**
     * Sets up the camera preview and capture use cases through the ViewModel.
     * Ensures that the view is properly initialized before attempting to set up the camera.
     */
    private fun setupCamera() {
        // Find the preview view in the fragment layout where the camera preview will be displayed.
        val previewView: PreviewView? = view?.findViewById(R.id.view_finder)
        if (previewView != null) {
            // Initialize the camera with the current camera selector (front or back).
            cameraViewModel.initializeCamera(viewLifecycleOwner, previewView, getCurrentCameraSelector())
        } else {
            // Show an error message if the preview view is not found.
            Toast.makeText(requireContext(), "Error: Camera view not found.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Captures a photo using the camera through the ViewModel. Handles the result and invokes the appropriate listener.
     */
    private fun capturePhoto() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Call the suspend function to capture the photo asynchronously.
            val result = cameraViewModel.capturePhoto()

            // Handle the result of the capture operation.
            result.onSuccess { photoFile ->
                // Invoke the success listener if the photo was captured successfully.
                onCaptureSuccessListener?.invoke(photoFile)
            }.onFailure { error ->
                // Invoke the error listener if there was an error during capture.
                onCaptureErrorListener?.invoke(
                    CameraService.Error.PhotoCaptureFailure(
                        Exception("Photo could not be captured due to error: ${error.message}")
                    )
                )
            }
        }
    }

    /**
     * Returns the currently selected camera (front or back) based on the isFrontCamera flag.
     *
     * @return The CameraSelector for the currently selected camera.
     */
    private fun getCurrentCameraSelector(): CameraSelector {
        return if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    /**
     * Checks if all required permissions for the camera are granted.
     *
     * @return True if all permissions are granted, otherwise false.
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Observes the ViewModel for any errors emitted during camera operations.
     * Sends the errors to the error listener for handling.
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Collect errors from the ViewModel and handle them as needed.
            cameraViewModel.error.collect { error ->
                error?.let { handleViewModelError(it) }
            }
        }
    }

    /**
     * Handles errors received from the ViewModel and forwards them to the onCaptureErrorListener.
     *
     * @param error The error emitted by the ViewModel.
     */
    private fun handleViewModelError(error: CameraService.Error) {
        onCaptureErrorListener?.invoke(error)
    }

    /**
     * Sets the listener for handling capture errors.
     *
     * @param listener The callback function to handle capture errors.
     */
    fun setOnCaptureErrorListener(listener: (CameraService.Error) -> Unit) {
        onCaptureErrorListener = listener
    }

    /**
     * Sets the listener for handling successful photo captures.
     *
     * @param listener The callback function to handle successful photo captures.
     */
    fun setOnCaptureSuccessListener(listener: (File) -> Unit) {
        onCaptureSuccessListener = listener
    }

    companion object {
        // Required permissions for the camera to function properly.
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

package com.example.seonsdk.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seonsdk.services.CameraService
import com.example.seonsdk.services.PhotoStorageService

/**
 * Factory class to provide CameraViewModel with required dependencies.
 */
class CameraViewModelFactory(
    private val cameraService: CameraService,
    private val photoStorageService: PhotoStorageService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CameraViewModel(cameraService, photoStorageService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

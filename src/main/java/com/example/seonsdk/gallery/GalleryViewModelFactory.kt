package com.example.seonsdk.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seonsdk.camera.CameraViewModel
import com.example.seonsdk.services.PhotoStorageService

/**
 * Factory class to provide GalleryViewModel with required dependencies.
 */
class GalleryViewModelFactory(
    private val photoStorageService: PhotoStorageService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(photoStorageService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

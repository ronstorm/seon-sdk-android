package com.example.seonsdk.gallery

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seonsdk.R
import com.example.seonsdk.services.PhotoStorageService
import kotlinx.coroutines.launch
import java.io.File

/**
 * GalleryFragment displays a grid of photos stored on the device and provides functionality to
 * view photos in full-screen mode and delete them. It interacts with the GalleryViewModel to manage
 * the photo data and observe changes reactively using Kotlin Flows.
 *
 * The fragment manages UI components such as a RecyclerView for displaying photos, a caption for
 * the number of photos, and a full-screen overlay to view selected photos in detail.
 */
class GalleryFragment : Fragment() {

    // ViewModel managing the loading, deletion, and state of photos.
    private val viewModel: GalleryViewModel by viewModels {
        GalleryViewModelFactory(PhotoStorageService(requireContext()))
    }

    // UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter
    private lateinit var captionTextView: TextView
    private lateinit var fullScreenOverlay: FrameLayout
    private lateinit var fullScreenImageView: ImageView
    private lateinit var closeButton: ImageView
    private lateinit var blurBackground: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)
        setupRecyclerView(view) // Set up the RecyclerView for displaying photos
        setupViews(view) // Initialize other UI components
        observeViewModel() // Observe ViewModel to update UI with photo data
        return view
    }

    /**
     * Initializes UI components such as the caption, full-screen overlay, and close button.
     * Sets up listeners for user interactions with the overlay components.
     *
     * @param view The root view of the fragment.
     */
    private fun setupViews(view: View) {
        // Initialize the caption TextView to update the number of photos
        captionTextView = view.findViewById(R.id.captionTextView)

        // Initialize the full-screen overlay and its components
        fullScreenOverlay = view.findViewById(R.id.fullScreenOverlay)
        fullScreenImageView = view.findViewById(R.id.fullScreenImageView)
        closeButton = view.findViewById(R.id.closeButton)
        blurBackground = view.findViewById(R.id.blurBackground)

        // Set click listener to close the full-screen overlay
        closeButton.setOnClickListener {
            hideFullScreenOverlay() // Hide the overlay when the close button is clicked
        }
    }

    /**
     * Sets up the RecyclerView with a GridLayoutManager and initializes the adapter.
     * Configures the adapter with callbacks for deleting photos and showing photos in full-screen mode.
     *
     * @param view The root view of the fragment.
     */
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 3) // Set the layout to display photos in a grid
        adapter = PhotoAdapter(
            onDeletePhoto = { photo ->
                viewModel.deletePhoto(photo) // Delete the selected photo
            },
            onPhotoClick = { photoFile ->
                showFullScreenOverlay(photoFile) // Show the photo in full-screen mode when clicked
            }
        )
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(GridSpacingItemDecoration(3, 16, true)) // Add spacing between grid items
    }

    /**
     * Observes changes from the ViewModel's photo and error state flows to update the UI.
     * Updates the RecyclerView with new photo data and displays any errors encountered.
     */
    private fun observeViewModel() {
        // Launch the coroutine when the lifecycle is at least STARTED
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect photos from ViewModel and update the adapter
                viewModel.photos.collect { photos ->
                    adapter.submitList(photos) // Update the adapter with the new photo list
                    updateCaption(photos.size) // Update the caption with the number of photos
                }
            }

            // Collect errors emitted by the ViewModel and handle them
            viewModel.error.collect { error ->
                error?.let {
                    handleErrors(it)
                }
            }
        }
    }

    /**
     * Handles errors emitted by the ViewModel by displaying a toast message to the user.
     *
     * @param error The error encountered during photo operations.
     */
    private fun handleErrors(error: PhotoStorageService.Error) {
        showToast(error.message ?: "An unknown error occurred!") // Display error message as toast
    }

    /**
     * Displays a toast message with the provided text.
     *
     * @param message The message to display in the toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Updates the caption TextView to reflect the current number of photos in the gallery.
     *
     * @param photoCount The current number of photos.
     */
    @SuppressLint("SetTextI18n")
    private fun updateCaption(photoCount: Int) {
        captionTextView.text = "$photoCount ${if (photoCount == 1) "photo" else "photos"}" // Set the caption text
    }

    /**
     * Shows the full-screen overlay with the selected photo displayed.
     *
     * @param photoFile The file object of the selected photo to display.
     */
    private fun showFullScreenOverlay(photoFile: File) {
        fullScreenImageView.setImageURI(Uri.fromFile(photoFile)) // Set the image URI to the full-screen ImageView
        blurBackground.visibility = View.VISIBLE
        blurBackground.setBackgroundColor(Color.parseColor("#80000000")) // Add a semi-transparent blur background
        fullScreenOverlay.visibility = View.VISIBLE // Show the full-screen overlay
    }

    /**
     * Hides the full-screen overlay, returning the user to the gallery view.
     */
    private fun hideFullScreenOverlay() {
        fullScreenOverlay.visibility = View.GONE // Hide the overlay
        blurBackground.visibility = View.GONE // Hide the blur background
    }
}

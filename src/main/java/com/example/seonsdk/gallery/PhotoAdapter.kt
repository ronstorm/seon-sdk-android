package com.example.seonsdk.gallery

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seonsdk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * PhotoAdapter is a RecyclerView adapter responsible for displaying photo thumbnails in a grid layout.
 * It provides functionalities to display, delete, and view photos in full-screen mode through click interactions.
 * The adapter efficiently manages the loading of images using coroutines to prevent blocking the main thread.
 *
 * @param onDeletePhoto Callback invoked when a photo is long-clicked and the user confirms deletion.
 * @param onPhotoClick Callback invoked when a photo is clicked to view it in full-screen mode.
 */
class PhotoAdapter(
    private val onDeletePhoto: (File) -> Unit, // Callback for deleting a photo.
    private val onPhotoClick: (File) -> Unit // Callback for showing the photo in full-screen.
) : ListAdapter<File, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    /**
     * Creates a new ViewHolder to represent each item in the RecyclerView.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new instance of PhotoViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    /**
     * Binds the ViewHolder with data for the given position in the adapter.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoFile = getItem(position) // Get the photo file for the current position.
        holder.bind(photoFile) // Bind the photo file to the ViewHolder.
    }

    /**
     * ViewHolder class that represents each photo item in the RecyclerView. It manages the display
     * of the photo thumbnail, full-screen display on click, and deletion confirmation on long click.
     *
     * @param itemView The view corresponding to the individual item.
     */
    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView) // ImageView for displaying the photo.

        /**
         * Binds the photo file to the ImageView and sets up click listeners for full-screen view and deletion.
         *
         * @param photoFile The photo file to display and interact with.
         */
        fun bind(photoFile: File) {
            // Load the image on a background thread to avoid blocking the main thread.
            CoroutineScope(Dispatchers.Main).launch {
                val bitmap = loadBitmap(photoFile) // Load the bitmap of the photo.
                imageView.setImageBitmap(bitmap) // Set the loaded bitmap to the ImageView.
            }

            // Handle photo click to show full-screen view.
            imageView.setOnClickListener {
                onPhotoClick(photoFile) // Trigger the callback to show the full-screen overlay.
            }

            // Set a long click listener to delete the photo with a confirmation dialog.
            imageView.setOnLongClickListener {
                AlertDialog.Builder(it.context)
                    .setTitle("Delete Photo")
                    .setMessage("Are you sure you want to delete this photo?")
                    .setPositiveButton("Yes") { _, _ ->
                        onDeletePhoto(photoFile) // Trigger the deletion callback if confirmed.
                    }
                    .setNegativeButton("No", null) // Do nothing if the user cancels.
                    .show()
                true
            }
        }

        /**
         * Loads a bitmap from the photo file asynchronously and corrects its orientation based on EXIF data.
         *
         * @param photoFile The file from which the bitmap is to be loaded.
         * @return The bitmap image of the photo, corrected for orientation, or null if loading fails.
         */
        private suspend fun loadBitmap(photoFile: File): Bitmap? = withContext(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeFile(photoFile.absolutePath, this)
                    inJustDecodeBounds = false
                    inSampleSize = calculateInSampleSize(this, imageView.width, imageView.height)
                }
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
                bitmap?.let { correctBitmapOrientation(it, photoFile) } // Correct the orientation of the bitmap.
            } catch (e: Exception) {
                e.printStackTrace()
                null // Return null if any exception occurs while loading the bitmap.
            }
        }

        /**
         * Calculates the appropriate inSampleSize for image loading to avoid excessive memory usage.
         *
         * @param options The BitmapFactory.Options containing the original image dimensions.
         * @param reqWidth The required width of the image.
         * @param reqHeight The required height of the image.
         * @return The calculated inSampleSize value to be used for image decoding.
         */
        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val (height: Int, width: Int) = options.outHeight to options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        /**
         * Corrects the bitmap orientation based on EXIF metadata, ensuring the image displays correctly.
         *
         * @param bitmap The original bitmap that may need orientation correction.
         * @param photoFile The file containing the photo's EXIF metadata.
         * @return The bitmap with corrected orientation.
         */
        private fun correctBitmapOrientation(bitmap: Bitmap, photoFile: File): Bitmap {
            val exif = ExifInterface(photoFile.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            // Adjust the rotation of the image based on the EXIF orientation tag.
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            // Return the newly rotated bitmap.
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

    /**
     * DiffUtil.ItemCallback implementation to optimize the RecyclerView by determining changes between items.
     */
    class PhotoDiffCallback : DiffUtil.ItemCallback<File>() {
        /**
         * Checks if two items have the same identity based on their file paths.
         *
         * @param oldItem The old item being compared.
         * @param newItem The new item being compared.
         * @return True if the items are the same, otherwise false.
         */
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

        /**
         * Checks if the contents of two items are the same.
         *
         * @param oldItem The old item being compared.
         * @param newItem The new item being compared.
         * @return True if the contents are the same, otherwise false.
         */
        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }
    }
}

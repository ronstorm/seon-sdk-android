package com.example.seonsdk.gallery

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.State

/**
 * GridSpacingItemDecoration is a custom RecyclerView.ItemDecoration that adds spacing between
 * items in a RecyclerView grid layout. It provides a flexible way to adjust spacing between grid items,
 * including the option to include spacing on the edges of the grid.
 *
 * This decoration helps to create visually appealing layouts by managing the spacing between items
 * consistently, enhancing the overall user experience when viewing grid-based content.
 *
 * @param spanCount The number of columns in the grid layout.
 * @param spacing The amount of spacing (in pixels) to be applied between grid items.
 * @param includeEdge If true, the spacing will also be applied to the edges of the grid; otherwise, it will only be between items.
 */
class GridSpacingItemDecoration(
    private val spanCount: Int, // Number of columns in the grid layout.
    private val spacing: Int,   // Spacing between items in pixels.
    private val includeEdge: Boolean // Whether to include spacing on the grid edges.
) : RecyclerView.ItemDecoration() {

    /**
     * Adjusts the item offsets for each item in the RecyclerView grid to apply the specified spacing.
     *
     * @param outRect Rect to receive the output. This rect will be modified to include the offsets.
     * @param view The child view to be decorated with offsets.
     * @param parent RecyclerView to which ItemDecoration is being applied.
     * @param state The current state of RecyclerView.
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
        val position = parent.getChildAdapterPosition(view) // Get the position of the item in the adapter.
        val column = position % spanCount // Calculate the column index of the item in the grid.

        if (includeEdge) {
            // Include spacing on the left and right edges of each item, based on the column index.
            outRect.left = spacing - column * spacing / spanCount // Adjust the left spacing based on column.
            outRect.right = (column + 1) * spacing / spanCount // Adjust the right spacing based on column.

            // Apply top spacing only if the item is in the first row.
            if (position < spanCount) {
                outRect.top = spacing // Top edge of the first row.
            }
            outRect.bottom = spacing // Bottom spacing is always applied.
        } else {
            // When edges are not included, apply spacing only between items.
            outRect.left = column * spacing / spanCount // Adjust the left spacing based on the column.
            outRect.right = spacing - (column + 1) * spacing / spanCount // Adjust the right spacing.

            // Apply top spacing for all rows except the first one.
            if (position >= spanCount) {
                outRect.top = spacing // Top spacing for subsequent rows.
            }
        }
    }
}

package com.example.theherd

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(
    private val space: Int,
    private val isHorizontal: Boolean = false
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (isHorizontal) {
            outRect.right = space
        } else {
            outRect.bottom = space
        }
    }
}
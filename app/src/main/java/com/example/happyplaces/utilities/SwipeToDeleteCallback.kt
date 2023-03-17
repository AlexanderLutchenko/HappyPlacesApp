package com.example.happyplaces.utilities

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R

//Step39 Create a class for feature to swipe and delete the HappyPlace RecyclerView item

// For detail explanation of this class, look at below link
// https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6

//An abstract class that will be used for delete feature
abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_frver_24dp)
    private val intrinsicWidth = deleteIcon!!.intrinsicWidth
    private val intrinsicHeight = deleteIcon!!.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#F67979")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }


    /*override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        *//**
         * To disable "swipe" for specific item return 0 here.
         * For example:
         * if (viewHolder?.itemViewType == YourAdapter.SOME_TYPE) return 0
         * if (viewHolder?.adapterPosition == 0) return 0
         *//*
        return super.getMovementFlags(recyclerView, viewHolder)
    }*/

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {

        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right - dX, itemView.top.toFloat(), itemView.right.toFloat()+dX, itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        //Draw the colored delete background
        background.color = backgroundColor
        background.setBounds(itemView.right - dX.toInt(), itemView.top, itemView.right+dX.toInt(), itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight)
        val deleteIconLeft = itemView.right - deleteIconMargin
        val deleteIconRight = itemView.right - deleteIconMargin + intrinsicWidth
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
        deleteIcon!!.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}

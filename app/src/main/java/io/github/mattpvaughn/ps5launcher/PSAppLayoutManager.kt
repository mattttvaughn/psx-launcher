package io.github.mattpvaughn.ps5launcher

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.*

class PSAppLayoutManager(context: Context) : LinearLayoutManager(context, HORIZONTAL, false) {

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        scaleChildren()
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return if (orientation == HORIZONTAL) {
            super.scrollHorizontallyBy(dx, recycler, state).also { scaleChildren() }
        } else {
            0
        }
    }


    private fun scaleChildren() {
        val focusLocation = if (childCount > 2) {
            // focus on the center of where the second child will be
            val child  = getChildAt(0)!!
            val childRight =  getDecoratedRight(child)
            val childLeft = getDecoratedLeft(child)
            1.6F * (childRight - childLeft)
        } else {
            0F
        }
        for (i in 0 until childCount) {
            val child = getChildAt(i) as ConstraintLayout
            val childCenter = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f
            val childWidth = getDecoratedRight(child) - getDecoratedLeft(child)
            val focusWidth = childWidth / 3.0f
            val diff = max(abs(childCenter - focusLocation), focusWidth)
            val scaleFactor = 1.2f - (0.2f * diff * diff / (focusWidth * focusWidth))
            val minScale = 0.90f
            val scale = max(scaleFactor, minScale)
            child.findViewById<View>(R.id.app_icon_image_wrapper).scaleX = scale
            child.findViewById<View>(R.id.app_icon_image_wrapper).scaleY = scale
            val appName = child.findViewById<View>(R.id.app_name)
            val alpha = (scale - minScale) * 5f
//            Log.i("sdpl", "Alpha is $alpha")
            appName.alpha = max(alpha, 0f) // no negative alpha please
            child.findViewById<View>(R.id.border).alpha = max(alpha, 0f)
        }
    }

}

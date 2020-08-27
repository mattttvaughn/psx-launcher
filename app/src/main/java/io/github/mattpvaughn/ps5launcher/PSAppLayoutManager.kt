package io.github.mattpvaughn.ps5launcher

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.*

/**
 * A [LinearLayoutManager] which focuses the child currently closest to an offset of [getFocusLocation]
 * from the left edge. The focused child is made to be scale to be larger than the other children as
 * it grows closer to the focus location ([getFocusLocation]). It also fades in an outline around
 * the view as well as text
 */
class PSAppLayoutManager(context: Context) : LinearLayoutManager(context, HORIZONTAL, false) {

    private val maxScale = 1.2f
    private val minScale = 0.9f

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        // Sets the initial scale for all children
        scaleChildren()
    }

    /**
     * Override normal scale behavior to accommodate scaling all child by [scaleChildren] every time
     * [scrollHorizontallyBy] is called
     */
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

    /**
     * Alters [View.setScaleX] and [View.setScaleY] for all children so the child closest to
     * [getFocusLocation] is scaled depending depending on its distance from the focus, being scaled
     * up to a maximum of [maxScale] when it is centered directly on the focus. All other children
     * are scaled to [minScale]
     *
     * Also applies an outline and shows the app name for the child located closest to [getFocusLocation]
     *
     * TODO: Tightly coupled with [R.layout.list_item_app_icon]. Relies on the item view having
     *       elements with [R.id.app_icon_image_wrapper] and [R.id.border] ids. Maybe some contract
     *       could be created to ensure compile time safety?
     *
     * TODO: Magic numbers galore
     */
    private fun scaleChildren() {
        val focusLocation = getFocusLocation()
        for (i in 0 until childCount) {
            val child = getChildAt(i) as ConstraintLayout
            val childCenter = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f
            val childWidth = getDecoratedRight(child) - getDecoratedLeft(child)
            val focusWidth = childWidth / 3.0f
            val diff = max(abs(childCenter - focusLocation), focusWidth)
            val scaleFactor = maxScale - ((1 - maxScale) * diff * diff / (focusWidth * focusWidth))
            val scale = max(scaleFactor, minScale)
            child.findViewById<View>(R.id.app_icon_image_wrapper).scaleX = scale
            child.findViewById<View>(R.id.app_icon_image_wrapper).scaleY = scale
            val appName = child.findViewById<View>(R.id.app_name)
            val alpha = (scale - minScale) * 5f
            appName.alpha = max(alpha, 0f) // prevent negative alpha
            child.findViewById<View>(R.id.border).alpha = max(alpha, 0f)
        }
    }

    /**
     * Returns the x value of the location where children ought to have focus
     *
     * TODO: this might still be a little wonky on different devices
     */
    private fun getFocusLocation(): Float {
        return if (childCount > 2) {
            // focus on the center of where the second child will be
            val child = getChildAt(0)!!
            val childRight = getDecoratedRight(child)
            val childLeft = getDecoratedLeft(child)
            1.6F * (childRight - childLeft)
        } else {
            0F
        }
    }
}

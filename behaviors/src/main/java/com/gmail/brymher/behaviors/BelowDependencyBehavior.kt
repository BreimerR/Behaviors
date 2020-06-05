package com.gmail.brymher.behaviors

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.util.ObjectsCompat
import androidx.core.view.GravityCompat
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


abstract class BelowDependencyBehavior<T : View>(context: Context, attributeSet: AttributeSet) :
    ViewOffsetBehavior<T>(context, attributeSet) {

    var tempRect1 = Rect()
    var tempRect2 = Rect()
    private var layoutInsets: WindowInsetsCompat? = null

    /**
     * @hide
     */

    private var mApplyWindowInsetsListener: OnApplyWindowInsetsListener? = null
    private var mDrawStatusBarBackground = true


    private var verticalLayoutGap = 0
    private var overlayTop = 0

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: T,
        dependency: View
    ): Boolean {
        val hasDependency = dependsOn(parent, child, dependency)

        if (hasDependency) setupForInsets(parent)

        return hasDependency
    }

    abstract fun dependsOn(parent: CoordinatorLayout, child: T, dependency: View): Boolean

    private var calendarHeight: Int? = null

    override fun layoutChild(
        parent: CoordinatorLayout,
        child: T,
        layoutDirection: Int
    ) {
        val dependencies = parent.getDependencies(child)

        val header: T? = findFirstDependency(dependencies)

        if (header != null) {
            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            val available: Rect = tempRect1
            available[parent.paddingLeft + lp.leftMargin, header.bottom + lp.topMargin, parent.width - parent.paddingRight - lp.rightMargin] =
                parent.height + header.bottom - parent.paddingBottom - lp.bottomMargin

            layoutInsets?.let { parentInsets ->
                if (
                    ViewCompat.getFitsSystemWindows(parent) &&
                    !ViewCompat.getFitsSystemWindows(child)
                ) {
                    // If we're set to handle insets but this child isn't, then it has been measured as
                    // if there are no insets. We need to lay it out to match horizontally.
                    // Top and bottom and already handled in the logic above

                    available.left += parentInsets.systemWindowInsetLeft
                    available.right -= parentInsets.systemWindowInsetRight
                }
            }


            val out: Rect = tempRect2
            GravityCompat.apply(
                resolveGravity(lp.gravity),
                child.measuredWidth,
                child.measuredHeight,
                available,
                out,
                layoutDirection
            )
            val overlap: Int = getOverlapPixelsForOffset(header)
            child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap)
            verticalLayoutGap = out.top - header.bottom
        } else {
            // If we don't have a dependency, let super handle it
            super.layoutChild(parent, child, layoutDirection)
            verticalLayoutGap = 0
        }

    }

    abstract fun checkView(view: View?): Boolean

    // this should be abstract not sure how one wants to select their prey s
    // one might use id location which is also quite different
    fun findFirstDependency(views: List<View?>): T? {
        var i = 0
        val z = views.size
        while (i < z) {
            val view = views[i]
            if (checkView(view)) {
                @Suppress("UNCHECKED_CAST")
                return view as T
            }
            i++
        }
        return null
    }

    fun getOverlapPixelsForOffset(header: View?): Int {
        return if (overlayTop == 0) 0 else MathUtils.clamp(
            (getOverlapRatioForOffset(
                header
            ) * overlayTop).toInt(), 0, overlayTop
        )
    }

    private fun resolveGravity(gravity: Int): Int {
        return if (gravity == Gravity.NO_GRAVITY) GravityCompat.START or Gravity.TOP else gravity
    }

    private fun getOverlapRatioForOffset(header: View?): Float {
        return 1f
    }

    open fun setupForInsets(view: View) {
        if (Build.VERSION.SDK_INT < 21) {
            return
        }
        if (ViewCompat.getFitsSystemWindows(view)) {
            if (mApplyWindowInsetsListener == null) {
                mApplyWindowInsetsListener = OnApplyWindowInsetsListener(::setWindowInsets)
            }

            // First apply the insets listener
            ViewCompat.setOnApplyWindowInsetsListener(view, mApplyWindowInsetsListener)

            // Now set the sys ui flags to enable us to layout in the window insets
            view.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(view, null)
        }
    }

    private fun setWindowInsets(view: View?, insets: WindowInsetsCompat?): WindowInsetsCompat? {
        if (!ObjectsCompat.equals(layoutInsets, insets)) {
            layoutInsets = insets
            mDrawStatusBarBackground = insets != null && insets.systemWindowInsetTop > 0
            view?.setWillNotDraw(!mDrawStatusBarBackground && view.background == null)

            view?.requestLayout()
        }
        return insets
    }
}
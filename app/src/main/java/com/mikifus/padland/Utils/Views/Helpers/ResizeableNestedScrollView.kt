package com.mikifus.padland.Utils.Views.Helpers

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.mikifus.padland.R

/**
 * The coordinator layout hides and shows FABs dynamically on
 * scroll, but when the size of the NestedScrollView contents
 * change, no scroll event is emitted and no scroll can be
 * done by the user. This means the FABs will stay hidden
 * without option to show.
 *
 * This class listens for layout changes and detects when
 * the contents of the NestedScrollView's single child
 * are smaller than the scroll view in order to search
 * for FABs to show.
 *
 * Combine it with some space in the child to avoid hiding
 * the content behind the buttons (padding won't do right).
 * Example:
 * <Space
 *      android:layout_width="match_parent"
 *      android:layout_height="?attr/actionBarSize"/>
 */
class ResizeableNestedScrollView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.nestedScrollViewStyle)
    : NestedScrollView(context, attributeSet, defStyleAttr) {

    init {
        addOnLayoutChangeListener {
                view,
                left, top, right, bottom,
                oldLeft, oldTop, oldRight, oldBottom->

            val nestedChild = (view as NestedScrollView).children.iterator().next()
            if(nestedChild.bottom < bottom - 1) {
                val it = (parent as ViewGroup).children.iterator()
                while(it.hasNext()) {
                    val child = it.next()
                    if(child.layoutParams
                                is CoordinatorLayout.LayoutParams &&
                        (child.layoutParams as CoordinatorLayout.LayoutParams).behavior
                                is HideBottomViewOnScrollBehavior
                        ) {
                        ((child.layoutParams as CoordinatorLayout.LayoutParams).behavior
                                as HideBottomViewOnScrollBehavior).slideUp(child, true)
                    }
                }
            }
        }
    }
}
package com.kreativesquadz.billkit.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kreativesquadz.billkit.R

class GenericDropDownToggle<T : TextView>(
    private val headerView: T,
    private val contentView: View,
    private val layoutView: View,
    private val expandedDrawable: Int,
    private val collapsedDrawable: Int,
    private val onExpand: (() -> Unit)? = null,
    private val onStateChange: ((Boolean) -> Unit)? = null
) {
    private var isAnimating = false

    fun initialize() {
        headerView.setOnClickListener {
            if (isAnimating) return@setOnClickListener

            val isCollapsed = contentView.visibility == View.GONE
            toggleVisibility(isCollapsed)
            headerView.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, if (isCollapsed) expandedDrawable else collapsedDrawable, 0
            )
            layoutView.setBackgroundColor(
                ContextCompat.getColor(headerView.context, if (isCollapsed) R.color.lite_grey_200 else R.color.white)
            )
            onExpand?.takeIf { isCollapsed }?.invoke()
            onStateChange?.invoke(!isCollapsed)
        }
    }

    private fun toggleVisibility(isCollapsed: Boolean) {
        if (isCollapsed) {
            expandView()
        } else {
            collapseView()
        }
    }

    private fun expandView() {
        contentView.visibility = View.VISIBLE

        // Request a layout pass and measure height
        contentView.post {
            // Force a layout pass to get accurate height
            contentView.measure(
                View.MeasureSpec.makeMeasureSpec(contentView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val targetHeight = contentView.measuredHeight

            // Delay animation slightly to ensure height measurement is correct
            contentView.postDelayed({
                isAnimating = true
                val animator = ValueAnimator.ofInt(0, targetHeight).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener { animation ->
                        contentView.layoutParams.height = animation.animatedValue as Int
                        contentView.requestLayout()
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            isAnimating = false
                        }
                    })
                }
                animator.start()
            }, 2) // Adjust delay as needed
        }
    }

    private fun collapseView() {
        val initialHeight = contentView.measuredHeight

        isAnimating = true
        val animator = ValueAnimator.ofInt(initialHeight, 0).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                contentView.layoutParams.height = animation.animatedValue as Int
                contentView.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    contentView.visibility = View.GONE
                    isAnimating = false
                }
            })
        }
        animator.start()
    }
}

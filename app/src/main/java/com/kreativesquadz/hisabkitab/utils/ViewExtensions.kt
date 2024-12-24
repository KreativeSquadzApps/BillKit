package com.kreativesquadz.hisabkitab.utils


import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator

fun View.expand(duration: Long = 300) {
    this.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetHeight = this.measuredHeight

    this.layoutParams.height = 0
    this.visibility = View.VISIBLE

    val animator = ValueAnimator.ofInt(0, targetHeight)
    animator.duration = duration
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.addUpdateListener { animation ->
        this.layoutParams.height = animation.animatedValue as Int
        this.requestLayout()
    }
    animator.start()
}
fun View.collapse(duration: Long = 300) {
    val initialHeight = this.measuredHeight

    val animator = ValueAnimator.ofInt(initialHeight, 0)
    animator.duration = duration
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.addUpdateListener { animation ->
        this.layoutParams.height = animation.animatedValue as Int
        this.requestLayout()
        if (animation.animatedValue as Int == 0) {
            this.visibility = View.GONE
        }
    }
    animator.start()
}

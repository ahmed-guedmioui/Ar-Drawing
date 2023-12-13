package com.med.drawing.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class AppAnimation {

    private lateinit var animatorSet: AnimatorSet
    private var isBig = false

    fun startRepeatingAnimation(view: View) {
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1f)

        val animationDuration = 500L
        scaleXAnimator.duration = animationDuration
        scaleYAnimator.duration = animationDuration
        val interpolator = AccelerateDecelerateInterpolator()
        scaleXAnimator.interpolator = interpolator
        scaleYAnimator.interpolator = interpolator

        animatorSet = AnimatorSet()
        animatorSet.play(scaleXAnimator).with(scaleYAnimator)

        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                if (isBig) {
                    scaleXAnimator.setFloatValues(1.05f, 1f)
                    scaleYAnimator.setFloatValues(1.05f, 1f)
                } else {
                    scaleXAnimator.setFloatValues(1f, 1.05f)
                    scaleYAnimator.setFloatValues(1f, 1.05f)
                }
                isBig = !isBig
                animatorSet.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animatorSet.start()
    }


    fun startLeftwardScaleAnimation(view: View) {
        view.translationX = view.width.toFloat()

        // Create translationX animator
        val translationXAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f)
        translationXAnimator.duration = 100L
        translationXAnimator.interpolator = AccelerateDecelerateInterpolator()

        // Create animator set and play the translationX animator
        val animatorSet = AnimatorSet()
        animatorSet.play(translationXAnimator)
        animatorSet.start()
    }

}
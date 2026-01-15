package com.example.aplapollo.helper

import android.view.MotionEvent



import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object Utils3D {

    fun apply(view: View) {
        view.apply {
            cameraDistance = 8000 * resources.displayMetrics.density

            // Start tilted
            rotationX = 12f
            rotationY = -10f
            scaleX = 0.95f
            scaleY = 0.95f

            animate()
                .rotationX(0f)
                .rotationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    fun pressEffect(view: View) {
        view.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(150)
            .start()
    }

    fun releaseEffect(view: View) {
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start()
    }
}

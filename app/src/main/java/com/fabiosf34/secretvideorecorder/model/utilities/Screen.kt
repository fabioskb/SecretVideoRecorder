package com.fabiosf34.secretvideorecorder.model.utilities

import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout

class Screen() {

    fun centerLinearLayoutFromConstraintLayout(view: View, center: Boolean) {
        val layoutParams = view.layoutParams as ConstraintLayout.LayoutParams
        if (center) {
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.horizontalBias = 0.5f
            layoutParams.verticalBias = 0.5f
        } else {
            layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.horizontalBias = 0.5f
            layoutParams.verticalBias = 0.5f
        }
        view.layoutParams = layoutParams
        view.requestLayout()
    }

    fun setMarginsForView(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(left, top, right, bottom)
            view.layoutParams = params
            view.requestLayout()
        }
    }
    fun getScreenResolution(windowManager: WindowManager, resources: Resources): List<Int> {

        var deviceScreenWidthPx: Int
        var deviceScreenHeightPx: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            deviceScreenWidthPx = windowMetrics.bounds.width() - insets.left - insets.right
            deviceScreenHeightPx = windowMetrics.bounds.height() - insets.top - insets.bottom

            Log.d("ScreenResolution", "Largura da Janela (API 30+): $deviceScreenWidthPx px")
            Log.d("ScreenResolution", "Altura da Janela (API 30+): $deviceScreenHeightPx px")
        } else {
            // Fallback para APIs mais antigas (usar DisplayMetrics)
            val displayMetrics = resources.displayMetrics
            deviceScreenWidthPx = displayMetrics.widthPixels
            deviceScreenHeightPx = displayMetrics.heightPixels
            Log.d("ScreenResolution", "Largura da Tela (Fallback): $deviceScreenWidthPx px")
            Log.d("ScreenResolution", "Altura da Tela (Fallback): $deviceScreenHeightPx px")
        }
        return listOf(deviceScreenWidthPx, deviceScreenHeightPx)
    }

    fun dpToPx(dp: Int): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (dp * density).toInt()
    }
    fun getScreenResolutionDp(windowManager: WindowManager, resources: Resources) {

        var deviceScreenWidthPx: Int
        var deviceScreenHeightPx: Int

        val deviceScreenDensity = resources.displayMetrics.density

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            deviceScreenWidthPx = windowMetrics.bounds.width() - insets.left - insets.right
            deviceScreenHeightPx = windowMetrics.bounds.height() - insets.top - insets.bottom

            Log.d(
                "ScreenResolution",
                "Largura da Janela em dp (API 30+): ${deviceScreenWidthPx / deviceScreenDensity} dp"
            )
            Log.d(
                "ScreenResolution",
                "Altura da Janela em dp (API 30+): ${deviceScreenHeightPx / deviceScreenDensity} dp"
            )
        } else {
            // Fallback para APIs mais antigas (usar DisplayMetrics)
            val displayMetrics = resources.displayMetrics
            deviceScreenWidthPx = displayMetrics.widthPixels
            deviceScreenHeightPx = displayMetrics.heightPixels
            Log.d(
                "ScreenResolution",
                "Largura da Tela (Fallback) em dp: ${deviceScreenWidthPx / deviceScreenDensity} dp"
            )
            Log.d(
                "ScreenResolution",
                "Altura da Tela (Fallback) em dp: ${deviceScreenHeightPx / deviceScreenDensity} dp"
            )
        }
    }
}
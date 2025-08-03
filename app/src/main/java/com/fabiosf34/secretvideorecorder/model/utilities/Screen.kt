package com.fabiosf34.secretvideorecorder.model.utilities

import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager

class Screen() {
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
}
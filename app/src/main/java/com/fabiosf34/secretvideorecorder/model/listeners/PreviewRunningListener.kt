package com.fabiosf34.secretvideorecorder.model.listeners

interface PreviewRunningListener {
    fun onPreviewRunning(isPreviewRunning: Boolean)
    fun onPreviewStopped(isPreviewRunning: Boolean)
}
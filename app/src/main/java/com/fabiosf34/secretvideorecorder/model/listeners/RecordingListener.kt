package com.fabiosf34.secretvideorecorder.model.listeners

interface RecordingListener {
    fun onRecordingComplete()
    fun onRecording()
    fun onRecordingError(msg: String)
}
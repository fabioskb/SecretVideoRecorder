package com.fabiosf34.secretvideorecorder.model.listeners

interface OnVideoListener {
    fun onClick(uri: String) {}
    fun onDelete(id: Int, uri: String) {}
}
package com.fabiosf34.secretvideorecorder.viewModel

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.view.View
import android.view.WindowManager
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fabiosf34.secretvideorecorder.model.Video
import com.fabiosf34.secretvideorecorder.model.listeners.PreviewRunningListener
import com.fabiosf34.secretvideorecorder.model.repository.Preferences
import com.fabiosf34.secretvideorecorder.model.repository.VideoRepository
import com.fabiosf34.secretvideorecorder.model.services.Camcorder
import com.fabiosf34.secretvideorecorder.model.utilities.Ads
import com.fabiosf34.secretvideorecorder.model.utilities.Notifications
import com.fabiosf34.secretvideorecorder.model.utilities.Screen
import com.fabiosf34.secretvideorecorder.model.utilities.Utils
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
//import com.google.android.gms.ads.AdView
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CamViewModel(application: Application) : AndroidViewModel(application),
     PreviewRunningListener {

    private val ads = Ads(application)
    private val screen = Screen()
    private val preferences = Preferences(application)
    private val camcorder = Camcorder(application)
    private val galleryRepository = VideoRepository(application)
    private val notifications = Notifications(application)

    private val _isPreview = MutableLiveData<Boolean>()
    val isPreview: LiveData<Boolean> = _isPreview

    private val _hasBothCamera = MutableLiveData<Boolean>()
    val hasBothCamera: LiveData<Boolean> = _hasBothCamera

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private val _startRecordingOnBoot = MutableLiveData<Boolean>()
    val startRecordingOnBoot: LiveData<Boolean> = _startRecordingOnBoot

    fun startCamPreview(
        viewFinder: PreviewView,
        cameraSelector: CameraSelector,
        lifecycleOwner: LifecycleOwner,
        useCamcorder: Boolean = false
    ) {
        camcorder.startCamPreview(
            viewFinder,
            cameraSelector,
            lifecycleOwner,
            this,
            useCamcorder
        )
        _isPreview.value = true
    }

//    fun requestBatteryOptimizationExemption(activity: Activity) {
//        permissions.requestBatteryOptimizationExemption(activity)
//    }

    fun stopCamPreview() {
        _isPreview.value = camcorder.stopCamPreview()
    }

    fun hasBackCamera() {
        camcorder.hasBackCamera()
    }

    fun hasFrontCamera() {
        camcorder.hasFrontCamera()
    }

    fun checkBothCameraAvailable() {
        val hasBackCamera = preferences.retrieve(Utils.HAS_BACK_CAMERA, false)
        val hasFrontCamera = preferences.retrieve(Utils.HAS_FRONT_CAMERA, false)
        _hasBothCamera.value = hasBackCamera && hasFrontCamera
    }

    override fun onPreviewRunning(isPreviewRunning: Boolean) {
        _isPreview.value = isPreviewRunning
    }

    override fun onPreviewStopped(isPreviewRunning: Boolean) {
        _isPreview.value = isPreviewRunning
    }

    fun saveVideo(video: Video) {
        galleryRepository.insert(video)
    }

    fun saveRecordingStatus(isRecording: Boolean) {
        preferences.store(Utils.IS_RECORDING, isRecording)
        _isRecording.value = isRecording
    }

    fun getRecordingStatus(): Boolean {
        return preferences.retrieve(Utils.IS_RECORDING, false)
    }

    fun getRecordingOnBootStatus() {
        _startRecordingOnBoot.value = retrievePreference(Utils.START_RECORDING_ON_BOOT, false)
    }

    fun getScreenResolution(windowManager: WindowManager, resources: Resources): List<Int> {
        return screen.getScreenResolution(windowManager, resources)
    }

    fun setMarginsForView(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        screen.setMarginsForView(view, left, top, right, bottom)
    }

    fun centerLinearLayoutFromConstraintLayout(view: View, center: Boolean) {
        screen.centerLinearLayoutFromConstraintLayout(view, center)
    }

    fun dpToPx(dp: Int): Int {
        return screen.dpToPx(dp)
    }
    fun restartApp(context: Context, lifecycleScope: LifecycleCoroutineScope) {
        // Exemplo com WorkManager para um atraso de 30 segundos
//        val workRequest = OneTimeWorkRequestBuilder<MyDelayedWorker>()
//            .setInitialDelay(30, TimeUnit.SECONDS) // Atraso desejado
//            .build()
//        WorkManager.getInstance(this).enqueue(workRequest)

        // Ou, se for algo mais simples e não precisar ser garantido,
        // você pode usar uma coroutine com delay:
        lifecycleScope.launch { // Se estiver em uma Activity/Fragment
            delay(1000) // 1 segundos de atraso
            Utils.AppUtils.triggerRebirth(context)
        }
    }

    fun completeVideoRecordingNotification(videoUri: Uri, videoId: Int) {
        notifications.showRecordingCompleteNotification(videoUri, videoId)
    }

    fun errorVideoRecordingNotification(message: String) {
        notifications.showErrorNotification(message)
    }

    fun getVideoIdFromUri(uri: String): Int {
        return galleryRepository.getVideoIdFromUri(uri)
    }

    fun retrievePreference(key: String, defaultValue: String): String? {
        return preferences.retrieve(key, defaultValue)
    }

    fun retrievePreference(key: String, defaultValue: Boolean): Boolean {
        return preferences.retrieve(key, defaultValue)
    }

    fun storePreference(key: String, value: String) {
        preferences.store(key, value)
    }

    fun storePreference(key: String, value: Boolean) {
        preferences.store(key, value)
    }

    fun adMobBanner(): AdView {
        return ads.adMobBanner()
    }

    fun adMobInterstitialLoader(loadCallback: InterstitialAdLoadCallback) {
        ads.adMobInterstitialLoader(loadCallback)
    }
}
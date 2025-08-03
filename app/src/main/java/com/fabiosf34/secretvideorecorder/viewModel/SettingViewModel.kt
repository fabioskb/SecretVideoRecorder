package com.fabiosf34.secretvideorecorder.viewModel

import android.app.Activity
import android.app.Application
import android.text.style.ClickableSpan
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import com.fabiosf34.secretvideorecorder.model.repository.Preferences
import com.fabiosf34.secretvideorecorder.model.utilities.BiometricHelper
import com.fabiosf34.secretvideorecorder.model.utilities.Links
import com.fabiosf34.secretvideorecorder.model.utilities.StorageHelper
import com.fabiosf34.secretvideorecorder.model.utilities.Utils

class SettingViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = Preferences(application)
    private val storageHelper = StorageHelper(application)

    private val _isStopRecordingSwitchChecked = MutableLiveData<Boolean>()
    val isStopRecordingSwitchChecked: LiveData<Boolean> = _isStopRecordingSwitchChecked

    private val _isPasswordChecked = MutableLiveData<Boolean>()
    val isPasswordChecked: LiveData<Boolean> = _isPasswordChecked

    private val _isBiometricChecked = MutableLiveData<Boolean>()
    val isBiometricChecked: LiveData<Boolean> = _isBiometricChecked

    private val _isBiometricAvailable = MutableLiveData<Boolean>()
    val isBiometricAvailable: LiveData<Boolean> = _isBiometricAvailable

    private val _isShowCompletedNotification = MutableLiveData<Boolean>()
    val isShowCompletedNotification: LiveData<Boolean> = _isShowCompletedNotification

    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private val _isAppNameSwitched = MutableLiveData<Boolean>()
    val isAppNameSwitched: LiveData<Boolean> = _isAppNameSwitched

    private val _isSdCardDirSwitchChecked = MutableLiveData<Boolean>()
    val isSdCardDirSwitchChecked: LiveData<Boolean> = _isSdCardDirSwitchChecked

    private val _hasSdCardVideoDir = MutableLiveData<Boolean>()
    val hasSdCardVideoDir: LiveData<Boolean> = _hasSdCardVideoDir
    private val _videoQuality = MutableLiveData<String?>()
    val videoQuality: LiveData<String?> = _videoQuality

    private val _appPassword = MutableLiveData<String>()
    val appPassword: LiveData<String> = _appPassword

    private val _startRecordingOnBoot = MutableLiveData<Boolean>()
    val startRecordingOnBoot: LiveData<Boolean> = _startRecordingOnBoot

    fun saveStopRecordingSwitchState(isChecked: Boolean) {
        preferences.store(Utils.STOP_RECORDING_SWITCH_SETTINGS, isChecked)
        _isStopRecordingSwitchChecked.value = isChecked
    }

    fun setSwitchesColorAndState(switch: SwitchCompat, isChecked: Boolean) {
        Utils.setSwitchesColorAndState(getApplication(), switch, isChecked)
    }

    fun setPrivacyPolicyLink(textView: TextView, clickableSpan: ClickableSpan) {
        Links.setPrivacyPolicyLink(textView, clickableSpan)
    }

    fun getPassword(): String {
        _appPassword.value = preferences.retrieve(Utils.PASSWORD.PASSWORD, "").toString()
        return _appPassword.value!!
    }

    fun setPassword(password: String) {
        preferences.store(Utils.PASSWORD.PASSWORD, password)
    }

    fun enablePasswdApp(isPasswdEnabled: Boolean) {
        preferences.store(Utils.PASSWORD.IS_PASSWD_ENABLED, isPasswdEnabled)
    }

    fun isPasswordEnabled(): Boolean {
        return preferences.retrieve(Utils.PASSWORD.IS_PASSWD_ENABLED, false)
    }

    fun loadSettingsPreferences() {
        _isPasswordChecked.value = getPrefers(Utils.PASSWORD.IS_PASSWD_ENABLED, false)
        _isStopRecordingSwitchChecked.value =
            getPrefers(Utils.STOP_RECORDING_SWITCH_SETTINGS, false)
        _isBiometricChecked.value =
            getPrefers(Utils.PASSWORD.BIOMETRIC_SETTINGS, false)
        _isShowCompletedNotification.value =
            getPrefers(Utils.SHOW_VIDEO_RECORDING_COMPLETE_NOTIFICATIONS, false)
        _isRecording.value = getPrefers(Utils.IS_RECORDING, false)
        _isAppNameSwitched.value = getPrefers(Utils.APP_NAME_SWITCH_SETTINGS, false)
        _isSdCardDirSwitchChecked.value = getPrefers(Utils.SAVE_VIDEO_ON_SD_CARD, false)
//        _hasSdCardVideoDir.value = storageHelper.hasSdCardVideoDir()
        _videoQuality.value = getPrefers(Utils.VIDEO_QUALITY, Utils.VIDEO_QUALITY_DEFAULT)
        _appPassword.value = preferences.retrieve(Utils.PASSWORD.PASSWORD, "").toString()
        _startRecordingOnBoot.value = getPrefers(Utils.START_RECORDING_ON_BOOT, false)
    }

    fun saveBiometricSwitchState(isChecked: Boolean) {
        preferences.store(Utils.PASSWORD.BIOMETRIC_SETTINGS, isChecked)
        _isBiometricChecked.value = isChecked

    }

    fun checkBiometricDisposable() {
        _isBiometricAvailable.value = BiometricHelper.isBiometricAvailable(application)
    }

    fun biometric(activity: Activity, isChecked: Boolean) {
        if (isChecked) {
            BiometricHelper.biometricActivitySuccessful(activity)
        }
    }

    fun saveShowCompletedNotification(isChecked: Boolean) {
        preferences.store(Utils.SHOW_VIDEO_RECORDING_COMPLETE_NOTIFICATIONS, isChecked)
        _isShowCompletedNotification.value = isChecked
    }

    fun saveAppNameSwitchState(isChecked: Boolean) {
        preferences.store(Utils.APP_NAME_SWITCH_SETTINGS, isChecked)
        _isAppNameSwitched.value = isChecked
    }

    fun saveSdCardDirSwitchState(isChecked: Boolean) {
        preferences.store(Utils.SAVE_VIDEO_ON_SD_CARD, isChecked)
        _isSdCardDirSwitchChecked.value = isChecked
    }

    fun saveVideoQuality(quality: String) {
        preferences.store(Utils.VIDEO_QUALITY, quality)
        _videoQuality.value = quality
    }

    fun setAppLauncherAlias(aliasToEnable: String, aliasToDisable: String) {
        Utils.AppUtils.setAppLauncherAlias(
            application.applicationContext,
            aliasToEnable,
            aliasToDisable
        )
    }

    fun hasSdCardVideoDir() {
        _hasSdCardVideoDir.value = storageHelper.hasSdCardVideoDir()
        savePrefers(Utils.HAS_SD_CARD_VIDEO_DIR, _hasSdCardVideoDir.value!!)
    }

    fun saveStartRecordingOnBoot(isChecked: Boolean) {
        preferences.store(Utils.START_RECORDING_ON_BOOT, isChecked)
        _startRecordingOnBoot.value = isChecked
    }

    // Preferences
    fun getPrefers(key: String, defaultValue: Boolean): Boolean {
        return preferences.retrieve(key, defaultValue)
    }

    fun savePrefers(key: String, value: Boolean) {
        preferences.store(key, value)
    }

    fun getPrefers(key: String, defaultValue: String): String? {
        return preferences.retrieve(key, defaultValue)
    }

//    fun savePrefers(key: String, value: String) {
//        preferences.store(key, value)
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
}


package com.fabiosf34.secretvideorecorder.model.utilities

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class Ads(val context: Context) {
    fun adMobBanner(): AdView {
//        val extras = Bundle()
//        extras.putString("collapsible", "bottom")
        val adView = AdView(context)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = Utils.BANNER_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        return adView
    }

    fun adMobInterstitialLoader(loadCallback: InterstitialAdLoadCallback) {
        // Interstitial
        InterstitialAd.load(
            context,
            Utils.VIDEO_INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            loadCallback
        )

    }
}
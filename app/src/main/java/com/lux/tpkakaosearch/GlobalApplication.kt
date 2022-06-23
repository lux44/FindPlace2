package com.lux.tpkakaosearch

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication:Application() {
    override fun onCreate() {
        super.onCreate()

        // Kakao SDK 초기화
        KakaoSdk.init(this, "0022b242ecad27618f9239aa8ba45476")
    }
}
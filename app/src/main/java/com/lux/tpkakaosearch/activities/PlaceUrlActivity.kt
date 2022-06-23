package com.lux.tpkakaosearch.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import com.lux.tpkakaosearch.databinding.ActivityPlaceUrlBinding

class PlaceUrlActivity : AppCompatActivity() {

    val binding:ActivityPlaceUrlBinding by lazy { ActivityPlaceUrlBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.wv.webViewClient= WebViewClient()
        binding.wv.webChromeClient= WebChromeClient()

        binding.wv.settings.javaScriptEnabled=true

        val placeUrl:String = intent.getStringExtra("place_url") ?: ""
        binding.wv.loadUrl(placeUrl)
    }

    // 디바이스의 뒤로가기 버튼을 클릭했을때
    // 웹페이지의 히스토리가 있다면 웹 페이지만 뒤로가기
    override fun onBackPressed() {
        if (binding.wv.canGoBack()) binding.wv.goBack()
        else super.onBackPressed()
    }
}
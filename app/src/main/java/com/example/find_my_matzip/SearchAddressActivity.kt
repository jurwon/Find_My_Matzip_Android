package com.matzip.find_my_matzip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.matzip.find_my_matzip.databinding.ActivitySearchAddressBinding

// firebase서버에 hosting된 address 검색 api 호출
class SearchAddressActivity : AppCompatActivity() {
    lateinit var binding : ActivitySearchAddressBinding
    private val TAG: String = "SearchAddressActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 자바스크립트 사용 허용
        val webView = binding.webView
        webView.settings.javaScriptEnabled = true

        webView.addJavascriptInterface(BridgeInterface(),"Android")


        // Javascript의 sample2_execDaumPostcode() 메서드가 실행되고,
        // 결과값은 BridgeInterface 통해서 전달받음
        webView.webViewClient = object : WebViewClient() {
            //2) 웹뷰 로드가 모두 끝나면 호출되는 메서드
            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                webView.loadUrl("javascript:sample2_execDaumPostcode();")
            }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Log.e(TAG, "Error: ${error?.description}")
            }
        }

        // 1) 최초 웹뷰 로드(firebase의 hostUrl)
        webView.loadUrl("https://searchaddress-7d401.web.app/")
    }


    // 3) 다음(카카오) 주소 검색 API의 결과 값을 여기를 통해 전달받음
    // 해당 코드는 호스팅 서버에 올라와있음.(JavaScript)
    inner class BridgeInterface {
        @JavascriptInterface
        fun processDATA(data: String) {
            val extra = Bundle()
            val intent = Intent()

            extra.putString("data", data)
            intent.putExtras(extra)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}
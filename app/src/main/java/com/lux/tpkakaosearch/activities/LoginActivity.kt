package com.lux.tpkakaosearch.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.lux.tpkakaosearch.G
import com.lux.tpkakaosearch.databinding.ActivityLoginBinding
import com.lux.tpkakaosearch.model.NaverUserInfoResponse
import com.lux.tpkakaosearch.model.UserAccount
import com.lux.tpkakaosearch.network.RetrofitApiService
import com.lux.tpkakaosearch.network.RetrofitHelper
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {

    val binding:ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(LayoutInflater.from(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 둘러보기 글씨 클릭으로 로그인없이 Main 화면으로 이동
        binding.tvGo.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        // 회원가입 버튼 클릭
        binding.tvSignup.setOnClickListener {
            //
            startActivity(Intent(this,SignUpActivity::class.java))
        }
        // 이메일 로그인 버튼 클릭
        binding.layoutLoginEmail.setOnClickListener {
            startActivity(Intent(this,EmailSignInActivity::class.java))
        }
        // 간편 로그인 버튼들 클릭
        binding.btnLoginKakao.setOnClickListener {  clickLoginKakao() }
        binding.btnLoginGoogle.setOnClickListener {  clickLoginGoogle() }
        binding.btnLoginNaver.setOnClickListener {  clickLoginNaver() }


        // 카카오 로그인 키해시값 얻어오기
        val keyHash:String=Utility.getKeyHash(this)
        Log.i("keyHash",keyHash)

    }  // onCreate Method

    private fun clickLoginKakao(){
        // kakao login sdk 설치

        //카카오 로그인 성공했을때 반응하는 callback 객체 생성
        val callback:(OAuthToken?, Throwable?)->Unit={token, error ->
            if (error !=null){
                Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "카카오 로그인 성공", Toast.LENGTH_SHORT).show()

                // 사용자의 정보 요청
                UserApiClient.instance.me{user, error ->
                    if (user!=null){
                        var id:String=user.id.toString()
                        var email:String = user.kakaoAccount?.email ?: ""


                        G.userAccount= UserAccount(id,email)

                        // MainActivity로 이동
                        startActivity(Intent(this,MainActivity::class.java))
                        finish()
                    }
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡 로그인, 없으면 카카오 계정 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
            UserApiClient.instance.loginWithKakaoTalk(this,callback = callback)
        }else{
            UserApiClient.instance.loginWithKakaoTalk(this,callback = callback)
        }

    }
    private fun clickLoginGoogle(){
        //Firebase Authentication - ID 공급업체 (Google)

        // google login 관련 가이드 문서를 firebase 에서 보면
        // 무조건 firebase 의 auth 제품과 연동하여 만들도록 안내함.

        // 그래서 구글 로그인만 하려면 구글 개발자 사이트의 가이드문서를 참고할 것을 권장

        // 구글 로그인 옵션객체 생성 - Builder 이용
        val gso:GoogleSignInOptions=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // 구글 로그인 화면 액티비티를 실행시켜주는 Intent 객체 얻어오기
        val intent:Intent= GoogleSignIn.getClient(this,gso).signInIntent
        resultLauncher.launch(intent)
    }

    // 새로운 액티비티를 실행하고 그 결과를 받아오는 객체를 등록하기
    val resultLauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult?) {
                if (result?.resultCode== RESULT_OK) return

                // 로그인 결과를 가져온 Intent 객체 소환
                val intent:Intent?=result?.data

                // Intent 로부터 구글 계정 정보를 가져오는 작업 객체 생성하여 결과 데이터 받기
                val account:GoogleSignInAccount=GoogleSignIn.getSignedInAccountFromIntent(intent).result

                val id:String=account.id.toString()
                var email:String=account.email?:""

                Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
                G.userAccount= UserAccount(id, email)

                // main 화면으로 이동
                startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                finish()
            }
        })


    private fun clickLoginNaver(){
        // 네이버 아이디 로그인[네아로] - 사용자 정보를 REST API 로 받아오는 방식
        // Retrofit 네트워크 라이브러리 이용

        // 네이버 개발자 센터의 가이드문서 참고
        // Nid-OAuth sdk 추가


        // 로그인 초기화
        NaverIdLoginSDK.initialize(this,"fLPv0NR7_3kyWWS2nDPc","J1RP6cmYPe","쏘쏘")

        // 로그인 인증하기 - 로그인 정보를 받는 것이 아니라
        // 로그인 정보를 받기 위한 REST API 의 접근 키(token : 토큰)을 발급받는 것임.
        // 이 token 으로 네트워크 API 를 통해 json 데이터를 받아 정보를 얻어오는 것.
        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback {
            override fun onError(errorCode: Int, message: String) {
                Toast.makeText(this@LoginActivity, "server error: $message", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Toast.makeText(this@LoginActivity, "로그인 실패 : $message", Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess() {
                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                // 사용자 정보를 가져오는 REST API 의 접속 token (access token) 을 받아오기
                val accesstoken:String? = NaverIdLoginSDK.getAccessToken()
                //Toast.makeText(this@LoginActivity, "token : $accesstoken", Toast.LENGTH_SHORT).show()

                // 사용자 정보 가져오는 네트워크 작업수행 (Retrofit library 이용)
                val retrofit:Retrofit =RetrofitHelper.getRetrofitInstance("https://openapi.naver.com")
                retrofit.create(RetrofitApiService::class.java).getNidUserInfo("Bearer $accesstoken").enqueue(
                    object : Callback<NaverUserInfoResponse> {
                        override fun onResponse(
                            call: Call<NaverUserInfoResponse>,
                            response: Response<NaverUserInfoResponse>
                        ) {
                            val userInfo: NaverUserInfoResponse? =response.body()
                            var id:String= userInfo?.response?.id ?: ""
                            var email:String = userInfo?.response?.email ?: ""
                            Toast.makeText(this@LoginActivity, "$email", Toast.LENGTH_SHORT).show()
                            G.userAccount= UserAccount(id, email)

                            // 메인 화면으로 이동
                            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                            finish()
                        }

                        override fun onFailure(call: Call<NaverUserInfoResponse>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "회원정보 읽기 실패 : ${t.message}", Toast.LENGTH_SHORT).show()
                        }

                    })
            }
        })

    }

}

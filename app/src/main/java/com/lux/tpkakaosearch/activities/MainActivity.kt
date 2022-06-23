package com.lux.tpkakaosearch.activities

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.lux.tpkakaosearch.R
import com.lux.tpkakaosearch.databinding.ActivityMainBinding
import com.lux.tpkakaosearch.fragments.PlaceListFragment
import com.lux.tpkakaosearch.fragments.PlaceMapFragment
import com.lux.tpkakaosearch.model.KakaoSearchPlaceResponse
import com.lux.tpkakaosearch.network.RetrofitApiService
import com.lux.tpkakaosearch.network.RetrofitHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    val binding:ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }


    // 1. 검색 장소 키워드
    var searchQuery:String ="화장실"  //앱 초기 검색어 - 내 주변 개방 화장실

    // 2. 현재 내 위치 정보 객체(위도,경도 정보를 멤버로 보유)
    var mylocation: Location?= null

    // 3. Kakao 검색 결과 응답 객체 : ListFragment, MapFragment 모두 이 정보를 사용하기에
    var searchPlaceResponse:KakaoSearchPlaceResponse?= null

    
    // [Google Fused Location API 사용 : play-services-location ]
    val providerClient:FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바를 제목줄로 설정
        setSupportActionBar(binding.toolbar)

        //첫 실행될 프레그먼트를 동적으로 추가(첫 시작은 무조건 List 형식으로)
        supportFragmentManager.beginTransaction().add(R.id.container_fragment, PlaceListFragment()).commit()

        //탭레이아웃의 탭버튼 클릭시에 보여줄 프레그먼트 변경
        binding.layoutTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if( tab?.text == "LIST" ){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, PlaceListFragment()).commit()
                }else if( tab?.text == "MAP"){
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, PlaceMapFragment()).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        } )

        //검색어 입력에 따라 장소검색요청하기
        binding.etSearch.setOnEditorActionListener { textView, i, keyEvent ->
            searchQuery = binding.etSearch.text.toString()
            //카카오 장소검색 API 작업 요청
            searchPlaces()

            false
        }

        // 특정 키워드 단축 choice 버튼들에 리스너 처리하는 함수 호출
        setChoiceButtonsListener()
        
        // 내 위치 정보 제공은 동적 퍼미션 필요
        val permissions:Array<String> = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (checkSelfPermission(permissions[0])== PackageManager.PERMISSION_DENIED){
            // 퍼미션 요청 다이얼로그 보이기
            requestPermissions(permissions,10)
        }else{
            // 내 위치 탐색 요청하는 기능 호출
            requestMyLocation()
        }

    }//onCreate method...

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode==10 && grantResults[0]==PackageManager.PERMISSION_GRANTED) requestMyLocation()
        else Toast.makeText(this, "내 위치 정보를 제공하지 않아서 검색 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun requestMyLocation(){
        // 내 위치 정보 얻어오는 기능 코드

        // 위치 검색 기준 설정값 객체
        val request: LocationRequest = LocationRequest.create()
        request.interval = 1000
        request.priority=Priority.PRIORITY_HIGH_ACCURACY  //  높은 정확도 우선


        // 실시간 위치정보 갱신을 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        providerClient.requestLocationUpdates(request,locationCallback,Looper.getMainLooper())
    }

    // 위치정보 검색 결과 콜백객체
    private val locationCallback:LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            // 갱신된 위치 정보 결과 객체에게 위치정보 얻어오기
            mylocation=p0.lastLocation

            // 위치 탐색이 끝났으니 내 위치 정보 업데이트는 이제 그만 -> 종료
            providerClient.removeLocationUpdates(this)  // this -> locationCallback 객체

            // 내 위치 정보가 있으니 카카오 검색을 시작!
            searchPlaces()
        }
    }
    
    private fun setChoiceButtonsListener(){
        binding.layoutChoice.choiceWc.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceGas.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceEv.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceMovie.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choicePark.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceFood.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choice1.setOnClickListener { clickChoice(it) }
        binding.layoutChoice.choiceHospital.setOnClickListener { clickChoice(it) }
    }

    //멤버변수로 기존 선택된 뷰의 id를 저장하는 변수
    var choiceID= R.id.choice_wc

    private fun clickChoice(view: View){

        //기존 선택된 뷰의 배경 이미지를 변경
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.background_choice)

        //현재 선택된 뷰의 배경 이미지를 변경
        view.setBackgroundResource(R.drawable.background_choice_selected)

        //현재 선택한 뷰의 id를 멤버변수에 저장
        choiceID= view.id

        //초이스한 것에 따라 검색장소 키워드를 변경하여 다시 요청
        when(choiceID){
            R.id.choice_wc-> searchQuery="화장실"
            R.id.choice_gas-> searchQuery="주유소"
            R.id.choice_ev-> searchQuery="전기차충전소"
            R.id.choice_movie-> searchQuery="영화관"
            R.id.choice_park-> searchQuery="공원"
            R.id.choice_food-> searchQuery="맛집"
            R.id.choice_1-> searchQuery="약국"
            R.id.choice_hospital ->searchQuery="병원"
        }
        //새로운 검색 요청
        searchPlaces()

        //검색창의 글씨가 있다면 지우기.
        binding.etSearch.text.clear()
        binding.etSearch.clearFocus()//이전 포커스로 인해 커서가 남아있을 수 있어서
    }
    //카카오 키워드 장소검색 API 작업을 수행하는 기능메소드
    private fun searchPlaces(){
        //Toast.makeText(this, "$searchQuery : ${mylocation?.latitude}, ${mylocation?.longitude}", Toast.LENGTH_SHORT).show()
        binding.progressBar.visibility=View.VISIBLE

        //레트로핏을 이용하여 카카오 키워드 장소검색 API를 파싱하기
        val retrofit: Retrofit =RetrofitHelper.getRetrofitInstance("https://dapi.kakao.com")
//        retrofit.create(RetrofitApiService::class.java)
//            .searchPlacesToString(searchQuery, mylocation?.longitude.toString(), mylocation?.latitude.toString())
//            .enqueue(object : Callback<String> {
//                override fun onResponse(call: Call<String>, response: Response<String>) {
//                    var s=response.body()
//                    AlertDialog.Builder(this@MainActivity).setMessage(s.toString()).create().show()
//                }
//
//                override fun onFailure(call: Call<String>, t: Throwable) {
//                    Toast.makeText(this@MainActivity, "서버 오류가 있습니다. \n잠시 뒤에 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
//                }
//
//            })

        retrofit.create(RetrofitApiService::class.java)
            .searchPlaces(searchQuery,mylocation?.longitude.toString(), mylocation?.latitude.toString())
            .enqueue(object : Callback<KakaoSearchPlaceResponse> {
                override fun onResponse(
                    call: Call<KakaoSearchPlaceResponse>,
                    response: Response<KakaoSearchPlaceResponse>
                ) {
                    searchPlaceResponse=response.body()

                    binding.progressBar.visibility=View.GONE

                    // 우선 객체가 잘 파싱되었는지 확인
                    //AlertDialog.Builder(this@MainActivity).setMessage(searchPlaceResponse?.documents!!.size.toString()).show()

                    // 무조건 검색이 완료되면 PlaceListFragment 부터 보여주기
                    supportFragmentManager.beginTransaction().replace(R.id.container_fragment, PlaceListFragment()).commit()

                    // 탭버튼의 위치를 'List' 탭으로 변경
                    binding.layoutTab.getTabAt(0)?.select()
                }

                override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "서버 오류가 있습니다. \n잠시 뒤에 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                }

            })
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

}//MainActivity class...
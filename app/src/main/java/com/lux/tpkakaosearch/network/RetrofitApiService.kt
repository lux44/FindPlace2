package com.lux.tpkakaosearch.network

import com.lux.tpkakaosearch.model.KakaoSearchPlaceResponse
import com.lux.tpkakaosearch.model.NaverUserInfoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitApiService {

    // 네아로 사용자 정보 API
    @GET("/v1/nid/me")
    fun getNidUserInfo(@Header("Authorization") authorization:String): Call<NaverUserInfoResponse>

    // 카카오 키워드 장소 검색 API - 결과를 json 파싱한 KakaoSearchPlaceResponse 로..
    @Headers("Authorization: KakaoAK d347be8010e5b1804c760417d9baf080")
    @GET("/v2/local/search/keyword.json")
    fun searchPlaces(@Query("query") query: String, @Query("x") longitude:String, @Query("y") latitude:String):Call<KakaoSearchPlaceResponse>

    // 카카오 키워드 장소 검색 API - 결과를 String 으로..
    @Headers("Authorization: KakaoAK d347be8010e5b1804c760417d9baf080")
    @GET("/v2/local/search/keyword.json")
    fun searchPlacesToString(@Query("query") query: String,
                             @Query("x") longitude:String,
                             @Query("y") latitude:String):Call<String>
}
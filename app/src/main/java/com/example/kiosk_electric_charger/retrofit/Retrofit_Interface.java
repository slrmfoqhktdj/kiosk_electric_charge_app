package com.example.kiosk_electric_charger.retrofit;

import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_ADModel;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_PayModel;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_VersionModel;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Retrofit_Interface {

    /// 결제

    // 전기 시세 받아오기
    @POST("api/pay")
    Call<Retrofit_PayModel> getPay(
//            @Query("Pay") int pay
    );

    /// 광고
    
//광고 리스트 받아오기
    @FormUrlEncoded
    @POST("api/ad")
    Call<List<Retrofit_ADModel>> getAdList(
            @FieldMap HashMap<String,String> param
            );

//광고 파일 다운
    @FormUrlEncoded
    @POST("file/ad")
    Call<ResponseBody> downAd(
            @FieldMap HashMap<String,String> param
    );

    /// 업데이트

    //버전 확인
    @POST("api/apk/version")
    Call<Retrofit_VersionModel> getApkVersion();

    //apk 파일 다운
    @FormUrlEncoded
    @POST("file/apk/operation/1")
    Call<ResponseBody> downApk(
            @FieldMap HashMap<String,String> param
    );

    //앱 업데이트 확인
    @FormUrlEncoded
    @POST("file/apk/consequnce")
    Call<ResponseBody> updateComplete(
            @FieldMap HashMap<String,String> param
    );

    //결제내역 보내기
    @FormUrlEncoded
    @POST("pay/history")
    Call<ResponseBody> sendPayHistory(
            @FieldMap HashMap<String,String> param
    );

    //충전 시작시간 보내기
    @FormUrlEncoded
    @POST("charging/start")
    Call<ResponseBody> sendCStart(
            @FieldMap HashMap<String,String> param
    );

    //충전 종료시간 보내기
    @FormUrlEncoded
    @POST("charging/end")
    Call<ResponseBody> sendCEnd(
            @FieldMap HashMap<String,String> param
    );

}


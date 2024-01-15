package com.example.kiosk_electric_charger.retrofit;

import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_ADModel;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_PayModel;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_VersionModel;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Retrofit_Interface_Weather {



//    @GET("api/{device_id}")
    @HTTP(method = "GET",path="api/{device_id}",hasBody = true)
    @Headers({"Content-Type: application/json"})
    Call<ResponseBody> GetWhether(
            @Path("device_id") String device_id,
//            @Query("timestamp") String timestamp,
//            @Query("ip") String ip,
//            @Query("temp") String temp,
//            @Query("humidity") String humidity,
//            @Query("ws") String ws,
//            @Query("wd") String wd,
//            @Query("north_direction") String north_direction,
//            @Query("atmospheric_pressure") String atmospheric_pressure,
//            @Query("rainfall") String rainfall,
//            @Query("voltage") String voltage,

//            @FieldMap HashMap<String,String> param
            @Body JsonObject body
    );

}


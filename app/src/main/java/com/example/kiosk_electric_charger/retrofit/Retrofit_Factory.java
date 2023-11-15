package com.example.kiosk_electric_charger.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit_Factory {
    private static final String BASE_URL = "http://192.168.0.7:8080/";

    public static Retrofit_Interface create() {

        // Retrofit 객체 생성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(Retrofit_Interface.class);
    }
}

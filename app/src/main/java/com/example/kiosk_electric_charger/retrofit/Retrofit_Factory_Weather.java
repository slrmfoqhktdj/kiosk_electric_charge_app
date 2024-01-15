package com.example.kiosk_electric_charger.retrofit;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit_Factory_Weather {
    private static final String WHETHER_BASE_URL = "http://54.180.106.155:4465/";

    private static final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();


    public static Retrofit_Interface_Weather create() {

//        Retrofit retrofit;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(loggingInterceptor);

        return new Retrofit.Builder()
                .baseUrl(WHETHER_BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

                .create(Retrofit_Interface_Weather.class);

    }
}

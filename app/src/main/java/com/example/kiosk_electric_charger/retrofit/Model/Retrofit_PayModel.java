package com.example.kiosk_electric_charger.retrofit.Model;

import com.google.gson.annotations.SerializedName;


public class Retrofit_PayModel {

//    @SerializedName("Pay")
    private float Pay;

    public float getPay(){
        return Pay;
    }

    public void setPay(float pay){
        this.Pay = pay;
    }
}
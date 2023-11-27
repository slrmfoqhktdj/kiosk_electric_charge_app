package com.example.kiosk_electric_charger.retrofit.Model;

import com.google.gson.annotations.SerializedName;


public class Retrofit_PayModel {

//    @SerializedName("Pay")
    private  int Pay;

    public int getPay(){
        return Pay;
    }

    public void setPay(int pay){
        this.Pay = pay;
    }
}
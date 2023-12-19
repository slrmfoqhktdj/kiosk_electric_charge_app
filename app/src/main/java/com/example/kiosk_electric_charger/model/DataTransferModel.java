package com.example.kiosk_electric_charger.model;

public class DataTransferModel {

    private static final String adlist = "adlist";      // 광고리스트 받아오기

    private static final String pay = "i_pay";        // 결제 정보 전달

    public static String getAdlist(){
        return adlist;
    }

    public static String getPay(){return pay;}
}

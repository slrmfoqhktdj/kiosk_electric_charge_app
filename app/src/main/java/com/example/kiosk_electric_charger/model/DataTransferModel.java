package com.example.kiosk_electric_charger.model;

public class DataTransferModel {

    private static final String adlist = "adlist";      // 광고리스트 받아오기

    private static final String i_pay = "i_pay";        // 결제 정보 전달
    
    private static final String g_pay = "g_pay";        // 전기 시세 가져오기

    public static String getAdlist(){
        return adlist;
    }

    public static String getI_Pay(){return i_pay;}     

    public static String getG_Pay(){return g_pay;}
}

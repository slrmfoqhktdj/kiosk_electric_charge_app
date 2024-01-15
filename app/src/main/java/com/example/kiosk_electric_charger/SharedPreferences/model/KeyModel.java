package com.example.kiosk_electric_charger.SharedPreferences.model;

public class KeyModel {
    private static final String LeftChargingEnabled = "LeftChargingEnabled";    //왼쪽 커넥터 이용가능
    private static final String RightChargingEnabled = "RightChargingEnabled";  //오른쪽 커넥터 이용가능

    public static String getLeftChargingEnabled(){
        return LeftChargingEnabled;
    }

    public static String getRightChargingEnabled(){return RightChargingEnabled;}
}

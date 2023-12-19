package com.example.kiosk_electric_charger.model;

import android.provider.Settings;

public class DeviceModel {
    private static final String Deviceid = "device_01";

    private static final String Devicevendor = "IF";
//    private static final String Devicelatitude = "35.187726962762";
//    private static final String Devicelongitude = "126.91088663129";
//    private static final String DeviceAddr = "광주광역시북구오치동";


    public static String getDeviceid(){
        return Deviceid;
    }

    public static String getDevicevendor(){return Devicevendor;}
}

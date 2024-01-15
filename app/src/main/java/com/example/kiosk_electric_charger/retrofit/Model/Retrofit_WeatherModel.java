package com.example.kiosk_electric_charger.retrofit.Model;

public class Retrofit_WeatherModel {

    public Retrofit_WeatherModel(String timestamp, String ip, String temp, String humidity, String ws, String wd, String north_direction, String atmospheric_pressure, String rainfall, String voltage) {
        this.timestamp = timestamp;
        this.ip = ip;
        this.temp = temp;
        this.humidity = humidity;
        this.ws = ws;
        this.wd = wd;
        this.north_direction = north_direction;
        this.atmospheric_pressure = atmospheric_pressure;
        this.rainfall = rainfall;
        this.voltage = voltage;
    }

    private  String timestamp;  // 시간
    private  String ip; // 충전기 기계 ip
    private  String temp;   // 온도
    private  String humidity;   //습기
    private  String ws; //
    private  String wd; //
    private  String north_direction;    //
    private  String atmospheric_pressure;   // 기압
    private  String rainfall;   // 강우
    private  String voltage;    //


    public String getTimestamp() {
        return timestamp;
    }

    public String getIp() {
        return ip;
    }

    public String getTemp() {
        return temp;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getWs() {
        return ws;
    }

    public String getWd() {
        return wd;
    }

    public String getNorth_direction() {
        return north_direction;
    }

    public String getAtmospheric_pressure() {
        return atmospheric_pressure;
    }

    public String getRainfall() {
        return rainfall;
    }

    public String getVoltage() {
        return voltage;
    }

}

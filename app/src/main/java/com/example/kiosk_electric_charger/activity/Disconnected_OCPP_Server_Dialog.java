package com.example.kiosk_electric_charger.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.R;

public class Disconnected_OCPP_Server_Dialog extends AppCompatActivity {

    boolean isRunning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnected_dialog);
        isRunning = true;
    }

    public boolean isOpend(){
        return isRunning;
    }

    public void isClosed(){
        isRunning = false;
        this.finish();
    }
}

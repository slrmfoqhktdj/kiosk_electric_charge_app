package com.example.kiosk_electric_charger.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.SecondActivity;
import com.example.kiosk_electric_charger.background.Backgound_service;

public class Charge_Complete_Activity extends AppCompatActivity {

    Backgound_service backgound_service;
    boolean isService = false;

    Intent service_intent;

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            Backgound_service.LocalBinder mb = (Backgound_service.LocalBinder) service;
            backgound_service = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 해제",
                    Toast.LENGTH_LONG).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge_complete);


        //서비스 시작
        service_intent = new Intent(
                Charge_Complete_Activity.this, // 현재 화면
                Backgound_service.class);

        bindService(service_intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);

        new Handler().postDelayed(() -> {
            backgound_service.Start_ad_thread();
        },1000);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isService){
            unbindService(conn); // 서비스 종료
            isService = false;
        }

    }

}

package com.example.kiosk_electric_charger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.activity.Charge_Conn_Activity;
import com.example.kiosk_electric_charger.activity.Expected_Amount_Activity;
import com.example.kiosk_electric_charger.background.Backgound_service;
import com.example.kiosk_electric_charger.receiver.ADReceiver;
import com.example.kiosk_electric_charger.receiver.LauncherReceiver;
import com.example.kiosk_electric_charger.receiver.UpdateReceiver;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Button main_btn ;

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


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("main","onCreate");

        main_btn = findViewById(R.id.main_next_btn);


        // 업데이트 알람 매니저 초기화
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);


        //서비스 시작
        service_intent = new Intent(
                MainActivity.this, // 현재 화면
                Backgound_service.class);

        startService(service_intent);

        bindService(service_intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);



        main_btn.setOnClickListener(v -> {
            backgound_service.Stop_ad_thread();
            Intent intent = new Intent(MainActivity.this, Charge_Conn_Activity.class);
            startActivity(intent);
        });



        // 업데이트 알람 등록
        Updateregist();






    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("main","onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //텀을 줘야함
        new Handler().postDelayed(() -> {
            // 광고 타이머 시작
            backgound_service.Start_ad_thread();
        },1000);

        Log.d("main","onResume");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrefsHelper.destroyPref();
        unregist();
        if(isService){
            unbindService(conn); // 서비스 종료
            stopService(service_intent);
            isService = false;
        }

    }

    // back 버튼 막기
    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    
    private AlarmManager alarmManager;

    public void Updateregist() {
        
        //FLAG_IMMUTABLE - 읽기 전용    -- 다른 어플에서 함부로 수정하지 못하게 하기위해
        //FLAG_CANCEL_CURRENT - 이미 pendingIntent가 활성화 되어있을때 이전것을 취소시키고 새로 만듦

        Intent intent = new Intent(this, UpdateReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0,intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        

        // 지정한 시간에 매일 알림
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),  AlarmManager.INTERVAL_DAY, pIntent);

    }// regist()..

    public void unregist() {
        Intent intent = new Intent(this, UpdateReceiver.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pIntent);
    }//




}
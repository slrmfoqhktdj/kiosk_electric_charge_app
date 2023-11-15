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
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
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

    private BroadcastReceiver mReceiver;

//    private LauncherReceiver lReceiver;


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        

        main_btn = findViewById(R.id.main_next_btn);

        //SharedPreferences 등록
        PrefsHelper.init(this);


        //광고 액티비티 띄우는 리시버 등록
        mReceiver = new ADReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ADReceiver.ADAction);
        this.registerReceiver(mReceiver,filter);

//        lReceiver = new LauncherReceiver();
//        IntentFilter filter1 = new IntentFilter();
//        filter.addAction(LauncherReceiver.START_INTENT);
////        filter.addDataScheme("package");
//        this.registerReceiver(lReceiver,filter1);

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
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);


            // 광고 타이머 시작
//            backgound_service.Start_ad_thread();


        });

        // 업데이트 알람 등록
        Updateregist();

        
        // 관리자 권한 부여 --- 안되는것같기도하고
        DevicePolicyManager dpm =
                (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        ComponentName componentName = new ComponentName(this, MainActivity.class);

        if(!dpm.isAdminActive(componentName)){
            Log.d("admin","No");
            enableAdmin(componentName);
        }

        // 소유자 권한 제거
//        dpm.clearDeviceOwnerApp(this.getPackageName());




    }
    private void enableAdmin(ComponentName componentName) {
//        if (isDeviceAdminApp()) {
//            return;
//        }

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        // Start the add device admin activity
        startActivity(intent);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrefsHelper.destroyPref();
        unregisterReceiver(mReceiver);
//        unregisterReceiver(lReceiver);
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
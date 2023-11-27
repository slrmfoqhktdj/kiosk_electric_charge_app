package com.example.kiosk_electric_charger.adview;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.MainActivity;
import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.SplashActivity;
import com.example.kiosk_electric_charger.adview.ImageVideoView;
import com.example.kiosk_electric_charger.background.Backgound_service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class AD_Activity extends AppCompatActivity {

    ImageVideoView imageVideoView;

    List<String> adlist = new ArrayList<>();

    int i = 0;

    private Handler mHandler = new Handler();



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
        setContentView(R.layout.activity_ad_show);

        imageVideoView = findViewById(R.id.imageVideoView);


        //서비스 시작
        service_intent = new Intent(
                AD_Activity.this, // 현재 화면
                Backgound_service.class);
        bindService(service_intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);


        //저장된 리스트 불러오기
        adlist = PrefsHelper.getStringArrayPref(PrefsHelper.ad_url_list_key);

        //최근까지본 광고의 다음 광고 불러오기
        i = PrefsHelper.read(PrefsHelper.recent_ad_count_key,0);

//        for(String a : PrefsHelper.getStringArrayPref("adurl")){
//            Log.d("adlist",a);
//        }


        imageVideoView.setOnClickListener(v -> {
            backgound_service.Stop_ad_thread();
            backgound_service.Start_ad_thread(0);
            finish();
        });



        // 처음 딜레이전에 한번 먼저 실행
        adstarter();

        // 딜레이 후 실행
        addelay();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(imageVideoView != null){
            imageVideoView.releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 핸들러 취소
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

        if(imageVideoView != null){
            imageVideoView.releasePlayer();
        }
        if(isService){
            unbindService(conn); // 바인드서비스 종료
            isService = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(imageVideoView != null){
            imageVideoView.StopPlayer();
        }
    }

    public void addelay(){
        long delaytime = 5000;
        if(imageVideoView.isVideo(adlist.get(i))){
            delaytime = imageVideoView.getVideoLengthToMili(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }

        Log.d("v_lenght", String.valueOf(delaytime));



        mHandler.postDelayed(() -> {
            addelay();
            adstarter();
        },5000);
    }


    public void adstarter(){
        imageVideoView.releasePlayer();

        if(imageVideoView.isVideo(adlist.get(i))){
            imageVideoView.setUrlVideo(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }else{
            imageVideoView.setUrlImage(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }


        if(i == adlist.size()-1){
            i = 0;
        }else{
            i++;
        }

        PrefsHelper.write(PrefsHelper.recent_ad_count_key,i);


    }

}

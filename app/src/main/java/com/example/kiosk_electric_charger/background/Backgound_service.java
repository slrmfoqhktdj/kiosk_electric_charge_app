package com.example.kiosk_electric_charger.background;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.kiosk_electric_charger.receiver.ADReceiver;

public class Backgound_service extends Service {

    private final IBinder mBinder = new LocalBinder();

    private final long AD_time = 5000;

    private final long To_Main_time = 5000;

    private Thread ad_thread;

    private Handler ad_handler;

    public class LocalBinder extends Binder {
        public Backgound_service getService() {
            return Backgound_service.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        super.onStartCommand(intent,flags,startId);

        ad_handler = new Handler(Looper.getMainLooper());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //inside on destroy method we are stopping our media player.
        Stop_ad_thread();

    }




    private void sendADReceiver(){
        Intent intent = new Intent(ADReceiver.ADAction);
        sendBroadcast(intent);
    }


    private void sendMainReceiver(){
        Intent intent = new Intent(ADReceiver.MPAction);
        sendBroadcast(intent);
        Log.d("background","sendMainReceiver");
        Stop_ad_thread();
    }

    public void Start_ad_thread(int type){


        if (type == 0) {    //메인 페이지일때 광고
            if (ad_thread == null) {
                ad_thread = new Thread(() -> ad_handler.postDelayed(() -> {
                    Log.d("background","Start_ad_thread 1");
                    sendADReceiver();
                }, AD_time));

                ad_thread.start();
            }
        } else {
            if (ad_thread == null) {
                ad_thread = new Thread(() -> ad_handler.postDelayed(() -> {
                    Log.d("background","Start_md_thread 1");
                    sendMainReceiver();

                }, To_Main_time));

                ad_thread.start();
            }
        }
    }




    public void Start_ad_thread(int type,long time){

        if (type == 0) {    //메인 페이지일때 광고
            if (ad_thread == null) {
                ad_thread = new Thread(() -> ad_handler.postDelayed(() -> {
                    sendADReceiver();
                },  time));

                ad_thread.start();
            }
        } else {
            if (ad_thread == null) {
                ad_thread = new Thread(() -> ad_handler.postDelayed(() -> {
                    sendMainReceiver();
//                    // 메인 이동 이후 일정 시간 후에 ad_activity 실행
//                    Start_ad_thread(0);
                },  time));

                ad_thread.start();
            }
        }
    }

    public void Stop_ad_thread() {
        if (ad_thread != null){
            ad_handler.removeCallbacksAndMessages(null);
            ad_thread.interrupt();
            ad_thread = null;
        }
    }

//    public void In_Touch_Event(){
//        Stop_ad_thread();
//        Start_ad_thread();
//    }






}

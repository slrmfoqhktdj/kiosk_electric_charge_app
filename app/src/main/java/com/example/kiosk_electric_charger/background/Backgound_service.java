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

    private final long backgound_time = 2000;

    private Thread ad_thread;

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
        


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //inside on destroy method we are stopping our media player.
        Stop_ad_thread();

    }




    private void sendReceiver(){
        Intent intent = new Intent(ADReceiver.ADAction);
        sendBroadcast(intent);

    }

    public void Start_ad_thread(){
        Log.d("start_ad_thread","1");
        if(ad_thread == null){
            ad_thread = new Thread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                sendReceiver();
            },backgound_time));

            ad_thread.start();
        }
    }

    public void Stop_ad_thread() {
        if (ad_thread != null){
            ad_thread.interrupt();
            ad_thread = null;
        }
    }

    public void In_Touch_Event(){
        Stop_ad_thread();
        Start_ad_thread();
    }






}

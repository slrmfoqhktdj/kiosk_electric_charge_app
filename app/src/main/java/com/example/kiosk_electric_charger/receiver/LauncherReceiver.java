package com.example.kiosk_electric_charger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.kiosk_electric_charger.MainActivity;
import com.example.kiosk_electric_charger.SplashActivity;

public class LauncherReceiver extends BroadcastReceiver {

    public static final String START_INTENT = "com.example.kiosk_electric_charger.action.START";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("receiver","CALL RECEIVER!!");
        Intent startIntent = new Intent(context, MainActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(startIntent);
    }
}
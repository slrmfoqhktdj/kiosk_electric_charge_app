package com.example.kiosk_electric_charger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver {

    public final static String UpdateAction = "Update.App.Application";

    @Override
    public void onReceive(Context context, Intent intent){
        Log.d("alarm","update receiver");
    }
}

package com.example.kiosk_electric_charger.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.kiosk_electric_charger.adview.AD_Activity;

import java.util.Objects;

public class ADReceiver extends BroadcastReceiver {
    public final static String ADAction = "Show.AD.Activity";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Log.d("Receive", "받음");

        if(Objects.equals(intent.getAction(), ADAction)){
            Log.d("Receive", "광고");
            Intent adactivity = new Intent(context, AD_Activity.class);
            context.startActivity(adactivity);
        }

    }
}
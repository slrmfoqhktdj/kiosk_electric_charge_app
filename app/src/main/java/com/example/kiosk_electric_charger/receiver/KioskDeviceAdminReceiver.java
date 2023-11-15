package com.example.kiosk_electric_charger.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class KioskDeviceAdminReceiver extends DeviceAdminReceiver {
    static final String TAG = "SIMPLE_KIOSK";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.getAction());
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "Device admin enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "Device admin disabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
        Log.i(TAG, "onLockTaskModeEntering: " + pkg);
    }
}
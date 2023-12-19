package com.example.kiosk_electric_charger.adview;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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


    
    Dialog ad_dialog;

    ImageButton ad_out_btn;

    ConstraintLayout ad_out_bg;


    private Thread ad_close_thread;
    private Handler ad_close_handler = new Handler();

    private final long Close_Ad_dialog_time = 5000;



    private AD_Controller adController;     // 광고 컨트롤러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_show);

        imageVideoView = findViewById(R.id.imageVideoView);

        adController = new AD_Controller(imageVideoView);


        imageVideoView.setOnClickListener(v -> {
            ShowDialog();
            Show_ad_thread();           // 일정시간후 다이어로그 닫히게
        });


        adController.Start_ad();

    }


    private void ShowDialog(){

        ad_dialog = new Dialog(AD_Activity.this);

        ad_dialog.setContentView(R.layout.dialog_ad_out);
        ad_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ad_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ad_dialog.setCancelable(false);

        ad_out_bg = ad_dialog.findViewById(R.id.ad_dialog_bg);
        ad_out_btn = ad_dialog.findViewById(R.id.ad_dialog_btn);

        ad_out_bg.setOnClickListener(v -> {
            Log.d("showdialog", "bg click");

            Close_ad_thread();
            Show_ad_thread();
        });


        ad_out_btn.setOnClickListener(v-> {
            AD_Activity.this.finish();                  // 광고 창 닫기
            ad_dialog.dismiss();                        // 창 닫기
        });

        ad_dialog.show();

    }


    @Override
    protected void onPause() {
        super.onPause();

        adController.Stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        adController.Close();
        adController = null;

        // 광고 클릭 다이어로그
        Close_ad_thread();
        ad_close_handler = null;
    }


    public void Show_ad_thread() {


        if (ad_close_thread == null) {
            ad_close_thread = new Thread(() -> ad_close_handler.postDelayed(() -> {
                if(ad_dialog != null){
                    ad_dialog.dismiss();
                }
                Close_ad_thread();
            }, Close_Ad_dialog_time));

            ad_close_thread.start();

        }
    }




    public void Close_ad_thread() {
        if (ad_close_thread != null){
            ad_close_thread.interrupt();
            ad_close_handler.removeCallbacksAndMessages(null);
            ad_close_thread = null;
            ad_dialog = null;
        }
    }

}

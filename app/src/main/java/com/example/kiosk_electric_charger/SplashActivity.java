package com.example.kiosk_electric_charger;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    VideoView svv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        svv = (VideoView) findViewById(R.id.splash_videoview);



        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/raw/splash_ex");
//        File path = new File(getExternalFilesDir(null),"lion.mp4");
//        Uri uri1 = Uri.fromFile(path);
//
        svv.setVideoURI(uri);


        svv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mp.stop();
                        Intent intent = new Intent(SplashActivity.this,FirstSettingActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },3000);
            }
        });





    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        svv.pause();
        svv.stopPlayback();
    }

}

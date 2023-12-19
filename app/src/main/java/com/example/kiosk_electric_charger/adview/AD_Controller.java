package com.example.kiosk_electric_charger.adview;

import android.os.Handler;
import android.util.Log;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AD_Controller {

    ImageVideoView imageVideoView;

    List<String> adlist;

    int i = 0;

    private Handler mHandler = new Handler();

    public AD_Controller(ImageVideoView imageVideoView){
        this.imageVideoView = imageVideoView;

        //저장된 리스트 불러오기
        adlist = PrefsHelper.getStringArrayPref(PrefsHelper.ad_url_list_key);


//        for(String a : PrefsHelper.getStringArrayPref("adurl")){
//            Log.d("adlist",a);
//        }

    }

    public void Start_ad(){

        //최근까지본 광고의 다음 광고 불러오기
        i = PrefsHelper.read(PrefsHelper.recent_ad_count_key,0);
        
        // 광고 실행
        
        imageVideoView.releasePlayer();

        if(imageVideoView.isVideo(adlist.get(i))){
            imageVideoView.setUrlVideo(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
            Log.d("v_lenght",i + " - " + "it's playing video");
        }else{
            imageVideoView.setUrlImage(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }


        // 현재 재생중인 광고 길이 가져와서 딜레이주기

        long delaytime = 5000;
        if(imageVideoView.isVideo(adlist.get(i))){
            delaytime = imageVideoView.getVideoLengthToMili(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }

        Log.d("v_lenght",i + " - " + String.valueOf(delaytime));


        // 다음 광고 대기
        
        if(i == adlist.size()-1){
            i = 0;
        }else{
            i++;
        }
        Log.d("v_lenght",i + " - " + "save");
        PrefsHelper.write(PrefsHelper.recent_ad_count_key,i);


        // 반복

        mHandler.postDelayed(this::Start_ad,delaytime);


    }




    public void Stop(){
        if(imageVideoView != null){
            imageVideoView.releasePlayer();
            mHandler.removeCallbacksAndMessages(null);
        }
    }


    public void Close(){
        // 핸들러 취소
        Stop();
        mHandler = null;
    }
}

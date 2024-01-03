package com.example.kiosk_electric_charger;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.filemanage.Filedownload;
import com.example.kiosk_electric_charger.model.DataTransferModel;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.receiver.KioskDeviceAdminReceiver;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_ADModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;
import com.example.kiosk_electric_charger.ws.JsonClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import eu.chargetime.ocpp.model.core.RegistrationStatus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    VideoView svv;

    private final Retrofit_Interface retrofitService = Retrofit_Factory.create();

    int downlistlenght = 0;
    int downcomplete = 0;
    
    boolean bootnotification_b = false; // 부트노티 승인


    boolean down_setting_com = false;
    boolean video_time_com = false;

    //영상길이
    long video_time = 7000;

    String tag = "SplashActivity";

    JsonClient mClient;


    // 충전시간
    private final BroadcastReceiver bootNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int interval = intent.getIntExtra("interval",60);
            bootnotification_b = intent.getBooleanExtra("status",false);

            Log.d(tag,"boot notification - "+interval + " "+ bootnotification_b);

//            mClient.disconnect();

//            sendFileList();

            request_AD_list();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //SharedPreferences 등록
        PrefsHelper.init(this);

        //ocpp bootnotification 결과 알림
        IntentFilter sfilter01 = new IntentFilter();
        sfilter01.addAction("BootNotificationBC");
        registerReceiver(bootNotificationReceiver,sfilter01);

        svv = (VideoView) findViewById(R.id.splash_videoview);



        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.igen2);



        // 인트로 영상 길이 받아오기

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(this, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            video_time = Long.parseLong(time );

        try {
            retriever.release();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(tag, "video time = "+ video_time);

        svv.setVideoURI(uri);


        svv.setOnPreparedListener(mp -> {
            mp.start();

            new Handler().postDelayed(() -> {
                Log.d(tag, "start handler");
//                mp.stop();

                video_time_com = true;

                // 다운로드가 다 완료됬다면
                if(down_setting_com){
                    nextActivity();
                }

            },video_time);


        });








        // 관리자 권한 부여 --- 안되는것같기도하고
        DevicePolicyManager dpm =
                (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        ComponentName componentName = new ComponentName(this, KioskDeviceAdminReceiver.class);

        if(!dpm.isAdminActive(componentName)){
            Log.d(tag,"admin No");
            enableAdmin(componentName);
        }

        // 소유자 관리자권한 제거
//        devicePolicyManager.removeActiveAdmin(componentName);

        // 소유자 권한 제거
//        dpm.clearDeviceOwnerApp(this.getPackageName());










        sendBootNotification();



    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        svv.pause();
        svv.stopPlayback();
        unregisterReceiver(bootNotificationReceiver);    // 부팅

//        mClient.disconnect();           // ws 연결 끊고
        mClient = null;
        
    }


    private void sendBootNotification(){



        new Thread(()->{
            mClient = JsonClient.getInstance(getApplicationContext());
            try {
                mClient.connect();
//                Log.d(tag,mClient.sendBootNotification());
                mClient.sendBootNotification();
//                mClient.disconnect();


            } catch (Exception e) {
                e.printStackTrace();
            }


        }).start();
    }


//    private void sendFileList(){
//        try {
//            mClient.sendDataTransfer(DeviceModel.getDeviceid(), DataTransferModel.getAdlist(),"");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }


    public void request_AD_list(){
        HashMap<String,String> input = new HashMap<>();
        input.put("device_id", DeviceModel.getDeviceid());

        String file_path_String = PrefsHelper.ad_path;


        //폴더 없으면 생성
        if(!new File(file_path_String).exists()){
            Log.d(tag,"folder make "+  String.valueOf(new File(file_path_String).mkdirs()));
        }

        // 해당 경로에 있는 파일 리스트
        File file_path = new File(file_path_String);

        List<String> f_list = Arrays.asList(file_path.list());


//        for( String l : f_list){
//            Log.d(tag, "f_list "+ l);
//        }





        retrofitService.getAdList(input).enqueue(new Callback<List<Retrofit_ADModel>>() {
            @Override
            public void onResponse(Call<List<Retrofit_ADModel>> call, Response<List<Retrofit_ADModel>> response) {
                if(response.isSuccessful()){

                    //받아온 광고 리스트 화
                    List<String> ad_list = new ArrayList<>();

                    try{
                        //받아온 리스트
//                    for(Retrofit_ADModel retrofitadModel : response.body()){
                        for(int s = 0; s < response.body().size()-1 ; s++){

                            Retrofit_ADModel retrofitadModel = response.body().get(s);


                            //받아온 리스트 스트링으로 담기
                            ad_list.add(retrofitadModel.getADid());

                            // 해당 기기에 파일이 있는지 확인
                            boolean r = f_list.contains(retrofitadModel.getADid());

                            //없으면 다운로드
                            if(!r){
                                down_AD_file(retrofitadModel.getADid());

                                //다운 리스트 길이
                                downlistlenght ++;

                            }

                            Log.d(tag,"success " + retrofitadModel.getADid()+ " "+ r);


                        }
                    }catch (Exception e){           // 배당 받은 광고가 없을 경우
                        e.printStackTrace();

                        // 해당 기기에 파일이 있는지 확인
                        boolean r = f_list.contains("adex01.mp4");      // 기본광고

                        //없으면 다운로드
                        if(!r){
                            down_AD_file("adex01.mp4");     // 기본광고

                            //다운 리스트 길이
                            downlistlenght ++;

                        }
                    }



                    // 그외 다른 파일 삭제
                    extra_file_remove(f_list, ad_list);

                    //sharedPreferences 에 저장
                    PrefsHelper.setStringArrayPref(PrefsHelper.ad_url_list_key,ad_list);
                    PrefsHelper.write(PrefsHelper.recent_ad_count_key,0);


                    //새로운 광고 다운로드 없을때
                    if(downlistlenght == 0){
                        down_setting_com = true;

                        // 영상 길이가 이미 끝난 시점에서 다운이 다 됬을때 넘어가기
                        if(video_time_com){
                            nextActivity();
                        }
                    }

                }

            }
            @Override
            public void onFailure(Call<List<Retrofit_ADModel>> call, Throwable t) {
                Log.e(tag,"get list fail " + t.getMessage());
            }
        });
    }

    public void down_AD_file(String fileName){
        HashMap<String,String> input = new HashMap<>();
        input.put("ad_id",fileName);


        retrofitService.downAd(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(Filedownload.writeResponseBodyToDisk(response.body(),PrefsHelper.ad_path + File.separator+ fileName)){
                    downcomplete ++;

                    Log.d(tag,"download com " + downcomplete + "/"+downlistlenght);
                    if(downcomplete == downlistlenght){
//                        nextActivity();

                        down_setting_com = true;

                        // 영상 길이가 이미 끝난 시점에서 다운이 다 됬을때 넘어가기
                        if(video_time_com){
                            nextActivity();
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(tag, "download ad fail " + t.getMessage());
            }
        });
    }



    private void extra_file_remove(List<String> filelist, List<String> adlist){
        // 파일 리스트 중 필요없는(공통되지 않는) 파일을 리스트화
        List<String> result = filelist.stream().filter(x -> !adlist.contains(x)).collect(Collectors.toList());
        for(String r : result){
            // 해당 경로 파일
            File file_path = new File(PrefsHelper.ad_path+ File.separator + r);

            //파일 있으면 삭제
            if(file_path.exists()){
                file_path.delete();
                Log.d(tag,"delete file " +  r);
            }
        }
    }


    private void nextActivity(){
        Log.d(tag, "go to CenterActivity");

        if(bootnotification_b){
            Intent intent = new Intent(SplashActivity.this, CenterActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this,"서버와의 통신이 되질 않습니다.",Toast.LENGTH_LONG).show();
        }

        finish();
    }


    private void enableAdmin(ComponentName componentName) {
//        if (isDeviceAdminApp()) {
//            return;
//        }

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        // Start the add device admin activity
        startActivity(intent);
    }

}

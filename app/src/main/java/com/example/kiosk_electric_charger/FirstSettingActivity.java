package com.example.kiosk_electric_charger;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.filemanage.Filedownload;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_ADModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstSettingActivity extends AppCompatActivity {

    private final Retrofit_Interface retrofitService = Retrofit_Factory.create();

//    ProgressBar progressBar;
//
//    int progress_max = 0;
    
    int downlistlenght = 0;
    int downcomplete = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_setting);

//        progressBar = findViewById(R.id.first_processbar);



        // 관리자 권한 부여 --- 안되는것같기도하고
        DevicePolicyManager dpm =
                (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        ComponentName componentName = new ComponentName(this, MainActivity.class);

        if(!dpm.isAdminActive(componentName)){
            Log.d("admin","No");
            enableAdmin(componentName);
        }

        // 소유자 권한 제거
//        dpm.clearDeviceOwnerApp(this.getPackageName());








        //SharedPreferences 등록
        PrefsHelper.init(this);



        HashMap<String,String> input = new HashMap<>();
        input.put("device_id", DeviceModel.getDeviceid());

        String file_path_String = PrefsHelper.ad_path;


        //폴더 없으면 생성
        if(!new File(file_path_String).exists()){
            Log.d("folder make ", String.valueOf(new File(file_path_String).mkdirs()));
        }

        // 해당 경로에 있는 파일 리스트
        File file_path = new File(file_path_String);

        List<String> f_list = Arrays.asList(file_path.list());


//            for(String a : f_list){
//                Log.d("list",a);
//            }




        retrofitService.getAdList(input).enqueue(new Callback<List<Retrofit_ADModel>>() {
            @Override
            public void onResponse(Call<List<Retrofit_ADModel>> call, Response<List<Retrofit_ADModel>> response) {
                if(response.isSuccessful()){




                    //받아온 광고 리스트 화
                    List<String> ad_list = new ArrayList<>();

                    //받아온 리스트
                    for(Retrofit_ADModel retrofitadModel : response.body()){

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

                        Log.d("success",retrofitadModel.getADid()+ " "+ r);

                    }

                    // 그외 다른 파일 삭제
                    extra_file_remove(f_list, ad_list);

                    //sharedPreferences 에 저장
                    PrefsHelper.setStringArrayPref(PrefsHelper.ad_url_list_key,ad_list);
                    PrefsHelper.write(PrefsHelper.recent_ad_count_key,0);


                    if(downlistlenght == 0){
                        nextActivity();
                    }

                }

            }
            @Override
            public void onFailure(Call<List<Retrofit_ADModel>> call, Throwable t) {
                Log.e("get list fail",t.getMessage());
            }
        });


    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }


    private void down_AD_file(String fileName){
        HashMap<String,String> input = new HashMap<>();
        input.put("ad_id",fileName);


        retrofitService.downAd(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if(Filedownload.writeResponseBodyToDisk(response.body(),PrefsHelper.ad_path + File.separator+ fileName)){
                        downcomplete ++;

                        if(downcomplete == downlistlenght){
                            nextActivity();
                        }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("download ad fail",t.getMessage());
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
                Log.d("delete file", r);
            }
        }
    }


    private void nextActivity(){
        Intent intent = new Intent(FirstSettingActivity.this, MainActivity.class);
        startActivity(intent);
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

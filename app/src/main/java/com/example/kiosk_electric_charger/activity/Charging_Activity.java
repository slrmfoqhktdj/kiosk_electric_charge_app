package com.example.kiosk_electric_charger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.adview.ImageVideoView;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Charging_Activity extends AppCompatActivity {

    private String pay_num;
    private String amount;
    private String time;

    private TextView expected_time_tv, expected_amount_tv;

    private ImageVideoView imageVideoView;

    private final Retrofit_Interface retrofitService = Retrofit_Factory.create();

    private Handler mHandler = new Handler();

    private List<String> adlist = new ArrayList<>();

    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging);


        expected_time_tv = findViewById(R.id.expected_time_txt);
        expected_amount_tv = findViewById(R.id.expected_amount_txt);
        imageVideoView = findViewById(R.id.charging_ad_view);

        //저장된 광고 리스트 불러오기
        adlist = PrefsHelper.getStringArrayPref(PrefsHelper.ad_url_list_key);
        //최근까지본 광고의 다음 광고 불러오기
        i = PrefsHelper.read(PrefsHelper.recent_ad_count_key,0);


        // 처음 딜레이전에 한번 먼저 실행
        adstarter();

        // 딜레이 후 실행
        addelay();


        // 값 받아오기
        Intent getintent = getIntent();
        pay_num = getintent.getExtras().getString("pay_num");       // 결제승인번호
        time = getintent.getExtras().getString("charging_time");  // 충전 걸리는 시간 량
        amount = getintent.getExtras().getString("charge_amount");    // 충전 량 제한


        //늘어나야함
        expected_amount_tv.setText(amount);
        
        //줄어들어야함
        expected_time_tv.setText(time);


        //충전 시작 서버 보냄
        Start_Charging_Rtrofit();


        new Handler().postDelayed(() -> {
            //충전 완료 서버 보냄
            End_Charging_Rtrofit();

            Intent intent = new Intent(Charging_Activity.this, Charge_Complete_Activity.class);
            startActivity(intent);
            finish();
        },2500);


    }

    //충전 시작, 종료 시간 서버로 보내기
    private String returnTime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        return sdf.format(date);
    }


    //충전 시작 통신
    private void Start_Charging_Rtrofit(){
        HashMap<String,String> input = new HashMap<>();
        input.put("Pay_num", pay_num);  // 결제 승인 번호
        input.put("C_time_s", returnTime());  // 충전시작시간
        retrofitService.sendCStart(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // 성공일때
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //서버로 안보내졌을때
            }
        });
    }

    //충전 끝 통신
    private void End_Charging_Rtrofit(){
        HashMap<String,String> input = new HashMap<>();
        input.put("Pay_num", pay_num);  // 결제 승인 번호
        input.put("C_time_e", returnTime());  // 충전종료시간
        retrofitService.sendCEnd(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // 성공일때
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //서버로 안보내졌을때
            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        if(imageVideoView != null){
            imageVideoView.releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 핸들러 취소
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;

        if(imageVideoView != null){
            imageVideoView.releasePlayer();
        }

//        backgound_service.Stop_ad_thread();
//        if(isService){
//            unbindService(conn); // 바인드서비스 종료
//            isService = false;
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(imageVideoView != null){
            imageVideoView.StopPlayer();
        }
    }

    public void addelay(){
        long delaytime = 5000;
        if(imageVideoView.isVideo(adlist.get(i))){
            delaytime = imageVideoView.getVideoLengthToMili(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }

        Log.d("v_lenght", String.valueOf(delaytime));



        mHandler.postDelayed(() -> {
            addelay();
            adstarter();
        },5000);
    }


    public void adstarter(){
        imageVideoView.releasePlayer();

        if(imageVideoView.isVideo(adlist.get(i))){
            imageVideoView.setUrlVideo(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }else{
            imageVideoView.setUrlImage(PrefsHelper.ad_path+ File.separator+ adlist.get(i));
        }


        if(i == adlist.size()-1){
            i = 0;
        }else{
            i++;
        }

        PrefsHelper.write(PrefsHelper.recent_ad_count_key,i);


    }
}

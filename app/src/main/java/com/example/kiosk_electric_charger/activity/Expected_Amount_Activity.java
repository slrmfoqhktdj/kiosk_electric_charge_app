package com.example.kiosk_electric_charger.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.background.Backgound_service;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_PayModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Expected_Amount_Activity extends AppCompatActivity {

    private TextView expected_pay,expected_time;
    private Button expected_next_btn;

    // 금액
    private String pay = "1003";
    // 충전량(kw)
    private String amount = "100";
    // 예상시간
    private String time = "50";

    private final Retrofit_Interface retrofitService = Retrofit_Factory.create();

    Backgound_service backgound_service;
    boolean isService = false;

    Intent service_intent;


    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            Backgound_service.LocalBinder mb = (Backgound_service.LocalBinder) service;
            backgound_service = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 해제",
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expected_amount);

        //서비스 시작
        service_intent = new Intent(
                Expected_Amount_Activity.this, // 현재 화면
                Backgound_service.class);

        bindService(service_intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);
        
        
        

        expected_pay = findViewById(R.id.expected_pay_txt);
        expected_time = findViewById(R.id.expected_time_txt);
        expected_next_btn = findViewById(R.id.expected_next_btn);


        //서버에서 전기값 가져오기
        retrofitService.getPay().enqueue(new Callback<Retrofit_PayModel>() {
            @Override
            public void onResponse(Call<Retrofit_PayModel> call, Response<Retrofit_PayModel> response) {
                if(response.isSuccessful()){
                    Retrofit_PayModel retrofitPayModel = response.body();
                    Log.d("success",String.valueOf(retrofitPayModel.getPay()));

                    // 결제값 계산후 반영
                    pay = String.valueOf(Integer.parseInt(String.valueOf(retrofitPayModel.getPay()))*Integer.parseInt(amount));
                    expected_pay.setText(pay);


                }

            }

            @Override
            public void onFailure(Call<Retrofit_PayModel> call, Throwable t) {
                Log.d("fail",t.getMessage());
            }
        });


        // 예상 시간 반영
        expected_time.setText(time);

        expected_next_btn.setOnClickListener(v -> {
            backgound_service.Stop_ad_thread();

            Intent intent = new Intent(Expected_Amount_Activity.this, Installments_Setting_Activity.class);
            intent.putExtra("pay",pay);
            intent.putExtra("amount",amount);
            intent.putExtra("time",time);
            startActivity(intent);
            finish();
        });


        //bindservice와 틈을 주기 위해
        new Handler().postDelayed(() -> {
            // 광고 타이머 시작
            backgound_service.Start_ad_thread(1);
        },1000);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Expected Amount","finish");
        backgound_service.Stop_ad_thread();
        if(isService){
            unbindService(conn); // 서비스 종료
            isService = false;
        }

    }

}

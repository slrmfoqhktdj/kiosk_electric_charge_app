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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.background.Backgound_service;

public class Installments_Setting_Activity extends AppCompatActivity {

    private RadioGroup inst_rg;
    private RadioButton inst_rb_01,inst_rb_02,inst_rb_03,inst_rb_06,inst_rb_09,inst_rb_12;

    private Button inst_next_btn;

    private String Inst_result = "00";

    private String pay;
    private String amount ;
    private String time;



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
        setContentView(R.layout.activity_installments_setting);



        //서비스 시작
        service_intent = new Intent(
                Installments_Setting_Activity.this, // 현재 화면
                Backgound_service.class);

        bindService(service_intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);


        inst_rb_01 = findViewById(R.id.installments_01);
        inst_rb_02 = findViewById(R.id.installments_02);
        inst_rb_03 = findViewById(R.id.installments_03);
        inst_rb_06 = findViewById(R.id.installments_06);
        inst_rb_09 = findViewById(R.id.installments_09);
        inst_rb_12 = findViewById(R.id.installments_12);

        inst_rg = findViewById(R.id.installments_group);
        inst_rg.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        inst_next_btn = findViewById(R.id.installments_next_btn);
        
        //이전페이지에서 충전금액 받아오기
        Intent intent1 = getIntent();
        pay = intent1.getExtras().getString("pay");
        amount = intent1.getExtras().getString("amount");
        time = intent1.getExtras().getString("time");

        inst_next_btn.setOnClickListener(v -> {
            backgound_service.Stop_ad_thread();
            
            Intent intent = new Intent(Installments_Setting_Activity.this, Card_Read_Activity.class);
            intent.putExtra("installments",Inst_result);
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





    //라디오 그룹 클릭 리스너
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = (radioGroup, i) -> {

        //시간 초기화
        backgound_service.Stop_ad_thread();
        backgound_service.Start_ad_thread(1);

        if(i == R.id.installments_01){
            Inst_result = "00";
            Toast.makeText(this, "00", Toast.LENGTH_SHORT).show();
        }
        else if(i == R.id.installments_02){
            Inst_result = "02";
            Toast.makeText(this, "02", Toast.LENGTH_SHORT).show();
        }
        else if(i == R.id.installments_03){
            Inst_result = "03";
            Toast.makeText(this, "03", Toast.LENGTH_SHORT).show();
        }
        else if(i == R.id.installments_06){
            Inst_result = "06";
            Toast.makeText(this, "06", Toast.LENGTH_SHORT).show();
        }
        else if(i == R.id.installments_09){
            Inst_result = "09";
            Toast.makeText(this, "09", Toast.LENGTH_SHORT).show();
        }
        else if(i == R.id.installments_12){
            Inst_result = "12";
            Toast.makeText(this, "12", Toast.LENGTH_SHORT).show();
        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Installments_ setting","finish");
        backgound_service.Stop_ad_thread();
        if(isService){
            unbindService(conn); // 서비스 종료
            isService = false;
        }

    }

}

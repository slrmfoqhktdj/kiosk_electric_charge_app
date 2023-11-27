package com.example.kiosk_electric_charger.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.SecondActivity;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Card_Read_Activity extends AppCompatActivity {

    //할부
    private String installments;

    //가격
    private String pay;

    //충전량
    private String amount;
    //충전시간
    private String time;

    private int try_cnt = 1;

    private final int SEND_REQUEST_CODE = 1;
    
    private final int SEND_REQUEST_NORMAL = 2;

    private final char fs = 0x1C;
    String pay_select = "0200";     // 거래구분     - 4
    String pay_category = "10";     // 거래 유형    - 2
    String pay_WCC = "I";           // WCC  - 1
    String pay_bugase = "0" ;    // 부가세 - V(12)
    String pay_bongsa = "0" ;    // 봉사료 - V(12)
    String pay_apprtid = "2393300001";            // 승인시 catid - 10
    String pay_myunse = "000000000";        // 면세금액 - V(9)
    String pay_prvnum = "00000000000000000001";         // POS 자체 전문관리번호    - 20
    String pay_filler = "ABCDEFGHIJKLNMOPQRSTUVWXYZABCDE";             // filler - V(31)
    String pay_txt = "PRO";             // 전문TEXT - V(3)
    String pay_devicegb = "H1";         // 기종구분 - V(2)

    TextView card_reader_txt;

    ImageView card_reader_img , card_arrow_img;
    private AnimationDrawable animationDrawable;

    //승인번호
    private String pay_number = "";
    //승인 일시(YYMMDDhhmmss)
    private String pay_time = "";
    //카드구분 17
    private String pay_by = "";

    //카드번호 대체 거래일련번호 19
    private String card_num = "";


    private final Retrofit_Interface retrofitService = Retrofit_Factory.create();

    //POS에서 NVCAT으로 결제 요청
    private void Request_Pay(){
        String senddata = "";
        senddata = pay_select + fs + pay_category + fs + pay_WCC + fs + pay + fs + pay_bugase + fs + pay_bongsa + fs + installments + fs + fs + fs + pay_apprtid + fs + fs + fs + fs + pay_myunse + fs + fs + fs + pay_prvnum + fs + fs + fs + pay_txt + fs + pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,SEND_REQUEST_CODE);
    }

    // NVCAT 재시작요청
    public void NVCAT_Restart(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","RESTART");
        intent.setType("text/plain");
        startActivityForResult(intent,SEND_REQUEST_NORMAL );
    }

    //카드 삽입 여부 체크
    public void Check_Card_Input(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","CHKCARDINMP");
        intent.setType("text/plain");
        startActivityForResult(intent,SEND_REQUEST_NORMAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEND_REQUEST_CODE ) {
            // 요청 정상
            if (resultCode == RESULT_OK) {

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT success",return_value + " "+return_data);

                String[] _msg = return_data.split(String.valueOf(fs));


                if(_msg[2].equals("0000")){
                    
                    //승인 번호, 일시 저장후 서버에 통신
                    pay_number = _msg[7].trim();
                    pay_time = _msg[8].trim();
                    pay_by = _msg[18].trim();
                    card_num = _msg[20].trim();

                    Log.d("pay info",pay_number + " "+pay_time + " "+pay_by+" "+card_num);


                    HashMap<String,String> input = new HashMap<>();
                    input.put("Device_id", DeviceModel.getDeviceid());  // 기기 아이디
                    input.put("Charging_time",time);    // 충전 소요 시간
                    input.put("Charge_amount",amount);  // 충전 량
                    input.put("Charge_Pay",pay);    //충전 결제액
                    input.put("Pay_By",pay_by);     // 결제 수단
                    input.put("Credit_num",card_num);   //카드 번호
                    input.put("Pay_num",pay_number);    // 승인 번호
                    input.put("Pay_day",pay_time);  // 결제일

                    retrofitService.sendPayHistory(input).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            // 성공일때
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            //서버로 안보내졌을때
                        }
                    });

                    //카드 제거 확인
                    new Handler().postDelayed(this::Check_Card_Input, 2000);




                    

                }else{
                    Log.e("Card_Read_Activity", "pay err");
                    //시도횟수
                    if(try_cnt>3){
                        NVCAT_Restart();
                        go_firstpage("오류가 발생했습니다 처음페이지로 이동합니다");
                    }else{
                        card_reader_txt.setText("결제진행에 오류가 발생했습니다 다시 시도해주세요 ("+try_cnt+"/3");
                        new Handler().postDelayed(this::Request_Pay, 2000);
                        try_cnt++;
                    }

                }



                // 요청 실패
            }else if(resultCode == RESULT_CANCELED){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT err",return_value + " "+return_data);


                // time over err
                if(return_value == -7){
                    go_firstpage("시간이 지체되었습니다. 처음페이지로 돌아갑니다.");
                }else if(return_value == -12){
                    go_firstpage("리더기 설정이 되지 않았습니다.");
                }
                else{
                    //NVCAT 연결 안될때
                    if(return_value != 1){
                        NVCAT_Restart();
                        try_cnt = 1;
                    }
                }





                // 예외처리
            }else{
                Log.d("NVCAT Card_Read_Activity err","예외");
            }
        }else if (requestCode == SEND_REQUEST_NORMAL){
            if(resultCode == RESULT_OK){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT err",return_value + " "+return_data);



                if(return_data.equals("I")){
                    //꽃혀있는 경우
                    card_reader_txt.setText("리더기에서 카드를 제거해주세요. 제거 후 충전이 시작됩니다.");
                    
                    //이미지 화살표 바꾸기
//                    card_arrow_img.setImageResource(0);
                    card_arrow_img.setImageResource(R.drawable.arrow_down);

                    //확인 반복
                    new Handler().postDelayed(this::Check_Card_Input, 2000);


                }else if(return_data.equals("N")){
                    //안 꽃혀 있는 경우
                    Intent intent = new Intent(Card_Read_Activity.this, Charging_Activity.class);
                    intent.putExtra("pay_num",pay_number);
                    intent.putExtra("charging_time",amount);
                    intent.putExtra("charge_amount",time);

                    startActivity(intent);
                    finish();
                }else{
                    //NVCAT에 오류가 있는 경우
                    go_firstpage("결제 시스템과 연결이 불안정하여 재시작 중입니다. 잠시만 기다려주세요.");
                    Log.e("재시작","재시작 메시지 보내기 성공");
                }

            }else if(resultCode == RESULT_CANCELED){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT err",return_value + " "+return_data);

                Log.e("재시작","재시작 메시지 보내기 실패");
            }else{
                Log.d("NVCAT Card_Read_Activity err","예외 "+resultCode);
            }
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_reader);

        Intent intent = getIntent();
        installments = intent.getExtras().getString("installments");
        pay = intent.getExtras().getString("pay");
        amount = intent.getExtras().getString("amount");
        time = intent.getExtras().getString("time");


        card_reader_txt = findViewById(R.id.card_reader_txt);
        card_reader_img = findViewById(R.id.card_reader_img);
        card_arrow_img = findViewById(R.id.card_arrow_img);

        
        if(installments != null || amount != null){
            Request_Pay();    
        }else{
            //할부값 제대로 안들어오면 오류 페이지
            Log.e("Card_Read_Activity", "할부값 null");
            go_firstpage("오류가 발생했습니다 처음페이지로 이동합니다");
        }

        card_arrow_img.setImageResource(R.drawable.arrow_up);

        animationDrawable = (AnimationDrawable) card_reader_img.getBackground();

        animationDrawable.start();





    }
    
    private void go_firstpage(String msg){
        card_reader_txt.setText(msg);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Card_Read_Activity.this, SecondActivity.class);
            startActivity(intent);
            finish();
        },2500);
    }







}

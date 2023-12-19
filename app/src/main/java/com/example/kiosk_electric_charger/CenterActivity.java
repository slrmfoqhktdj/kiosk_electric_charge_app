package com.example.kiosk_electric_charger;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.kiosk_electric_charger.adapter.SectionPageAdapter;
import com.example.kiosk_electric_charger.adview.AD_Controller;
import com.example.kiosk_electric_charger.adview.ImageVideoView;
import com.example.kiosk_electric_charger.background.Backgound_service;
import com.example.kiosk_electric_charger.fragment.ChargeInfoFragment;
import com.example.kiosk_electric_charger.fragment.ChargeSelectFragment;
import com.example.kiosk_electric_charger.fragment.InputPayFragment;
import com.example.kiosk_electric_charger.fragment.PayFragment;
import com.example.kiosk_electric_charger.fragment.PaySelectFragment;
import com.example.kiosk_electric_charger.model.CardStateModel;
import com.example.kiosk_electric_charger.model.DataTransferModel;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.model.MainViewModel;
import com.example.kiosk_electric_charger.model.PageAddrModel;
import com.example.kiosk_electric_charger.model.PayInfoModel;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_PayModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;
import com.example.kiosk_electric_charger.ws.JsonClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import eu.chargetime.ocpp.JSONClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CenterActivity extends AppCompatActivity {

    private String tag = "CenterActivity";

    private ViewPager mViewPager;
    private SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());

    private final Retrofit_Interface retrofitService = Retrofit_Factory.create();
    private ImageVideoView imageVideoView;

    private MainViewModel viewModel;

    private AD_Controller adController;     // 광고 컨트롤러

    JsonClient mClient;                     // ws 클라이언트

    Backgound_service backgound_service;
    private boolean isService = false;

    Intent service_intent;

    //충전 타이머 초
    private int left_sec = 0;
    private int right_sec = 0;

    //충전 타이머
    private Timer left_timer;
    private Timer right_timer;

    //승인번호
    private String pay_number = "";
    //승인 일시(YYMMDDhhmmss)
    private String pay_time = "";
    //카드구분 17
    private String pay_by = "";

    //카드번호 대체 거래일련번호 19
    private String card_num = "";

    private int try_cnt = 1;    // 결제 시도 카운터
    //충전량
    private String amount;


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




    // 충전시간
    private final BroadcastReceiver chargetimerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean timertype = intent.getBooleanExtra("type",false);


            if(timertype){
                Stop_left_charge_time();            // 왼쪽 충전 중지
//                End_Charging_Rtrofit(viewModel.getLeft_pay_number().getValue());
            }else{
                Stop_right_charge_time();           // 오른쪽 충전 중지
//                End_Charging_Rtrofit(viewModel.getRight_pay_number().getValue());
            }


        }
    };


    // 페이지 이동
    private final BroadcastReceiver pageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int page = intent.getIntExtra("page",0);
            Log.d(tag,"pageReceiver");
            ResetAD_Show();         // 광고 대기시간 초기화

            switch (page){

                    // 가격 입력 페이지
                case PageAddrModel.InputPayFgm_r:       // inputpay fragment 일때 서버에서 가격 단가 받아오기
                case PageAddrModel.InputPayFgm_l:
                    retrofitService.getPay().enqueue(new Callback<Retrofit_PayModel>() {
                        @Override
                        public void onResponse(Call<Retrofit_PayModel> call, Response<Retrofit_PayModel> response) {
                            if(response.isSuccessful()){
                                Retrofit_PayModel retrofitPayModel = response.body();

                                viewModel.getPayPrice().setValue(retrofitPayModel.getPay());

                                Log.d("success getPay()",String.valueOf(retrofitPayModel.getPay()));
                            }
                        }
                        @Override
                        public void onFailure(Call<Retrofit_PayModel> call, Throwable t) {
                            Log.d("fail",t.getMessage());
                        }
                    });
                    break;

                    //결제페이지
                case PageAddrModel.PayFgm_r:

                    new Handler().postDelayed(() -> {
                        viewModel.getright_device_charging().setValue(true);
                        mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_r);
                        Start_right_charge_time();
                    },2500);
                    break;
                case PageAddrModel.PayFgm_l:

                    //결제 유형에 따른 결제 명령
                    switch (viewModel.getCard_read_state().getValue()){
                        case CardStateModel.NFC_IC_State:
                            amount = String.valueOf(viewModel.getleft_pre_charge_amount().getValue());
                            Request_IC_Pay(String.valueOf(viewModel.getleft_payValue().getValue()));
                            break;
                        case CardStateModel.BarCode_STATE:
                            amount = String.valueOf(viewModel.getleft_pre_charge_amount().getValue());
                            Request_Read_Qr();
                            break;
                        case CardStateModel.RFID_STATE:
                            amount = String.valueOf(viewModel.getleft_pre_charge_amount().getValue());
                            Request_Read_RFID();
                            break;
                    }
                    break;

                    //정보페이지
                case PageAddrModel.ChargeInfoFgm_r:
                case PageAddrModel.ChargeInfoFgm_l:

                    break;


                default:
                    break;
            }



            mViewPager.setCurrentItem(page);
//            mViewPager.setCurrentItem(page,false);    //애니메이션 없애기
        }
    };






    //POS에서 NVCAT으로 승인요청결제 요청
    private void Request_IC_Pay(String pay){
        String senddata = "";
        final char fs = PayInfoModel.fs;
        senddata = PayInfoModel.pay_select_apply + fs + PayInfoModel.pay_category + fs + PayInfoModel.pay_WCC + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs + PayInfoModel.pay_apprtid + fs + fs + fs + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CODE);
    }

    //POS에서 NVCAT으로 승인요청결제 취소 요청
    private void Request_IC_Cancle_Pay(String pay){
        String senddata = "";
        final char fs = PayInfoModel.fs;
        String cancle_pay_num = "11283808";
        String cancle_pay_day = "231212";
        senddata = "0420" + fs + PayInfoModel.pay_category + fs + "L" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs +cancle_pay_num+ fs +cancle_pay_day + fs + PayInfoModel.pay_apprtid + fs + fs + fs +"NS231212112838D33115"+  fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs +"P000000001500"+ fs + "PCL" + fs + PayInfoModel.pay_devicegb + fs  + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CODE);
    }

    //NVCAT QR읽기 요청
    private void Request_Read_Qr(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","REQBARCODE");
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_QR);
    }

    //POS에서 NVCAT으로 QR결제 요청
    private void Request_QR_Pay(String pay,String QR_code){
        String senddata = "";
        final char fs = PayInfoModel.fs;
        senddata = "0300" + fs + PayInfoModel.pay_category + fs + "L" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs + PayInfoModel.pay_apprtid + fs + fs + fs + QR_code + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CODE);
    }

    //NVCAT RFID읽기 요청
    private void Request_Read_RFID(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","REQBALANCE");
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_RFID);
    }


    //POS에서 NVCAT으로 RFID결제 요청
    private void Request_RFID_Pay(String pay,String RFID_code){
        String senddata = "";
        final char fs = PayInfoModel.fs;
        senddata = "0300" + fs + PayInfoModel.pay_category + fs + "L" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs + PayInfoModel.pay_apprtid + fs + fs + fs + RFID_code + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CODE);
    }

    //POS에서 NVCAT으로 현금 iC 결제 요청
    private void Request_Pay_Pay(String pay){
        String senddata = "";
        final char fs = PayInfoModel.fs;

        String cancle_select = "0200";
        String cancle_category = "I1";
        String cancle_segum = "000000000";
        String cancle_type = "01";
        String cancle_pay_num = "10481452";
        String cancle_pay_day = "231205";


        senddata = cancle_select + fs + cancle_category + fs +  fs + pay + fs + cancle_segum + fs + "000000000" + fs + cancle_type + fs + fs + fs + PayInfoModel.pay_apprtid + fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs;

        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CANCLE);
    }


    //POS에서 NVCAT으로 현금 IC 결제 취소 요청
    private void Request_Pay_Cancle(String pay){
        String senddata = "";
        final char fs = PayInfoModel.fs;

        String cancle_select = "0420";
        String cancle_category = "I4";
        String cancle_segum = "000000000";
        String cancle_type = "02";
        String cancle_pay_num = "00028901";
        String cancle_pay_day = "231206";


        senddata = cancle_select + fs + cancle_category + fs +  fs + pay + fs + cancle_segum + fs + "000000000" + fs + cancle_type + fs + cancle_pay_num + fs + cancle_pay_day + fs + PayInfoModel.pay_apprtid + fs +"123"+ fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs;

        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CANCLE);
    }




    // NVCAT 재시작요청
    public void NVCAT_Restart(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","RESTART");
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_NORMAL );
    }

    //카드 삽입 여부 체크
    public void Check_Card_Input(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","CHKCARDINMP");
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_NORMAL);
    }











    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(tag,"onActivityResult");
        ResetAD_Show();     // 광고 대기시간 초기화

        if (requestCode == PayInfoModel.SEND_REQUEST_CODE ) {
            // 요청 정상
            if (resultCode == RESULT_OK) {

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("CODE RESULT_OK",return_value + " "+return_data);

                String[] _msg = return_data.split(String.valueOf(PayInfoModel.fs));


                if(_msg[2].equals("0000")){

                    //승인 번호, 일시 저장후 서버에 통신
                    if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){
                        viewModel.getLeft_pay_number().setValue(_msg[7].trim());
                    }else{
                        viewModel.getRight_pay_number().setValue(_msg[7].trim());
                    }
                    pay_number = _msg[7].trim();
                    pay_time = _msg[8].trim();

                    if(viewModel.getCard_read_state().getValue() == CardStateModel.NFC_IC_State){
                        pay_by = _msg[18].trim();
                        card_num = _msg[20].trim();
                    }else if(viewModel.getCard_read_state().getValue() == CardStateModel.BarCode_STATE){
                        pay_by = _msg[27].trim();
                        card_num = _msg[17].trim();
                    }else{
                        pay_by = _msg[18].trim();
                        card_num = _msg[20].trim();
                    }
                    Log.d("CODE RESULT_OK", "Check 0");

                    Log.d("pay info",pay_number + " / "+pay_time + " / "+pay_by+" / "+card_num);


//                    HashMap<String,String> input = new HashMap<>();
//                    input.put("Device_Id", DeviceModel.getDeviceid());  // 기기 아이디
////                    input.put("Charge_amount",amount);  // 충전 량
//                    if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){
//                        input.put("Charge_Pay",String.valueOf(viewModel.getleft_payValue().getValue()));    //충전 결제액
//                    }else{
//                        input.put("Charge_Pay",String.valueOf(viewModel.getright_payValue().getValue()));    //충전 결제액
//                    }
//
//                    Log.d("CODE RESULT_OK", "Check 1");
//
//                    input.put("Pay_By",pay_by);     // 결제 수단    = 카드구분
//                    input.put("Credit_Num",card_num);   //카드 번호
//                    input.put("Pay_Num",pay_number);    // 승인 번호
//                    input.put("Pay_Day",pay_time);  // 결제일
//
//                    Log.d("CODE RESULT_OK", "Check 2");


                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Device_Id",DeviceModel.getDeviceid());

                        if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){
                            jsonObject.put("Charge_Pay",String.valueOf(viewModel.getleft_payValue().getValue()));    //충전 결제액
                        }else{
                            jsonObject.put("Charge_Pay",String.valueOf(viewModel.getright_payValue().getValue()));    //충전 결제액
                        }

                        jsonObject.put("Pay_By",pay_by);     // 결제 수단    = 카드구분
                        jsonObject.put("Credit_Num",card_num);   //카드 번호
                        jsonObject.put("Pay_Num",pay_number);    // 승인 번호
                        jsonObject.put("Pay_Day",pay_time);  // 결제일

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    try {
                        mClient.connect();
                        mClient.sendDataTransfer(DeviceModel.getDeviceid(), DataTransferModel.getPay(), String.valueOf(jsonObject));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

//                    retrofitService.sendPayHistory(input).enqueue(new Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {                    // 결제 성공, 통신 성공
//                            // 성공일때
//                            Log.d(tag, "SendPayHistory Success");
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {                   // 결제 성공, 통신 실패
//                            //서버로 안보내졌을때
//                            Log.d(tag, t.getMessage());
//                        }
//                    });

                    // 결제 성공
                    try_cnt= 1;
                    //카드 제거 확인
                    new Handler().postDelayed(this::Check_Card_Input, 2000);






                }else{      // 결제 오류

                    if(_msg[2].equals("8374")){             // 결제 응답 메시지 : 회선장애 재시도요망
                        Toast.makeText(this,"회선장애가 발생했습니다. 결제 재시도 바랍니다.",Toast.LENGTH_LONG).show();
                    }


                    Log.e("CODE", "pay err");
                    //시도횟수
                    if(try_cnt>=3){
                        NVCAT_Restart();
                        Toast.makeText(this,"시도 횟수가 초과하였습니다. 처음페이지로 이동합니다",Toast.LENGTH_LONG).show();
                        mViewPager.setCurrentItem(PageAddrModel.ChargeSelectFgm);
                        try_cnt = 1;
                    }else{
                        Toast.makeText(this,"결제진행에 오류가 발생했습니다 다시 시도해주세요 ("+try_cnt+"/3",Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(() -> {

                            //결제 유형에 따른 결제 명령
                            switch (viewModel.getCard_read_state().getValue()){
                                case CardStateModel.NFC_IC_State:
                                    if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){
                                        Request_IC_Pay(String.valueOf(viewModel.getleft_payValue().getValue()));
                                    }else{
                                        Request_IC_Pay(String.valueOf(viewModel.getright_payValue().getValue()));
                                    }
                                    break;
                                case CardStateModel.BarCode_STATE:
                                    Request_Read_Qr();
                                    break;
                                case CardStateModel.RFID_STATE:
                                    Request_Read_RFID();
                                    break;
                            }

                        },2000);
                        try_cnt++;
                    }

                }



                // 결제 요청 실패
            }else if(resultCode == RESULT_CANCELED){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("CODE RESULT_CANCELED",return_value + " "+return_data);


                // time over err
                if(return_value == -3){
                    Toast.makeText(this,"결제서버와의 통신이 불안정합니다. 다시 시도해주세요.",Toast.LENGTH_LONG).show();
                } else if(return_value == -7){
                    Toast.makeText(this,"시간이 지체되었습니다. 처음페이지로 돌아갑니다.",Toast.LENGTH_LONG).show();
                }else if(return_value == -12){
                    Toast.makeText(this,"리더기 설정이 되지 않았습니다. 재부팅을 해주세요.",Toast.LENGTH_LONG).show();
                }else if(return_value == -13){
                    Log.d("CODE CANCELED","전문 오류");
                }
                else{
                    //NVCAT 연결 안될때
                    if(return_value != 1){
                        NVCAT_Restart();
                    }
                }
                try_cnt = 1;
                mViewPager.setCurrentItem(PageAddrModel.ChargeSelectFgm);
                // 예외처리
            }else{
                Log.d("CODE err","예외");
            }
        }else if (requestCode == PayInfoModel.SEND_REQUEST_NORMAL){
            if(resultCode == RESULT_OK){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NORMAL RESULT_OK",return_value + " "+return_data);



                if(return_data.equals("I")){
                    //꽃혀있는 경우
                    Toast.makeText(this,"리더기에서 카드를 제거해주세요. 제거 후 충전이 시작됩니다.",Toast.LENGTH_LONG).show();
                    viewModel.getCard_read_state().setValue(CardStateModel.Card_Out);
                    //확인 반복
                    new Handler().postDelayed(this::Check_Card_Input, 2000);


                }else if(return_data.equals("N")){
                    //안 꽃혀 있는 경우
                    viewModel.getCard_read_state().setValue(CardStateModel.Pay_Complete);
                    if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){              // 왼쪽결제 일때

//                        Start_Charging_Rtrofit(viewModel.getLeft_pay_number().getValue());      // 시작시간 업데이트 보내기
                        viewModel.getleft_device_charging().setValue(true);                     // 충전중 체크
                        mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_l);               // 정보 페이지로
                        Start_left_charge_time();                                               // 타이머 시작
                    }else{                                                                      // 오른쪽결제 일때
                        
//                        Start_Charging_Rtrofit(viewModel.getRight_pay_number().getValue());      // 시작시간 업데이트 보내기
                        viewModel.getright_device_charging().setValue(true);                     // 충전중 체크
                        mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_r);                // 정보 페이지로
                        Start_right_charge_time();                                               // 타이머 시작
                    }

                    

                }else{
                    Log.e("재시작","재시작 메시지 보내기 성공");
                }

            }else if(resultCode == RESULT_CANCELED){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NORMAL RESULT_CANCELED",return_value + " "+return_data);

                Log.e("재시작","재시작 메시지 보내기 실패");
            }else{
                Log.d("NORMAL err","예외 "+resultCode);
            }
        }
        else if (requestCode == PayInfoModel.SEND_REQUEST_QR) {
            if(resultCode == RESULT_OK){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("QR RESULT_OK ",return_value + " "+return_data);

                if(return_value == 1){

                    if(mViewPager.getCurrentItem() < PageAddrModel.ChargeSelectFgm){
                        Request_QR_Pay(String.valueOf(viewModel.getleft_payValue().getValue()),return_data);
                    }else{
                        Request_QR_Pay(String.valueOf(viewModel.getright_payValue().getValue()),return_data);
                    }


                }

            }else if(resultCode == RESULT_CANCELED){                // qr 읽기 요청 오류

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("QR CANCELED",return_value + " "+return_data);

                if(return_value == -7){             //QR 시간 초과
                    Toast.makeText(this,"QR 결제 시간이 초과했습니다.",Toast.LENGTH_LONG).show();
                }else if(return_value == -12){
                    Toast.makeText(this,"리더기 설정이 되지 않았습니다. 재부팅을 해주세요.",Toast.LENGTH_LONG).show();
                }
                else{
                    //NVCAT 연결 안될때
                    if(return_value != 1){
                        NVCAT_Restart();
                    }
                }
                try_cnt = 1;
                mViewPager.setCurrentItem(PageAddrModel.ChargeSelectFgm);

            }else{
                Log.d("QR err","예외 "+resultCode);
            }
        }
        else if (requestCode == PayInfoModel.SEND_REQUEST_RFID) {               //RF 결제시
            if(resultCode == RESULT_OK){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("RFID RESULT_OK ",return_value + " "+return_data);


                if(return_value == 1){

                    if(mViewPager.getCurrentItem() < PageAddrModel.ChargeSelectFgm){
                        Request_RFID_Pay(String.valueOf(viewModel.getleft_payValue().getValue()),return_data);
                    }else{
                        Request_RFID_Pay(String.valueOf(viewModel.getright_payValue().getValue()),return_data);
                    }

                }


            }else if(resultCode == RESULT_CANCELED){                // rfid 읽기 요청 오류

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("RFID CANCELED",return_value + " "+return_data);


                if(return_value == -7){             //RF 시간 초과
                    Toast.makeText(this,"RF 결제 시간이 초과했습니다.",Toast.LENGTH_LONG).show();
                }else if(return_value == -12){
                    Toast.makeText(this,"리더기 설정이 되지 않았습니다. 재부팅을 해주세요.",Toast.LENGTH_LONG).show();
                }
                else{
                    //NVCAT 연결 안될때
                    if(return_value != 1){
                        NVCAT_Restart();
                    }
                }
                try_cnt = 1;
                mViewPager.setCurrentItem(PageAddrModel.ChargeSelectFgm);



            }else{
                Log.d("RFID err","예외 "+resultCode);
            }
        }

        else if (requestCode == PayInfoModel.SEND_REQUEST_CANCLE) {               // 결제 취소
            if(resultCode == RESULT_OK){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("CANCLE RESULT_OK ",return_value + " "+return_data);


            }else if(resultCode == RESULT_CANCELED){                // rfid 읽기 요청 오류

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("CANCLE CANCELED",return_value + " "+return_data);



            }else{
                Log.d("CANCLE err","예외 "+resultCode);
            }
        }

    }

    //충전 시작 통신
//    private void Start_Charging_Rtrofit(String pay_num){
//        HashMap<String,String> input = new HashMap<>();
//        input.put("Pay_num", pay_num);  // 결제 승인 번호
//        input.put("C_time_s", returnTime());  // 충전시작시간
//        retrofitService.sendCStart(input).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                // 성공일때
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                //서버로 안보내졌을때
//            }
//        });
//    }

    //충전 끝 통신
//    private void End_Charging_Rtrofit(String pay_num){
//        HashMap<String,String> input = new HashMap<>();
//        input.put("Pay_num", pay_num);  // 결제 승인 번호
//        input.put("C_time_e", returnTime());  // 충전종료시간
//        retrofitService.sendCEnd(input).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                // 성공일때
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                //서버로 안보내졌을때
//            }
//        });
//    }

    // 다른 앱 위에 그리기 권한 얻기
    public void getPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우

            if (!Settings.canDrawOverlays(this)) {              // 체크
                Log.d(tag,"앱위에 그리기 권한 없음");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
//
//    private WindowManager windowManager;
//    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_center);
        
//        startLockTask();    // 화면 고정
//        stopLockTask();
        
        // 권한 허용
        getPermission();

//          // 다른앱위에 그리기
//        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        view = layoutInflater.inflate(R.layout.fragment_pay, null);
//
//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT
//        );
//        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        windowManager.addView(view, layoutParams);

        //서비스 시작
        service_intent = new Intent(
                CenterActivity.this, // 현재 화면
                Backgound_service.class);

        startService(service_intent);

        bindService(service_intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);


        mViewPager = findViewById(R.id.viewpager);
//        mViewPager.setOffscreenPageLimit(1);

        setupViewPager(mViewPager);

        mViewPager.setCurrentItem(PageAddrModel.ChargeSelectFgm);

        imageVideoView = findViewById(R.id.imageVideoView);


        adController = new AD_Controller(imageVideoView);       // 광고 컨트롤러 init

        //페이지 전환
        IntentFilter sfilter01 = new IntentFilter();
        sfilter01.addAction("Page");
        registerReceiver(pageReceiver,sfilter01);

        //충전 타이머
        IntentFilter sfilter02 = new IntentFilter();
        sfilter02.addAction("ChargeTimer");
        registerReceiver(chargetimerReceiver,sfilter02);




        viewModel = new ViewModelProvider(this).get(MainViewModel.class);




//        Request_IC_Pay("1500");

//        Request_IC_Cancle_Pay("500");

//        Request_Pay_Cancle("1000");

//        Request_Pay_Pay("1000");



        hb_handler = new Handler(Looper.getMainLooper());
        mClient = JsonClient.getInstance(getApplicationContext());




        Start_hb_thread();



    }

    Handler hb_handler;
    Thread hb_thread;
    public void Start_hb_thread(){
        

        hb_thread = new Thread(() -> hb_handler.postDelayed(()->{
            try {
                mClient.connect();
                mClient.sendHeartBeat();
                mClient.disconnect();

                Start_hb_thread();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },60000));

        hb_thread.start();
    }
    

    public void Stop_hb_thread() {
        if (hb_thread != null){
            hb_handler.removeCallbacksAndMessages(null);
            hb_thread.interrupt();
            hb_thread = null;
        }
    }

    public void setupViewPager(ViewPager viewPager) {

        adapter.addFragment(new ChargeInfoFragment(true));
        adapter.addFragment(new PayFragment(true));
        adapter.addFragment(new InputPayFragment(true));
        adapter.addFragment(new PaySelectFragment(true));
        adapter.addFragment(new ChargeSelectFragment());
        adapter.addFragment(new PaySelectFragment(false));
        adapter.addFragment(new InputPayFragment(false));
        adapter.addFragment(new PayFragment(false));
        adapter.addFragment(new ChargeInfoFragment(false));

        viewPager.setAdapter(adapter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(imageVideoView != null){
            imageVideoView.releasePlayer();
        }
        Log.d(tag,"onPause");


        if(backgound_service != null){
            backgound_service.Stop_ad_thread();
        }

        adController.Stop();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //딜레이를 줘야함  - bindservice 하는데 시간이 필요함
        new Handler().postDelayed(() -> {
            // 광고 타이머 시작
            backgound_service.Start_ad_thread();
        },2000);


//        // 광고 실행
        adController.Start_ad();

        Log.d(tag,"onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e(tag,"onDestroy");

        unregisterReceiver(pageReceiver);           // 페이지 이동
        unregisterReceiver(chargetimerReceiver);    // 충전 시간

        // 핸들러 취소
        adController.Close();
        adController = null;

        if(imageVideoView != null){
            imageVideoView.releasePlayer();
        }
        if(isService){
            unbindService(conn); // 바인드서비스 종료
            isService = false;
        }

        Stop_hb_thread();               // 하트비트 종료

        mClient.disconnect();           // ws 연결 끊고
        mClient = null;
    }

    @Override
    protected void onStop() {
        super.onStop();

        adController.Stop();
    }


    //왼쪽 충전 타이머
    private void Start_left_charge_time(){
        left_timer = new Timer();

        TimerTask left_timerTask = new TimerTask() {
            @Override
            public void run() {
                left_sec ++;
                viewModel.getleft_charge_time().postValue(String.format("%02d:%02d:%02d", left_sec/3600,left_sec/60, left_sec%60));
            }
        };

        left_timer.schedule(left_timerTask,0,1000);
    }

    private void Stop_left_charge_time(){
        if(left_timer != null){
            left_timer.cancel();
            left_timer = null;
            left_sec = 0;
        }
    }

    //오른쪽 충전 타이머
    private void Start_right_charge_time(){
        right_timer = new Timer();
        TimerTask right_timerTask = new TimerTask() {
            @Override
            public void run() {
                right_sec ++;
                viewModel.getright_charge_time().postValue(String.format("%02d:%02d:%02d", right_sec/3600,right_sec/60, right_sec%60));
            }
        };

        right_timer.schedule(right_timerTask,0,1000);
    }

    private void Stop_right_charge_time(){
        if(right_timer != null){
            right_timer.cancel();
            right_timer = null;
            right_sec = 0;
        }
    }


    //시간 반환
    private String returnTime(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        return sdf.format(date);
    }

    private void ResetAD_Show(){
        //광고 대기 시간 초기화
        backgound_service.Stop_ad_thread();
        backgound_service.Start_ad_thread();
    }


}

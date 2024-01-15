package com.example.kiosk_electric_charger;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.SharedPreferences.model.KeyModel;
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
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;
import com.example.kiosk_electric_charger.ws.JsonClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import eu.chargetime.ocpp.model.core.AvailabilityType;
import eu.chargetime.ocpp.model.core.ChargePointErrorCode;
import eu.chargetime.ocpp.model.core.ChargePointStatus;

public class CenterActivity extends AppCompatActivity {

    private String tag = "CenterActivity";

    private ViewPager mViewPager;
    private SectionPageAdapter adapter = new SectionPageAdapter(getSupportFragmentManager());

    private final Retrofit_Interface s_retrofitService = Retrofit_Factory.create();   // OCPP Server


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
    
    
    // 초기 설정값 - preference 에서 가져와야하는 값들
    private long mv_timer = 60000;  // metervalue 보내는 간격
    private long hb_timer = 60000;  // heartbeat 보내는 간격
    


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
//    private final BroadcastReceiver chargetimerReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            boolean timertype = intent.getBooleanExtra("type",false);
//            String pay_num = intent.getStringExtra("pay_num");
//
//            try {
//
//                if(pay_num == null){
//                    if(timertype){
//                        pay_num = viewModel.getLeft_pay_number().getValue();
//                    }else{
//                        pay_num = viewModel.getRight_pay_number().getValue();
//                    }
//                }
////                mClient.connect();
//
//                mClient.sendStopAuthorize(Integer.parseInt(pay_num));
//
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//
//
//
//        }
//    };


    //충전 중지
//    private final BroadcastReceiver chargeEndReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String pay_num = intent.getStringExtra("pay_num");
//            ChargePointStatus chargepointstatus = (ChargePointStatus) intent.getSerializableExtra("ChargePointStatus");
//
//            if(viewModel.getLeft_pay_number().getValue().equals(pay_num)){
//                Stop_left_charge_time();            // 왼쪽 충전 중지
//                Stop_mv_l_thread();                 // metervalue 중지
//                try {
////                    mClient.connect();
//                    mClient.sendStatusNotification(1, ChargePointErrorCode.NoError, chargepointstatus,null);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }else{
//                Stop_right_charge_time();           // 오른쪽 충전 중지}
//                Stop_mv_r_thread();                  // metervalue 중지
//                try {
////                    mClient.connect();
//                    mClient.sendStatusNotification(2, ChargePointErrorCode.NoError, chargepointstatus,null);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            }
//
//
//
//        }
//    };


    // 충전 권한 획득
//    private final BroadcastReceiver authorize_Receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            Log.d(tag," authorize_Receiver ");
//
//            try {
//                int connector;
//                String itag;
//
//                if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){              // 왼쪽결제 일때
//                    connector = 1;                                                          // 충전기커넥터 id
//                    itag = viewModel.getLeft_pay_number().getValue();                       // 결제 번호
//                }else{                                                                      // 오른쪽결제 일때
//                    connector = 2;                                                          // 충전기커넥터 id
//                    itag = viewModel.getRight_pay_number().getValue();
//                }
//
//
////                mClient.connect();
//                mClient.sendStartTransaction(connector,itag,11000);
//
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//
//
//        }
//    };

    // 충전시작
//    private final BroadcastReceiver startChargingReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
////            int com_con = intent.getIntExtra("connector_id",10);
//            int connector;
//
//            Log.d(tag," startChargingReceiver ");
//
//            try {
//
//                if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){              // 왼쪽결제 일때
//                    connector = 1;
//                    viewModel.getleft_device_charging().setValue(true);                     // 충전중 체크
//                    mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_l);               // 정보 페이지로
//                    Start_left_charge_time();                                               // 타이머 시작
//                    Start_mv_l_thread(Integer.parseInt(viewModel.getLeft_pay_number().getValue()));                                                    // metervalue
//                }else{                                                                      // 오른쪽결제 일때
//                    connector = 2;
//                    viewModel.getright_device_charging().setValue(true);                     // 충전중 체크
//                    mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_r);                // 정보 페이지로
//                    Start_right_charge_time();                                               // 타이머 시작
//                    Start_mv_r_thread(Integer.parseInt(viewModel.getRight_pay_number().getValue()));                                                    // metervalue
//                }
//
//                mClient.sendStatusNotification(connector, ChargePointErrorCode.NoError, ChargePointStatus.Charging,null);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//
//
//
//        }
//    };



    // 충전시작 리모트

//    private final BroadcastReceiver remote_Start_Charge_Receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            int com_con = intent.getIntExtra("connector_id",10);
//
//            Log.d(tag," startChargingReceiver ");
//
//
//
//            try {
//                if(com_con == 1){
//                    if(Boolean.FALSE.equals(viewModel.getleft_device_charging().getValue())){   // 충전중이 아닐경우
//                        if(viewModel.getLeft_pay_number().getValue() == null){
//                            viewModel.getLeft_pay_number().setValue(String.valueOf(ThreadLocalRandom.current().nextInt(100000000, 1000000000)));  // 페이 넘버 없으면 생성
//                        }
////                        mClient.sendAuthorize(viewModel.getLeft_pay_number().getValue());
//                        mClient.sendStartTransaction(1,viewModel.getLeft_pay_number().getValue(),11000);
//                    }
//
//                }else{
//
//                    if(Boolean.FALSE.equals(viewModel.getright_device_charging().getValue())){        // 충전중이 아닐경우
//                        if(viewModel.getRight_pay_number().getValue() == null){
//                            viewModel.getRight_pay_number().setValue(String.valueOf(ThreadLocalRandom.current().nextInt(100000000, 1000000000)));  // 페이 넘버 없으면 생성
//                        }
////                        mClient.sendAuthorize(viewModel.getRight_pay_number().getValue());
//                        mClient.sendStartTransaction(2,viewModel.getRight_pay_number().getValue(),11000);
//                    }
//
//                }
//
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//
//        }
//    };


    // 전기 시세값 저장
//    private final BroadcastReceiver g_pay_Receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            float timertype = intent.getFloatExtra("pay",0.f);
//
//            if(!(timertype == 0)){
//                viewModel.getPayPrice().setValue(timertype);
//            }else{
//                Toast.makeText(getApplicationContext(),"값을 불러오는데 오류가 생겼습니다.",Toast.LENGTH_LONG).show();
//            }
//
//        }
//    };


    private final BroadcastReceiver ChargingReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case "ChargeTimer":         //충전시간

                    boolean timertype = intent.getBooleanExtra("type",false);
                    String pay_num = intent.getStringExtra("pay_num");

                    try {

                        if(pay_num == null){
                            if(timertype){
                                pay_num = viewModel.getLeft_pay_number().getValue();
                            }else{
                                pay_num = viewModel.getRight_pay_number().getValue();
                            }
                        }
//                mClient.connect();

                        mClient.sendStopAuthorize(Integer.parseInt(pay_num));


                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    
                    break;
                case "Get_Pay":     // 전기 시세값 저장

                    float timertype_Get_Pay = intent.getFloatExtra("pay",0.f);

                    if(!(timertype_Get_Pay == 0)){
                        viewModel.getPayPrice().setValue(timertype_Get_Pay);
                    }else{
                        Toast.makeText(getApplicationContext(),"값을 불러오는데 오류가 생겼습니다.",Toast.LENGTH_LONG).show();
                    }
                    
                    break;
                case "Start_Charging":          // 충전시작

//                    int com_con = intent.getIntExtra("connector_id",10);
                    int connector;

                    int get_connect_id = intent.getIntExtra("connect_id",-2);

                    Log.d(tag," startChargingReceiver ");

                    try {

                        if(get_connect_id == -2){
                            if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){              // 왼쪽결제 일때
                                connector = 1;
                                viewModel.getleft_device_charging().setValue(true);                     // 충전중 체크
                                mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_l);               // 정보 페이지로
                                Start_left_charge_time();                                               // 타이머 시작
                                Start_mv_l_thread(Integer.parseInt(viewModel.getLeft_pay_number().getValue()));                                                    // metervalue
                            }else{                                                                      // 오른쪽결제 일때
                                connector = 2;
                                viewModel.getright_device_charging().setValue(true);                     // 충전중 체크
                                mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_r);                // 정보 페이지로
                                Start_right_charge_time();                                               // 타이머 시작
                                Start_mv_r_thread(Integer.parseInt(viewModel.getRight_pay_number().getValue()));                                                    // metervalue
                            }
                        }else{
                            if(get_connect_id == 1){
                                connector = 1;
                                viewModel.getleft_device_charging().setValue(true);                     // 충전중 체크
                                mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_l);               // 정보 페이지로
                                Start_left_charge_time();                                               // 타이머 시작
                                Start_mv_l_thread(Integer.parseInt(viewModel.getLeft_pay_number().getValue()));
                            }else{
                                connector = 2;
                                viewModel.getright_device_charging().setValue(true);                     // 충전중 체크
                                mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_r);                // 정보 페이지로
                                Start_right_charge_time();                                               // 타이머 시작
                                Start_mv_r_thread(Integer.parseInt(viewModel.getRight_pay_number().getValue()));                                                    // metervalue
                            }
                        }



                        mClient.sendStatusNotification(connector, ChargePointErrorCode.NoError, ChargePointStatus.Charging,null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    
                    break;
                case "authorize_Receive":           // 충전 권한 획득

                    Log.d(tag," authorize_Receiver ");

                    try {
                        int connector_authorize_Receive;
                        String itag;

                        if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){              // 왼쪽결제 일때
                            connector_authorize_Receive = 1;                                                          // 충전기커넥터 id
                            itag = viewModel.getLeft_pay_number().getValue();                       // 결제 번호
                        }else{                                                                      // 오른쪽결제 일때
                            connector_authorize_Receive = 2;                                                          // 충전기커넥터 id
                            itag = viewModel.getRight_pay_number().getValue();
                        }


//                mClient.connect();
                        mClient.sendStartTransaction(connector_authorize_Receive,itag,11000);


                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    break;
                case "chargeEnd_Receiver":                  //충전 중지

                    String pay_num_chargeEnd_Receiver = intent.getStringExtra("pay_num");
                    ChargePointStatus chargepointstatus = (ChargePointStatus) intent.getSerializableExtra("ChargePointStatus");

                    if(viewModel.getLeft_pay_number().getValue().equals(pay_num_chargeEnd_Receiver)){
                        Stop_left_charge_time();            // 왼쪽 충전 중지
                        Stop_mv_l_thread();                 // metervalue 중지
                        try {
//                    mClient.connect();
                            mClient.sendStatusNotification(1, ChargePointErrorCode.NoError, chargepointstatus,null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        Stop_right_charge_time();           // 오른쪽 충전 중지}
                        Stop_mv_r_thread();                  // metervalue 중지
                        try {
//                    mClient.connect();
                            mClient.sendStatusNotification(2, ChargePointErrorCode.NoError, chargepointstatus,null);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    break;
                case "remote_Start_Charge_Receiver":          //충전 시작 리모트

                    int com_con = intent.getIntExtra("connector_id",10);

                    Log.d(tag," startChargingReceiver " + com_con);
                    String ex_num = String.valueOf(ThreadLocalRandom.current().nextLong(100000000, 1000000000));
                    Log.d(tag," startChargingReceiver 3 " + ex_num);

                    try {
                        if(com_con == 1){
                            Log.d(tag," startChargingReceiver 1 " + com_con);
                            if(Boolean.FALSE.equals(viewModel.getleft_device_charging().getValue())){   // 충전중이 아닐경우
                                if(viewModel.getLeft_pay_number().getValue().equals("00")){
                                    viewModel.getLeft_pay_number().setValue(ex_num);  // 페이 넘버 없으면 생성
                                }
//                                mClient.sendAuthorize(viewModel.getLeft_pay_number().getValue());       // 지난 pay 기록이 없기때문
                                mClient.sendStartTransaction(1,ex_num,11000);
                            }

                        }else{
                            Log.d(tag," startChargingReceiver 2 " + com_con);
                            if(Boolean.FALSE.equals(viewModel.getright_device_charging().getValue())){        // 충전중이 아닐경우
                                if(viewModel.getRight_pay_number().getValue().equals("00")){
                                    viewModel.getRight_pay_number().setValue(ex_num);  // 페이 넘버 없으면 생성
                                }
//                                mClient.sendAuthorize(viewModel.getRight_pay_number().getValue());
                                mClient.sendStartTransaction(2,ex_num,11000);
                            }

                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                    break;


                case "reset_Receive":


                    if(viewModel.getleft_device_charging().getValue()){
                        try {
                            mClient.sendStopAuthorize(Integer.parseInt(viewModel.getLeft_pay_number().getValue()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if(viewModel.getright_device_charging().getValue()){
                        try {
                            mClient.sendStopAuthorize(Integer.parseInt(viewModel.getRight_pay_number().getValue()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                    new Handler().postDelayed(() -> {
                        // 어플 재부팅
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        pm.reboot(null);
                    },2000);


                    break;


                case "changeAvailablitity_Receive":

                    int con = intent.getIntExtra("Connectore_id",10);
                    AvailabilityType T = (AvailabilityType) intent.getSerializableExtra("Type");

                    if(con == 1){      // 왼쪽 커넥터일 경우

                        
                        if(T == AvailabilityType.Inoperative){   // 사용중지메시지일 경우

                            if(viewModel.getleft_device_charging().getValue()){ // 충전 중일경우
                                try {
                                    mClient.sendStopAuthorize(Integer.parseInt(viewModel.getLeft_pay_number().getValue()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            PrefsHelper.write(KeyModel.getLeftChargingEnabled(),false);
                            //뷰모델 바꾸기
                            viewModel.getleft_charge_enable().setValue(false);

                        }else if(T == AvailabilityType.Operative){ // 사용가능메시지일 경우
                            PrefsHelper.write(KeyModel.getLeftChargingEnabled(),true);
                            //뷰모델 바꾸기
                            viewModel.getleft_charge_enable().setValue(true);
                        }
                    }else if(con == 2){    // 오른쪽 커넥터일 경우




                        if(T == AvailabilityType.Inoperative){   // 사용중지메시지일 경우


                            if(viewModel.getright_device_charging().getValue()){        // 충전중일때 취소
                                try {
                                    mClient.sendStopAuthorize(Integer.parseInt(viewModel.getRight_pay_number().getValue()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            PrefsHelper.write(KeyModel.getRightChargingEnabled(),false);
                            //뷰모델 바꾸기
                            viewModel.getright_charge_enable().setValue(false);

                        }else if(T == AvailabilityType.Operative){ // 사용가능메시지일 경우
                            PrefsHelper.write(KeyModel.getRightChargingEnabled(),true);
                            //뷰모델 바꾸기
                            viewModel.getright_charge_enable().setValue(true);

                        }
                    }



                    break;
                    
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

                    try {
//                        mClient.connect();
                        mClient.sendDataTransfer(DeviceModel.getDevicevendor(),DataTransferModel.getG_Pay(),null);      // 단가 받아오기
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    break;

                    //결제페이지
                case PageAddrModel.PayFgm_r:

                    //결제 유형에 따른 결제 명령
                    switch (viewModel.getCard_read_state().getValue()){
                        case CardStateModel.NFC_IC_State:
                            amount = String.valueOf(viewModel.getright_pre_charge_amount().getValue());
                            Request_IC_Pay(String.valueOf(viewModel.getright_payValue().getValue()));
                            break;
                        case CardStateModel.BarCode_STATE:
                            amount = String.valueOf(viewModel.getright_pre_charge_amount().getValue());
                            Request_Read_Qr();
                            break;
                        case CardStateModel.RFID_STATE:
                            amount = String.valueOf(viewModel.getright_pre_charge_amount().getValue());
                            Request_Read_RFID();
                            break;
                    }
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
//        senddata = PayInfoModel.pay_select_apply + fs + PayInfoModel.pay_category + fs + PayInfoModel.pay_WCC + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs + "7283730001" + fs + fs + fs + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        senddata = "0300" + fs + "10" + fs + "I" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs  + fs + fs + fs + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CODE);
    }

    //POS에서 NVCAT으로 승인요청결제 취소 요청    - 신용카드 확인, 체크 x
    private void Request_IC_Cancle_Pay(String pay,String cancle_pay_num,String cancle_pay_day,String cancle_pay_token,String origin_pay){
        String senddata = "";
        String origin_pay_c = String.format("P%012d",Integer.parseInt(origin_pay));
        final char fs = PayInfoModel.fs;
//        String cancle_pay_num = "15132936";
//        String cancle_pay_day = "240102";
//        String cancle_pay_token = "NS240102151321D03953";
//        senddata = "0420" + fs + "10" + fs + "N" + fs +pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs +cancle_pay_num+ fs +cancle_pay_day + fs + "7283730001" + fs + fs + fs +cancle_pay_token+  fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs +"P000000001500"+ fs + "PCL" + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        senddata = "0520" + fs + "10" + fs + "N" + fs +pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs +cancle_pay_num+ fs +cancle_pay_day + fs +  fs + fs + fs +cancle_pay_token+  fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs +origin_pay_c+ fs + "PCL" + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
//        intent.setAction("ReqNoCardCnl2");
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
    private void Request_QR_Cancle_Pay(String pay,String QR_code,String origin_pay){
        String senddata = "";
        String cancle_pay_num = "11020612";
        String cancle_pay_day = "240103";
        String origin_pay_c = String.format("P%012d", Integer.parseInt(origin_pay));
        final char fs = PayInfoModel.fs;
        senddata = "0520" + fs + PayInfoModel.pay_category + fs + "N" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs +cancle_pay_num+ fs +cancle_pay_day+ fs + fs + fs + fs + QR_code + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs +origin_pay_c+ fs + "PCL" + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CODE);
    }

    //POS에서 NVCAT으로 QR결제 요청
    private void Request_QR_Pay(String pay,String QR_code){
        String senddata = "";
        final char fs = PayInfoModel.fs;
        senddata = "0300" + fs + PayInfoModel.pay_category + fs + "L" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs + fs + fs + fs + QR_code + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
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
//        senddata = "0300" + fs + PayInfoModel.pay_category + fs + "L" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs + PayInfoModel.pay_apprtid + fs + fs + fs + RFID_code + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        senddata = "0300" + fs + PayInfoModel.pay_category + fs + "L" + fs + pay + fs + PayInfoModel.pay_bugase + fs + PayInfoModel.pay_bongsa + fs + PayInfoModel.installments + fs + fs + fs + "7283730001" + fs + fs + fs + RFID_code + fs + PayInfoModel.pay_myunse + fs + fs + fs + PayInfoModel.pay_prvnum + fs + fs + fs + PayInfoModel.pay_txt + fs + PayInfoModel.pay_devicegb + fs + fs + fs + fs + fs + fs + fs + fs + fs + fs;
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA",senddata);
        intent.setType("text/plain");
        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CODE);
    }

    //POS에서 NVCAT으로 현금 iC 결제 요청
//    private void Request_Pay_Pay(String pay){
//        String senddata = "";
//        final char fs = PayInfoModel.fs;
//
//        String cancle_select = "0200";
//        String cancle_category = "I1";
//        String cancle_segum = "000000000";
//        String cancle_type = "01";
//
//
//        senddata = cancle_select + fs + cancle_category + fs +  fs + pay + fs + cancle_segum + fs + "000000000" + fs + cancle_type + fs + fs + fs + PayInfoModel.pay_apprtid + fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs;
//
//        Intent intent = new Intent();
//        intent.setAction("NICEVCAT");
//        intent.putExtra("NVCATSENDDATA",senddata);
//        intent.setType("text/plain");
//        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CANCLE);
//    }


    //POS에서 NVCAT으로 현금 IC 결제 취소 요청
//    private void Request_Pay_Cancle(String pay){
//        String senddata = "";
//        final char fs = PayInfoModel.fs;
//
//        String cancle_select = "0420";
//        String cancle_category = "I4";
//        String cancle_segum = "000000000";
//        String cancle_type = "02";
//        String cancle_pay_num = "00028901";
//        String cancle_pay_day = "231227";
//
//
//        senddata = cancle_select + fs + cancle_category + fs +  fs + pay + fs + cancle_segum + fs + "000000000" + fs + cancle_type + fs + cancle_pay_num + fs + cancle_pay_day + fs + PayInfoModel.pay_apprtid + fs +"123"+ fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs + fs+ fs;
//
//        Intent intent = new Intent();
//        intent.setAction("NICEVCAT");
//        intent.putExtra("NVCATSENDDATA",senddata);
//        intent.setType("text/plain");
//        startActivityForResult(intent,PayInfoModel.SEND_REQUEST_CANCLE);
//    }




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
//                        mClient.connect();
                        mClient.sendDataTransfer(DeviceModel.getDeviceid(), DataTransferModel.getI_Pay(), String.valueOf(jsonObject));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


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
                    Toast.makeText(this,"시간이 지체되었거나 카드리딩에 실패했습니다. 처음페이지로 돌아갑니다.",Toast.LENGTH_LONG).show();
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
//                    viewModel.getCard_read_state().setValue(CardStateModel.Pay_Complete);
//                    if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){              // 왼쪽결제 일때
//
////                        Start_Charging_Rtrofit(viewModel.getLeft_pay_number().getValue());      // 시작시간 업데이트 보내기
//                        viewModel.getleft_device_charging().setValue(true);                     // 충전중 체크
//                        mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_l);               // 정보 페이지로
//                        Start_left_charge_time();                                               // 타이머 시작
//                    }else{                                                                      // 오른쪽결제 일때
//
////                        Start_Charging_Rtrofit(viewModel.getRight_pay_number().getValue());      // 시작시간 업데이트 보내기
//                        viewModel.getright_device_charging().setValue(true);                     // 충전중 체크
//                        mViewPager.setCurrentItem(PageAddrModel.ChargeInfoFgm_r);                // 정보 페이지로
//                        Start_right_charge_time();                                               // 타이머 시작
//                    }

                    try {
//                        mClient.connect();
                        if(mViewPager.getCurrentItem()<PageAddrModel.ChargeSelectFgm){
                            mClient.sendAuthorize(viewModel.getLeft_pay_number().getValue());
                        }else{
                            mClient.sendAuthorize(viewModel.getRight_pay_number().getValue());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    

                }else{
                    Toast.makeText(this,"리더기오류인해 처음페이지로 돌아옵니다.",Toast.LENGTH_LONG).show();
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


        adController = new AD_Controller(imageVideoView,false);       // 광고 컨트롤러 init

        //페이지 전환
        IntentFilter sfilter01 = new IntentFilter();
        sfilter01.addAction("Page");
        LocalBroadcastManager.getInstance(this).registerReceiver(pageReceiver,sfilter01);
//        registerReceiver(pageReceiver,sfilter01);

        IntentFilter chargingFilter = new IntentFilter();
        chargingFilter.addAction("ChargeTimer");
        chargingFilter.addAction("Get_Pay");
        chargingFilter.addAction("Start_Charging");
        chargingFilter.addAction("authorize_Receive");
        chargingFilter.addAction("chargeEnd_Receiver");
        chargingFilter.addAction("remote_Start_Charge_Receiver");
        chargingFilter.addAction("reset_Receive");
        chargingFilter.addAction("changeAvailablitity_Receive");
        LocalBroadcastManager.getInstance(this).registerReceiver(ChargingReceiver,chargingFilter);

//        //충전 타이머
//        IntentFilter sfilter02 = new IntentFilter();
//        sfilter02.addAction("ChargeTimer");
//        registerReceiver(chargetimerReceiver,sfilter02);
//
//        //시세 저장
//        IntentFilter sfilter03 = new IntentFilter();
//        sfilter03.addAction("Get_Pay");
//        registerReceiver(g_pay_Receiver,sfilter03);
//
//        //충전 시작
//        IntentFilter sfilter04 = new IntentFilter();
//        sfilter04.addAction("Start_Charging");
//        registerReceiver(startChargingReceiver,sfilter04);
//
//        //권한
//        IntentFilter sfilter05 = new IntentFilter();
//        sfilter05.addAction("authorize_Receive");
//        registerReceiver(authorize_Receiver,sfilter05);
//
//        //충전 중지
//        IntentFilter sfilter06 = new IntentFilter();
//        sfilter06.addAction("chargeEnd_Receiver");
//        registerReceiver(chargeEndReceiver,sfilter06);
//
//        //충전 시작 리모트
//        IntentFilter sfilter07 = new IntentFilter();
//        sfilter07.addAction("remote_Start_Charge_Receiver");
//        registerReceiver(remote_Start_Charge_Receiver,sfilter07);



        viewModel = new ViewModelProvider(this).get(MainViewModel.class);




//        Request_IC_Pay("1500");

//        Request_IC_Cancle_Pay("500");

//        Request_Pay_Cancle("500");

//        Request_Pay_Pay("1000");


//        Request_IC_Pay("1000");


//        Request_RFID_Pay("1000","T00000000001010010182131441");

//        Request_QR_Cancle_Pay("500","281006022481454870170612","2000");



        hb_handler = new Handler(Looper.getMainLooper());
        mv_l_handler = new Handler(Looper.getMainLooper());
        mv_r_handler = new Handler(Looper.getMainLooper());

        mClient = JsonClient.getInstance(getApplicationContext());

        Log.d("버전","1.0.0");


//        Start_hb_thread();

//        HashMap<String,String> input = new HashMap<>();
//        input.put("timestamp", "");
//        input.put("ip", "");
//        input.put("temp", "");
//        input.put("humidity", "");
//        input.put("ws", "");
//        input.put("wd", "");
//        input.put("north_direction", "");
//        input.put("atmospheric_pressure", "");
//        input.put("rainfall", "");
//        input.put("voltage", "");




//        w_retrofitService.GetWhether("dev_02","","","","","","","","","","").enqueue(new Callback<Retrofit_WhetherModel>() {
//            @Override
//            public void onResponse(Call<Retrofit_WhetherModel> call, Response<Retrofit_WhetherModel> response) {
//                Log.d(tag, "retrofit whether : "+String.valueOf(response.body()));
//
////                Retrofit_WhetherModel retrofitadModel = response.body();
////                Log.d(tag,retrofitadModel.getTimestamp());
////                Log.d(tag,retrofitadModel.getIp());
////                Log.d(tag,retrofitadModel.getTemp());
////                Log.d(tag,retrofitadModel.getHumidity());
////                Log.d(tag,retrofitadModel.getWs());
////                Log.d(tag,retrofitadModel.getWd());
////                Log.d(tag,retrofitadModel.getNorth_direction());
////                Log.d(tag,retrofitadModel.getAtmospheric_pressure());
////                Log.d(tag,retrofitadModel.getRainfall());
////                Log.d(tag,retrofitadModel.getVoltage());
//
//            }
//
//            @Override
//            public void onFailure(Call<Retrofit_WhetherModel> call, Throwable t) {
//                Log.d(tag, String.valueOf(t));
//            }
//        });



    }

    Handler hb_handler; // heartbeat
    Thread hb_thread; // heartbeat

    Handler mv_l_handler ;  // metervalues - l
    Thread mv_l_thread;     // metervalues - l

    Handler mv_r_handler ;  // metervalues - r
    Thread mv_r_thread;     // metervalues - r

    public void Start_hb_thread(){
        

        hb_thread = new Thread(() -> hb_handler.postDelayed(()->{
            try {
//                mClient.connect();
                mClient.sendHeartBeat();
//                mClient.disconnect();

                Start_hb_thread();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },hb_timer));

        hb_thread.start();
    }

    public void Start_mv_l_thread(int t_id){

        mv_l_thread = new Thread(() -> mv_l_handler.postDelayed(()->{
            try {
//                mClient.connect();
                mClient.sendMeterValues(1,t_id,"22000");
//                mClient.disconnect();

                Start_mv_l_thread(t_id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        },mv_timer));

        mv_l_thread.start();
    }

    public void Start_mv_r_thread(int t_id){

        mv_r_thread = new Thread(() -> mv_r_handler.postDelayed(()->{
            try {
//                mClient.connect();
                mClient.sendMeterValues(2,t_id,"22000");
//                mClient.disconnect();

                Start_mv_r_thread(t_id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        },mv_timer));

        mv_r_thread.start();
    }
    

    public void Stop_hb_thread() {
        if (hb_thread != null){
            hb_handler.removeCallbacksAndMessages(null);
            hb_thread.interrupt();
            hb_thread = null;
        }
    }

    public void Stop_mv_l_thread() {
        if (mv_l_thread != null){
            mv_l_handler.removeCallbacksAndMessages(null);
            mv_l_thread.interrupt();
            mv_l_thread = null;
        }
    }

    public void Stop_mv_r_thread() {
        if (mv_r_thread != null){
            mv_r_handler.removeCallbacksAndMessages(null);
            mv_r_thread.interrupt();
            mv_r_thread = null;
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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(pageReceiver);           // 페이지 이동
//        unregisterReceiver(chargetimerReceiver);    // 충전 시간
//        unregisterReceiver(g_pay_Receiver);         // 시세 가져오기
//        unregisterReceiver(authorize_Receiver);     // 권한
//        unregisterReceiver(startChargingReceiver);  // 충전 시작
//        unregisterReceiver(chargeEndReceiver);      // 충전 중지
//        unregisterReceiver(remote_Start_Charge_Receiver);   // 충전 시작 리모트


        LocalBroadcastManager.getInstance(this).unregisterReceiver(ChargingReceiver);       // 기능 리시버

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
        Stop_mv_r_thread();
        Stop_mv_l_thread();

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

                // 예시.
                viewModel.getleft_charge_amount().postValue((float) (viewModel.getleft_charge_amount().getValue()+0.1)); // 충전량
                viewModel.getleft_input_pay().postValue((int) (viewModel.getPayPrice().getValue()*viewModel.getleft_charge_amount().getValue())); //결제

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

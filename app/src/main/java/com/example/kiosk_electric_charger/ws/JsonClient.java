package com.example.kiosk_electric_charger.ws;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.model.PageAddrModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.chargetime.ocpp.IClientAPI;
import eu.chargetime.ocpp.feature.profile.ClientCoreEventHandler;
import eu.chargetime.ocpp.feature.profile.ClientCoreProfile;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.*;

/*
 * ChargeTime.eu - Java-OCA-OCPP
 * Copyright (C) 2015-2016 Thomas Volden <tv@chargetime.eu>
 *
 * MIT License
 *
 * Copyright (C) 2016-2018 Thomas Volden
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class JsonClient {
    private IClientAPI client;
    private ClientCoreProfile core;

    private String tag = "JsonClient.java";

    private static JsonClient jsonClient = null;

    private Context mcontext;

    private JsonClient(Context context){
        mcontext = context;
    }


    public static JsonClient getInstance(Context context){


        if(jsonClient == null){
            jsonClient = new JsonClient(context);
        }

        return jsonClient;
    }

    public void connect() throws Exception {

        // The core profile is mandatory
        core = new ClientCoreProfile(new ClientCoreEventHandler() {

            @Override
            public ClearCacheConfirmation handleClearCacheRequest(ClearCacheRequest request) {

                Log.d(tag,"handleClearCacheRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }


            @Override
            public ChangeAvailabilityConfirmation handleChangeAvailabilityRequest(ChangeAvailabilityRequest request) {

                Log.d(tag,"handleChangeAvailabilityRequest : "+request);
                // ... handle event


                return new ChangeAvailabilityConfirmation(AvailabilityStatus.Accepted);
            }

            @Override
            public ChangeConfigurationConfirmation handleChangeConfigurationRequest(ChangeConfigurationRequest request) {

                Log.d(tag,"handleChangeConfigurationRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }

            @Override
            public GetConfigurationConfirmation handleGetConfigurationRequest(GetConfigurationRequest request) {

                Log.d(tag,"handleGetConfigurationRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }



            @Override
            public DataTransferConfirmation handleDataTransferRequest(DataTransferRequest request) {

                Log.d(tag,"handleDataTransferRequest : "+request);
                // ... handle event


                return null; // returning null means unsupported feature
            }

            @Override
            public RemoteStartTransactionConfirmation handleRemoteStartTransactionRequest(RemoteStartTransactionRequest request) {

                Log.d(tag,"handleRemoteStartTransactionRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }

            @Override
            public RemoteStopTransactionConfirmation handleRemoteStopTransactionRequest(RemoteStopTransactionRequest request) {

                Log.d(tag,"handleRemoteStopTransactionRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }

            @Override
            public ResetConfirmation handleResetRequest(ResetRequest request) {

                Log.d(tag,"handleResetRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }

            @Override
            public UnlockConnectorConfirmation handleUnlockConnectorRequest(UnlockConnectorRequest request) {

                Log.d(tag,"handleUnlockConnectorRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }



        });
//        client = new JSONClient(core);
        client = new JsonClientTest(core);


        client.connect("ws://192.168.0.7:8999/ws", null);
    }

    public void sendAuthorize(String idtag) throws Exception {

        // Use the feature profile to help create event
        Request request = core.createAuthorizeRequest(idtag);

        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"AuthorizeConfirm receive : "+s);
        });
    }

    public void sendBootNotification() throws Exception {

        // Use the feature profile to help create event

        String versionName;
        PackageManager packageManager = mcontext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(mcontext.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        BootNotificationRequest request = new BootNotificationRequest(DeviceModel.getDevicevendor(), DeviceModel.getDeviceid());
        request.setFirmwareVersion(versionName);



        // Client returns a promise which will be filled once it receives a confirmation.
        // 통신이 안되면 시작이 안되야함. 거부가 뜨면 제시된 시간만큼 기다렸다가 다시 보내야함
        client.send(request).whenComplete((s, ex) ->{
//            System.out.println(s);
            Log.d(tag,"BootNotificationConfirm receive : "+s);


            BootNotificationConfirmation a = (BootNotificationConfirmation) s;


            Intent intent = new Intent("BootNotificationBC");
            intent.putExtra("interval",a.getInterval() );

            if(a.getStatus() == RegistrationStatus.Accepted){
                intent.putExtra("status",true);
            }else{
                intent.putExtra("status",false);
            }

            mcontext.sendBroadcast(intent);

        } );

    }

    public void sendHeartBeat() throws Exception {
        Log.d(tag,"sendHeartbeatRequst ");
        // Use the feature profile to help create event
        Request request = core.createHeartbeatRequest();

        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"HeartbeatConfirm receive : "+s);
        });
    }

    public void sendStartTransaction(int Connectorid, String idTag, int meterstart) throws Exception {


        // Use the feature profile to help create event
        Request request = core.createStartTransactionRequest(Connectorid,idTag,meterstart,ZonedDateTime.now(ZoneId.of("Asia/Seoul")));

        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"StartTransaction receive : "+s);
        });
    }

    public void sendStopTransaction(String idTag,int meterstop,int transactionid) throws Exception {

        StopTransactionRequest request = new StopTransactionRequest(meterstop,ZonedDateTime.now(ZoneId.of("Asia/Seoul")),transactionid);
        request.setIdTag(idTag);


        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"StopTransaction receive : "+s);
        });
    }

    public void sendDataTransfer(String vendorId,String messageID,String data) throws Exception {

        DataTransferRequest request = new DataTransferRequest(vendorId);
        request.setMessageId(messageID);
        request.setData(data);


        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"DataTransfer receive : "+s);

            DataTransferConfirmation a = (DataTransferConfirmation) s;

            try {
                JSONObject jsonObject = new JSONObject(a.getData());
                int type = jsonObject.getInt("MessageType");
                String _data = jsonObject.getString("Data");
                Log.d(tag, String.valueOf(type));


                switch (type){
                    case 1: // 광고 리스트 받기
//                        JSONArray jsonArray = new JSONArray(_data);
//                        AcceptedADList(jsonArray);
                        break;
                        
                    case 2: //  결제 정보 insert 성공
                        client.disconnect();
                        break;
                        
                    case -1:    // 서버에 규격에 안맞거나 db에 저장이 안되거나 하는 오류  
                        //오류 낫을때 일정기간후 다시 보내거나 따로 log 저장해야함
                        client.disconnect();
                        break;



                }



            } catch (JSONException e) {
                e.printStackTrace();
            }


        });
    }

    public void sendMeterValues(int connectorId,int transactionId, MeterValue meterValue) throws Exception {


        MeterValuesRequest request = new MeterValuesRequest(connectorId);
        request.setTransactionId(transactionId);
        request.setMeterValue(new MeterValue[]{meterValue});



        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"MeterValues receive : "+s);
        });
    }


//    public enum ChargePointErrorCode {
//        ConnectorLockFailure,
//        EVCommunicationError,
//        GroundFailure,
//        HighTemperature,
//        InternalError,
//        LocalListConflict,
//        NoError,
//        OtherError,
//        OverCurrentFailure,
//        OverVoltage,
//        PowerMeterFailure,
//        PowerSwitchFailure,
//        ReaderFailure,
//        ResetFailure,
//        UnderVoltage,
//        WeakSignal
//    }

//    public enum ChargePointStatus {
//        Available,
//        Preparing,
//        Charging,
//        SuspendedEVSE,
//        SuspendedEV,
//        Finishing,
//        Reserved,
//        Unavailable,
//        Faulted
//    }

    public void sendStatusNotification(int connectorId,ChargePointErrorCode errorCode, ChargePointStatus status,String info) throws Exception {



        StatusNotificationRequest request = new StatusNotificationRequest(connectorId,errorCode,status);
        request.setInfo(info);




        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"StatusNotification receive : "+s);
        });
    }



    public void disconnect() {
        client.disconnect();
    }



//    int downlistlenght = 0;
//
//    // 광고 리스트, 광고 다운로드
//    private void AcceptedADList(JSONArray jsonArray){
//
//        String file_path_String = PrefsHelper.ad_path;
//
//        //폴더 없으면 생성
//        if(!new File(file_path_String).exists()){
//            Log.d(tag,"folder make "+ new File(file_path_String).mkdirs());
//        }
//
//        // 해당 경로에 있는 파일 리스트
//        File file_path = new File(file_path_String);
//
//        List<String> f_list = Arrays.asList(file_path.list());
//
//        try{
//            //받아온 광고 리스트 화
//            List<String> ad_list = new ArrayList<>();
//
//            for(int i = 0; i< jsonArray.length(); i++){
//                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
//                String item = jsonObject1.getString("ADId");
//                Log.d(tag,item);
//                //받아온 리스트 스트링으로 담기
//                ad_list.add(item);
//
//                // 해당 기기에 파일이 있는지 확인
//                boolean r = f_list.contains(item);
//
//                //없으면 다운로드
//                if(!r){
//                    down_AD_file(retrofitadModel.getADid());
//
//                    //다운 리스트 길이
//                    downlistlenght ++;
//
//                }
//            }
//
//            // 그외 다른 파일 삭제
//            extra_file_remove(f_list, ad_list);
//
//            //sharedPreferences 에 저장
//            PrefsHelper.setStringArrayPref(PrefsHelper.ad_url_list_key,ad_list);
//            PrefsHelper.write(PrefsHelper.recent_ad_count_key,0);
//
//
//            //새로운 광고 다운로드 없을때
//            if(downlistlenght == 0){
//                down_setting_com = true;
//
//                // 영상 길이가 이미 끝난 시점에서 다운이 다 됬을때 넘어가기
//                if(video_time_com){
//                    nextActivity();
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//    }


}
package com.example.kiosk_electric_charger.ws;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.ColorSpace;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.SharedPreferences.model.KeyModel;
import com.example.kiosk_electric_charger.activity.Disconnected_OCPP_Server_Dialog;
import com.example.kiosk_electric_charger.filemanage.Filedownload;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.retrofit.Model.Retrofit_VersionModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;

import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.chargetime.ocpp.ClientEvents;
import eu.chargetime.ocpp.IClientAPI;
import eu.chargetime.ocpp.feature.profile.ClientCoreEventHandler;
import eu.chargetime.ocpp.feature.profile.ClientCoreProfile;
import eu.chargetime.ocpp.feature.profile.ClientFirmwareManagementEventHandler;
import eu.chargetime.ocpp.feature.profile.ClientFirmwareManagementProfile;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.*;
import eu.chargetime.ocpp.model.firmware.GetDiagnosticsConfirmation;
import eu.chargetime.ocpp.model.firmware.GetDiagnosticsRequest;
import eu.chargetime.ocpp.model.firmware.UpdateFirmwareConfirmation;
import eu.chargetime.ocpp.model.firmware.UpdateFirmwareRequest;
import okhttp3.ResponseBody;
import okhttp3.internal.ws.WebSocketExtensions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private ClientFirmwareManagementProfile firmware;

    private String tag = "JsonClient.java";

    private static JsonClient jsonClient = null;

    private Context mcontext;

    private int TransactionStartMessageAttempts = 0;
    private int TransactionStopMessageAttempts = 0;

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


        firmware = new ClientFirmwareManagementProfile(new ClientFirmwareManagementEventHandler() {
            @Override
            public GetDiagnosticsConfirmation handleGetDiagnosticsRequest(GetDiagnosticsRequest request) {

                Log.d(tag,"handleGetDiagnosticsRequest : "+request);

                return null;
            }

            @Override
            public UpdateFirmwareConfirmation handleUpdateFirmwareRequest(UpdateFirmwareRequest request) {

                Log.d(tag,"handleUpdateFirmwareRequest : "+request);




                // 앱 버전 가져오기
                String versionName;
                PackageManager packageManager = mcontext.getPackageManager();
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(mcontext.getPackageName(), 0);
                    versionName = packageInfo.versionName;
                    Log.d(tag,versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }

                String[] a = request.getLocation().split("\\$\\$");
                Log.d(tag,a[0]);
                Log.d(tag,a[1]);


                // 앱 다운로드 프로세스
                apk_update(versionName,a[0],a[1]);

                return new UpdateFirmwareConfirmation();
            }
        });

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


                Intent intent = new Intent("changeAvailablitity_Receive");
                intent.putExtra("Connectore_id",request.getConnectorId());
                intent.putExtra("Type",request.getType());
                LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);



                return new ChangeAvailabilityConfirmation(AvailabilityStatus.Accepted);
            }

            @Override
            public ChangeConfigurationConfirmation handleChangeConfigurationRequest(ChangeConfigurationRequest request) {

                Log.d(tag,"handleChangeConfigurationRequest : "+request);
                // ... handle event

                switch (request.getKey()){
                    case "WebSocketPingInterval":
                        PrefsHelper.write(request.getKey(),request.getValue());
                        break;


                }




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



                if(request.getIdTag().equals(DeviceModel.getDeviceid())){
                    Intent intent = new Intent("remote_Start_Charge_Receiver");
                    intent.putExtra("connector_id",request.getConnectorId());

                    LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);
                }

                return new RemoteStartTransactionConfirmation(RemoteStartStopStatus.Accepted); // returning null means unsupported feature
            }

            @Override
            public RemoteStopTransactionConfirmation handleRemoteStopTransactionRequest(RemoteStopTransactionRequest request) {

                Log.d(tag,"handleRemoteStopTransactionRequest : "+request);
                // ... handle event

                Intent intent = new Intent("ChargeTimer");
                intent.putExtra("pay_num",request.getTransactionId());
                LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);


                new RemoteStartTransactionConfirmation(RemoteStartStopStatus.Accepted);
                return null; // returning null means unsupported feature
            }

            @Override
            public ResetConfirmation handleResetRequest(ResetRequest request) {

                Log.d(tag,"handleResetRequest : "+request);
                // ... handle event

                Intent intent = new Intent("reset_Receive");       // 만약 충전중이라면 해제하고 진행
                LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);
                

                return new ResetConfirmation(ResetStatus.Accepted); // returning null means unsupported feature
            }

            @Override
            public UnlockConnectorConfirmation handleUnlockConnectorRequest(UnlockConnectorRequest request) {

                Log.d(tag,"handleUnlockConnectorRequest : "+request);
                // ... handle event

                return null; // returning null means unsupported feature
            }



        });
//        client = new JSONClient(core);


        client = new JsonClientTest(core,firmware);

        

        ClientEvents events = new ClientEvents() {
            @Override
            public void connectionOpened() {
                Log.e("connecnt err","ConnectOpened");
            }

            @Override
            public void connectionClosed() {


                client.connect("ws://192.168.0.7:8999/ws", this);

                Log.e("connecnt err","ConnectClosed");
            }
        };



        client.connect("ws://192.168.0.7:8999/ws", events);






    }


    public void sendAuthorize(String idtag) throws Exception {

        // Use the feature profile to help create event
        Request request = core.createAuthorizeRequest(idtag);

        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"AuthorizeConfirm receive : "+s);

            AuthorizeConfirmation a = (AuthorizeConfirmation) s;

            if(a.getIdTagInfo().getStatus() == AuthorizationStatus.Accepted){
                Intent intent = new Intent("authorize_Receive");       // 권한획득
                LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);

            }else{
                Toast.makeText(mcontext,"허가를 받지않은 기기에서 충전을 시도했습니다.",Toast.LENGTH_LONG).show();
            }
//            client.disconnect();


        });
    }


    public void sendStopAuthorize(int idtag) throws Exception  {


        // Use the feature profile to help create event
        Request request = core.createAuthorizeRequest(String.valueOf(idtag));

        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"AuthorizeConfirm receive : "+s);

            AuthorizeConfirmation a = (AuthorizeConfirmation) s;

            if(a.getIdTagInfo().getStatus() == AuthorizationStatus.Accepted){
                try {

                    sendStopTransaction(11000,idtag,ChargePointStatus.Finishing);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }else{
                Toast.makeText(mcontext,"인증이 되지 않은 사용자입니다. ",Toast.LENGTH_LONG).show();
            }


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

            LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);




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
        Request request = null;
        
        try{
            request = core.createStartTransactionRequest(Connectorid,idTag,meterstart, ZonedDateTime.now(ZoneId.of("Asia/Seoul")));
        }catch (Exception e){
            e.printStackTrace();
        }



        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"StartTransaction receive : "+s);
            Log.d(tag,"StartTransaction receive err : "+ex);

            StartTransactionConfirmation a = (StartTransactionConfirmation) s;

            if(a.getIdTagInfo().getStatus() == AuthorizationStatus.Accepted){
                Intent intent = new Intent("Start_Charging");       // 충전시작
                intent.putExtra("connect_id",Connectorid);
                LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);
//                client.disconnect();
            }else{
                Toast.makeText(mcontext,"서버에서 충전을 거부하였습니다.",Toast.LENGTH_LONG).show();      // 거부되었을때 다시 통신시도해야함 (22p).

                if(TransactionStartMessageAttempts<2){           // 거부되었을때 2번정도 다시보냄
                    TransactionStartMessageAttempts++;
                    try {
                        Thread.sleep(10000);

                        sendStartTransaction(Connectorid,idTag,meterstart);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    TransactionStartMessageAttempts = 0;
                    Intent intent = new Intent("Start_Charging");       // 충전시작
                    LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);
//                    client.disconnect();
                }

            }

        });
    }

    public void sendStopTransaction(int meterstop,int transactionid,ChargePointStatus chargePointStatus) throws Exception {
//        SampledValue bbb = new SampledValue("50.0");
//        bbb.setMeasurand("Frequency");
//        bbb.setUnit("Wh");
//        SampledValue[] bbbb = new SampledValue[]{bbb};
//        MeterValue bb = new MeterValue(ZonedDateTime.now(ZoneId.of("Asia/Seoul")),bbbb);
//        MeterValue[] b = new MeterValue[]{bb};


        Log.d(tag, "date : "+ ZonedDateTime.now(ZoneId.of("Asia/Seoul")));

        StopTransactionRequest request = new StopTransactionRequest(meterstop,ZonedDateTime.now(ZoneId.of("Asia/Seoul")),transactionid);
        request.setReason(Reason.Local);


        Log.d(tag, String.valueOf(request));


        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {

            Log.e(tag, String.valueOf(ex));

            StopTransactionConfirmation a = (StopTransactionConfirmation) s;


            Intent intent = new Intent("chargeEnd_Receiver");       // 충전중단
            intent.putExtra("pay_num",String.valueOf(transactionid));
            intent.putExtra("ChargePointStatus",chargePointStatus);
            LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);


            if(a.getIdTagInfo().getStatus() == AuthorizationStatus.Accepted){
//                client.disconnect();
            }else{
                Toast.makeText(mcontext,"서버에서 충전종료를 거부하였습니다.",Toast.LENGTH_LONG).show();      // 거부되었을때 다시 통신시도해야함 (22p).

                if(TransactionStopMessageAttempts<2){           // 거부되었을때 2번정도 다시보냄
                    TransactionStopMessageAttempts++;
                    try {
                        Thread.sleep(10000);

                        sendStopTransaction( meterstop,transactionid,chargePointStatus);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    TransactionStopMessageAttempts = 0;
                }

            }


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
//                        client.disconnect();
                        break;

                    case 3: //  시세 가져오기 성공
                        JSONObject jsonObject1 = new JSONObject(_data);
                        float pay = jsonObject1.getInt("Pay");

                        Intent intent = new Intent("Get_Pay");
                        intent.putExtra("pay",pay);
                        LocalBroadcastManager.getInstance(mcontext).sendBroadcast(intent);

//                        client.disconnect();
                        break;
                        
                    case -1:    // 서버에 규격에 안맞거나 db에 저장이 안되거나 하는 오류  
                        //오류 낫을때 일정기간후 다시 보내거나 따로 log 저장해야함
//                        client.disconnect();
                        break;



                }



            } catch (JSONException e) {
                e.printStackTrace();
            }


        });
    }

    public void sendMeterValues(int connectorId,int transactionId, String amount) throws Exception {

        SampledValue bbb = new SampledValue(amount);
//        bbb.setMeasurand("Frequency");
//        bbb.setUnit("Wh");
        SampledValue[] bbbb = new SampledValue[]{bbb};
        MeterValue bb = new MeterValue(ZonedDateTime.now(ZoneId.of("Asia/Seoul")),bbbb);
        MeterValue[] b = new MeterValue[]{bb};

        MeterValuesRequest request = new MeterValuesRequest(connectorId);
        request.setTransactionId(transactionId);
        request.setMeterValue(b);



        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"MeterValues receive : "+s);
            Log.d(tag,"MeterValues receive : "+ex);
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



    // 권한 부여와 충전 시작,끝 사이에 유효성 충돌이 일어날때도 보내 오류를 알려야함(19p)
    //이전 OCPP 버전에 정의된 점유 상태 는 더 이상 관련이 없습니다. Occupied 상태 는 준비, 충전 중, SuspendedEV,SuspendedEVSE 및 완료 라는 5가지 새로운 상태로 나뉩니다 (43p)(82p)
    //전환 횟수를 제한하기 위해 충전 포인트는 선택적 구성 키인 MinimalStatusDuration에 정의된 시간보다 짧은 시간 동안 활성화된 경우 StatusNotification.req 전송을 생략할 수 있습니다 . 이런 방식으로 청구 지점은 특정 StatusNotification.req PDU를 보내지 않도록 선택할 수 있습니다 .(47p)

    public void sendStatusNotification(int connectorId,ChargePointErrorCode errorCode, ChargePointStatus status,String info) throws Exception {



        StatusNotificationRequest request = new StatusNotificationRequest(connectorId,errorCode,status);

        if(info !=null){
            request.setInfo(info);
        }





        // Client returns a promise which will be filled once it receives a confirmation.
        client.send(request).whenComplete((s, ex) -> {
            Log.d(tag,"StatusNotification receive : "+s);
            Log.d(tag,"StatusNotification receive : "+ex);

//            if(ex == null){
//                sendMeterValues();
//            }
        });
    }



    public void disconnect() {
        client.disconnect();
    }

    public boolean isClosed(){
        return client.isClosed();
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



    public void apk_update(String versionName,String receive_vn,String path){
        // 해당 경로에 있는 파일 리스트
        File file_path = new File(PrefsHelper.apk_path);

        //폴더 없으면 생성
        if(!file_path.exists()){
            file_path.mkdirs();
        }


        // 버전이 다르면
        if(!(versionName.equals(receive_vn))){

            down_Apk_file("new_apk.zip",path);


        }
    }


    private void down_Apk_file(String fileName,String path){
        Log.d(tag,"Path = "+path);

        HashMap<String,String> input = new HashMap<>();
        input.put("device_id", DeviceModel.getDeviceid());

        File file_path = new File(PrefsHelper.apk_path);
        List<String> f_list = Arrays.asList(file_path.list());

        for(String a : f_list){
            Log.d("apk-a",a);
        }

        Retrofit_Interface s_retrofitService = Retrofit_Factory.create();
        s_retrofitService.downApk(path,input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // 압축파일 다운
                if(Filedownload.writeResponseBodyToDisk(response.body(),PrefsHelper.apk_path + File.separator+ fileName)){
                    Log.d("zip down", "if ");
//                    // 압축 풀기
                    if(Filedownload.unzip(PrefsHelper.apk_path + File.separator+ fileName,PrefsHelper.apk_path)){
                        try {
                            _installPackage(new FileInputStream(new File(PrefsHelper.apk_path + File.separator + "app-release.apk").getPath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("download ad fail",t.getMessage());
            }
        });

    }


    public void _installPackage(InputStream inputStream)
            throws IOException {

        PackageInstaller packageInstaller = mcontext.getPackageManager().getPackageInstaller();
        int sessionId = packageInstaller.createSession(new PackageInstaller
                .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);

        long sizeBytes = 0;

        OutputStream out = null;
        out = session.openWrite("my_app_session", 0, sizeBytes);

        int total = 0;
        byte[] buffer = new byte[65536];
        int c;
        while ((c = inputStream.read(buffer)) != -1) {
            total += c;
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        inputStream.close();
        out.close();

        session.commit(createIntentSender(mcontext,sessionId));

//        Intent i = new Intent(LauncherReceiver.START_INTENT);
//        mcontext.sendBroadcast(i);

    }

    private static IntentSender createIntentSender(Context context, int sessionId) {

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
//                new Intent(LauncherReceiver.START_INTENT),
                new Intent(Intent.ACTION_MY_PACKAGE_REPLACED),
                PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent.getIntentSender();

//        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.example.kiosk_electric_charger");
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        return pendingIntent.getIntentSender();
    }

}
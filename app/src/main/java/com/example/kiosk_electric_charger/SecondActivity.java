package com.example.kiosk_electric_charger;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;



import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.filemanage.Filedownload;
import com.example.kiosk_electric_charger.model.DeviceModel;
import com.example.kiosk_electric_charger.receiver.ADReceiver;
import com.example.kiosk_electric_charger.receiver.LauncherReceiver;
import com.example.kiosk_electric_charger.retrofit.Retrofit_ADModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Factory;
import com.example.kiosk_electric_charger.retrofit.Retrofit_Interface;
import com.example.kiosk_electric_charger.retrofit.Retrofit_PayModel;
import com.example.kiosk_electric_charger.retrofit.Retrofit_VersionModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class SecondActivity extends AppCompatActivity {

    private final Retrofit_Interface retrofitService = Retrofit_Factory.create();
    Button charger_btn ;
    Button advertise_list_down_btn ;
    Button app_update_btn ;
    Button pay_btn ;
    TextView version_tv;






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEND_REQUEST_CHKVALID ) {
            // 요청 정상
            if (resultCode != RESULT_OK) {

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT success",return_value + " "+return_data);
            // 요청 실패
            }else if(resultCode == RESULT_CANCELED){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT err",return_value + " "+return_data);
            // 예외처리
            }else{
                Log.d("NVCAT err","예외");
            }
        }else if(requestCode == SEND_REQUEST_NORMAL ){
            // 요청 정상
            if (resultCode != RESULT_OK) {

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT success",return_value + " "+return_data);
                // 요청 실패
            }else if(resultCode == RESULT_CANCELED){

                int return_value = data.getIntExtra("NVCATRETURNCODE",-99);
                String return_data = data.getStringExtra("NVCATRECVDATA");
                Log.d("NVCAT err",return_value + " "+return_data);
                // 예외처리
            }else{
                Log.d("NVCAT err","예외");
            }
        }else{

        }
        
        
        
        
        
    }

    //리더기 상호인증 요청
    private final int SEND_REQUEST_CHKVALID  = 100;
    
    //,nvcat 재시작,강제종료 요청, 리더기 정보요청,환경설정,카드삽입여부,파일권한 허용요청,RF잔액조회 및 카드번호 요청,사용자 선택버튼기능 요청
    private final int SEND_REQUEST_NORMAL = 200;
    
    //POS에서 NVCAT으로 결제요청
    private final int SEND_REQUEST_CODE = 300;


    //    카드리더기 상호인증 요청
    public void Check_CardReaderDevice(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","CHKVALID");
        intent.setType("text/plain");
        startActivityForResult(intent,SEND_REQUEST_CHKVALID );
    }

    //    NVCAT 재시작 요청
    public void NVCAT_Restart(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","RESTART");
        intent.setType("text/plain");
        startActivityForResult(intent,SEND_REQUEST_NORMAL );
    }

    // 리더기 정보 요청
    public void Request_CardReader_Info(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","GETREADERINFO");
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

    //POS에서 NVCAT으로 결제 요청
    public void Request_Pay(){

    }

    //NVCAT 강제종료 요청
    public void Close_NVCAT(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","SHUTDOWN");
        intent.setType("text/plain");
        startActivityForResult(intent,SEND_REQUEST_NORMAL);
    }

    //RF 잔액 조회 및 카드번호 요청
    public void Requst_RF_Card_Info(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","REQBALANCE");
        intent.setType("text/plain");
        startActivityForResult(intent,SEND_REQUEST_NORMAL);
    }

    //사용자 선택 버튼 기능 요청   -- 멀티패드1 , 터치스크린2
    public void User_Interface_Window(){
        Intent intent = new Intent();
        intent.setAction("NICEVCAT");
        intent.putExtra("NVCATSENDDATA","REQSELECTBTN ");
        intent.setType("text/plain");
        intent.putExtra("TYPE","1");
        intent.putExtra("SENDDATA","동의하시겠습니까?");
        startActivityForResult(intent,SEND_REQUEST_NORMAL);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


        // 앱 버전 가져오기
        String versionName;
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


        charger_btn = findViewById(R.id.second_btn01);
        advertise_list_down_btn = findViewById(R.id.second_btn02);
        app_update_btn = findViewById(R.id.second_btn03);
        pay_btn = findViewById(R.id.second_btn04);

        version_tv = findViewById(R.id.second_version_tv);
        version_tv.setText(versionName);




        charger_btn.setOnClickListener(v -> {
            //됨
//            Check_CardReaderDevice();     --  -16 리더기 오류
//            NVCAT_Restart();              --- 해당 페이지가 하나 더 떠서 터치가 막힘.
//            Request_CardReader_Info();    --  -16 리더기 오류




            //안됨
//            User_Interface_Window();      -- -14 오류
//            Check_Card_Input();           --- 해당 페이지가 하나 더 떠서 터치가 막힘. 반환안됨. 리더기 달아봐야함
        });


        advertise_list_down_btn.setOnClickListener(v -> {


            HashMap<String,String> input = new HashMap<>();
            input.put("device_id", DeviceModel.getDeviceid());


            // 해당 경로에 있는 파일 리스트
            File file_path = new File(PrefsHelper.ad_path);

            //폴더 없으면 생성
            if(!file_path.exists()){
                Log.d("folder make ", String.valueOf(file_path.mkdirs()));
            }

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
                            }

                            Log.d("success",retrofitadModel.getADid()+ " "+ r);

                        }

                        // 그외 다른 파일 삭제
                        extra_file_remove(f_list, ad_list);
                        
                        //sharedPreferences 에 저장
                        PrefsHelper.setStringArrayPref(PrefsHelper.ad_url_list_key,ad_list);
                        PrefsHelper.write(PrefsHelper.recent_ad_count_key,0);



                    }

                }
                @Override
                public void onFailure(Call<List<Retrofit_ADModel>> call, Throwable t) {
                    Log.e("get list fail",t.getMessage());
                }
            });


        });


        app_update_btn.setOnClickListener(v -> {
            retrofitService.getApkVersion().enqueue(new Callback<Retrofit_VersionModel>() {
                @Override
                public void onResponse(Call<Retrofit_VersionModel> call, Response<Retrofit_VersionModel> response) {
                    if(response.isSuccessful()){


                        // 해당 경로에 있는 파일 리스트
                        File file_path = new File(PrefsHelper.apk_path);

                        //폴더 없으면 생성
                        if(!file_path.exists()){
                            file_path.mkdirs();
                        }


                        Retrofit_VersionModel retrofitVersionModel = response.body();


                        Log.d("version name",retrofitVersionModel.getVersion() + " " +versionName);

                        // 버전이 다르면
                        if(!(retrofitVersionModel.getVersion().equals(versionName))){

                            down_Apk_file("new_apk.zip");


                        }


                    }
                }

                @Override
                public void onFailure(Call<Retrofit_VersionModel> call, Throwable t) {
                    Log.d("fail",t.getMessage());
                }


            });
        });


        pay_btn.setOnClickListener(v -> {
            retrofitService.getPay().enqueue(new Callback<Retrofit_PayModel>() {
                @Override
                public void onResponse(Call<Retrofit_PayModel> call, Response<Retrofit_PayModel> response) {
                    if(response.isSuccessful()){
                        Retrofit_PayModel retrofitPayModel = response.body();
                        Log.d("success",String.valueOf(retrofitPayModel.getPay()));
                    }

                }

                @Override
                public void onFailure(Call<Retrofit_PayModel> call, Throwable t) {
                    Log.d("fail",t.getMessage());
                }
            });
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

    private void down_AD_file(String fileName){
        HashMap<String,String> input = new HashMap<>();
        input.put("ad_id",fileName);


        retrofitService.downAd(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                 Filedownload.writeResponseBodyToDisk(response.body(),PrefsHelper.ad_path + File.separator+ fileName);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("download ad fail",t.getMessage());
            }
        });
    }


    private void down_Apk_file(String fileName){
        HashMap<String,String> input = new HashMap<>();
        input.put("device_id", DeviceModel.getDeviceid());

        File file_path = new File(PrefsHelper.apk_path);
        List<String> f_list = Arrays.asList(file_path.list());

        for(String a : f_list){
            Log.d("apk-a",a);
        }


        retrofitService.downApk(input).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // 압축파일 다운
                if(Filedownload.writeResponseBodyToDisk(response.body(),PrefsHelper.apk_path + File.separator+ fileName)){
                    Log.d("zip down", "if ");
//                    // 압축 풀기
                    if(Filedownload.unzip(PrefsHelper.apk_path + File.separator+ fileName,PrefsHelper.apk_path,getApplicationContext())){
                        try {
                            _installPackage(new FileInputStream(new File(PrefsHelper.apk_path + File.separator + "app-debug.apk").getPath()));
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

        PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
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

        session.commit(createIntentSender(this,sessionId));

        Intent i = new Intent(LauncherReceiver.START_INTENT);
        sendBroadcast(i);
    }

    private static IntentSender createIntentSender(Context context, int sessionId) {

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(LauncherReceiver.START_INTENT),
                PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent.getIntentSender();
    }



}


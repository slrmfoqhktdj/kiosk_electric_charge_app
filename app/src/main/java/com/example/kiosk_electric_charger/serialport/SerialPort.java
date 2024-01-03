package com.example.kiosk_electric_charger.serialport;
//
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kiosk_electric_charger.model.MainViewModel;
import com.example.kiosk_electric_charger.serialport_api.ztl.serialOperator.SerialOperator;

import java.text.SimpleDateFormat;

public class SerialPort {

    private SerialOperator mySerialOperator;
    String Spin_Baudrate_num;
    String Serial_port_name;

    int sendtype = 1;//发送类型 0 = 文本 1 = hex

    String tag = "SerialPort";

    int send = 0;

    int take = 0;

    public void Serialport_Open(MainViewModel viewModel){
        if (mySerialOperator != null){
            mySerialOperator.close();
            mySerialOperator = null;
        }



        mySerialOperator = new SerialOperator(viewModel);
        mySerialOperator.setBaudRate(Spin_Baudrate_num);
        mySerialOperator.setPort("/dev/" + Serial_port_name);
        mySerialOperator.setHandler(mHandler);
        Log.d("직렬 포트", "open\r");

        Log.d(tag,"직렬 포트를 성공적으로 열었습니다.");

        try {
            mySerialOperator.open();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void Serialport_SendMessage(String message){
        if (mySerialOperator == null || mySerialOperator.isOpen() == false) {
            Log.d(tag,"먼저 직렬 포트를 열어주세요");
            return;
        }


        if (sendtype == 0) {
            mySerialOperator.send(message.getBytes());
        } else {
            message.replace("\r", "");
            message.replace("\n", "");
            message.replace(" ", "");

            if (message.length() % 2 != 0)
                message += "0";
            mySerialOperator.send(hexStringToBytes(message));

        }
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }



    public static String bytes2HexString(byte[] b, int len) {
        String ret = "";
        for (int i = 0; i < len; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    //拿到数据,完成主界面更新  - 데이터 가져오기 및 기본 인터페이스 업데이트 완료
//              String data = (String)msg.obj;
                    byte[] bytes = (byte[]) msg.obj;
                    int arg1 = msg.arg1;
                    String strRecv;
                    if (sendtype == 0) {
                        strRecv = new String(bytes, 0, arg1);
                    } else {
                        strRecv = bytes2HexString(bytes, arg1);
                        StringBuilder stringBuilder2 = new StringBuilder(strRecv);

                        for (int i = strRecv.length(); i > 0; i--) {
                            if (i % 2 == 0)
                                stringBuilder2.insert(i, " ");
                        }

                        strRecv = stringBuilder2.toString();
                    }

                    SimpleDateFormat dateformat = new SimpleDateFormat("[HH:mm:ss]");
                    final String Send = dateformat.format(System.currentTimeMillis());

//              txt_recv.setText(data);

                    StringBuilder sMsg = new StringBuilder();
                    sMsg.append("\r\n");
                    sMsg.append(Send + strRecv);

                    Log.d(tag, String.valueOf(sMsg));
//                    txt_comrecv.append(sMsg);
                    send += arg1;

                    Log.d(tag,send+"보내고 "+take+ "받기");
//                    txt_log.setText(String.format("发%d收%d", send, take));

                    //txt_log.setText(Long.toString(ss));

                    break;
                default:
                    break;
            }
        }

    };


}

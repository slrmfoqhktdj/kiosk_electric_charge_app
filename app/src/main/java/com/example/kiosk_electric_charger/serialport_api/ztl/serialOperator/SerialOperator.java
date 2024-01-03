package com.example.kiosk_electric_charger.serialport_api.ztl.serialOperator;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.kiosk_electric_charger.model.MainViewModel;
import com.example.kiosk_electric_charger.serialport_api.SerialOperatorHelper;

import java.util.Arrays;

import com.example.kiosk_electric_charger.serialport_api.ComBean;

public class SerialOperator extends SerialOperatorHelper {

    String tag = "SerialOperator";

    MainViewModel mviewModel;

    public SerialOperator(MainViewModel viewModel) {
        mviewModel = viewModel;
    }

    @Override
    protected void onDataReceived(ComBean ComRecData) {
        // TODO Auto-generated method stub
//		DispQueue.AddQueue(ComRecData);//线程定时刷新显示(推荐)
//        System.out.println(Arrays.toString(ComRecData.bRec));
        Log.d(tag, Arrays.toString(ComRecData.bRec));



    }

}

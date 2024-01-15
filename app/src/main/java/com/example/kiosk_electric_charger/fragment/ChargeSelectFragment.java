package com.example.kiosk_electric_charger.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.model.MainViewModel;
import com.example.kiosk_electric_charger.model.PageAddrModel;

public class ChargeSelectFragment extends Fragment {


    private String Tag = "Charge_Select_Fragment";

    public ChargeSelectFragment() {

    }

    ImageButton charge_01_btn,charge_02_btn,charge_select_left_charge_detail_btn,charge_select_right_charge_detail_btn;

    TextView charge_select_left_charge_persent_tv,charge_select_left_time_tv,charge_select_left_setting_pay_tv,charge_select_left_input_pay_tv,charge_select_left_charge_amount_tv;
    TextView charge_select_right_charge_persent_tv,charge_select_right_time_tv,charge_select_right_setting_pay_tv,charge_select_right_input_pay_tv,charge_select_right_charge_amount_tv;
    ConstraintLayout rootcontainer_left_charging,rootcontainer_left_noncharging,rootcontainer_right_charging,rootcontainer_right_noncharging,rootcontainer_left_unablecharging,rootcontainer_right_unablecharging;
    MainViewModel veiwModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(Tag,"onCreate");

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_charge_select, container, false);


        rootcontainer_left_noncharging = view.findViewById(R.id.charge_select_left_notcharge_container);        //왼쪽 충전중 아닐때
        rootcontainer_left_charging = view.findViewById(R.id.charge_select_left_charge_container);              //왼쪽 충전중 일때
        rootcontainer_right_noncharging = view.findViewById(R.id.charge_select_right_notcharge_container);      //오른쪽 충전중 아닐때
        rootcontainer_right_charging = view.findViewById(R.id.charge_select_right_charge_container);            //오른쪽 충전중 일때
        rootcontainer_left_unablecharging = view.findViewById(R.id.charge_select_left_unable_charge_container);    //왼쪽 사용 중지
        rootcontainer_right_unablecharging = view.findViewById(R.id.charge_select_right_unable_charge_container);    // 오른쪽 사용 중지

        charge_select_left_charge_persent_tv = view.findViewById(R.id.charge_select_left_charge_persent_tv);     // 왼쪽 충전 진행량
        charge_select_left_time_tv = view.findViewById(R.id.charge_select_left_time_tv);                // 왼쪽 충전 시간
        charge_select_left_setting_pay_tv = view.findViewById(R.id.charge_select_left_setting_pay_tv);          // 왼쪽 설정 금액
        charge_select_left_input_pay_tv = view.findViewById(R.id.charge_select_left_input_pay_tv);            // 왼쪽 충전 금액
        charge_select_left_charge_amount_tv = view.findViewById(R.id.charge_select_left_charge_amount_tv);        // 왼쪽 충전량

        charge_select_right_charge_persent_tv = view.findViewById(R.id.charge_select_right_charge_persent_tv);     // 오른쪽 충전 진행량
        charge_select_right_time_tv = view.findViewById(R.id.charge_select_right_time_tv);                // 오른쪽 충전 시간
        charge_select_right_setting_pay_tv = view.findViewById(R.id.charge_select_right_setting_pay_tv);          // 오른쪽 설정 금액
        charge_select_right_input_pay_tv = view.findViewById(R.id.charge_select_right_input_pay_tv);            // 오른쪽 충전 금액
        charge_select_right_charge_amount_tv = view.findViewById(R.id.charge_select_right_charge_amount_tv);        // 오른쪽 충전량

        charge_select_left_charge_detail_btn = view.findViewById(R.id.charge_select_left_charge_detail_btn);    //왼쪽 자세히 보기 버튼
        charge_select_right_charge_detail_btn = view.findViewById(R.id.charge_select_right_charge_detail_btn);   //오른쪽 자세히 보기 버튼

        charge_01_btn = view.findViewById(R.id.charge_01);      // 왼쪽 충전하기 버튼
        charge_02_btn = view.findViewById(R.id.charge_02);      // 오른쪽 충전하기 버튼

        charge_01_btn.setOnClickListener(v -> {
            Intent intent = new Intent("Page");
            intent.putExtra("page", PageAddrModel.PaySelectFgm_l);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        });


        charge_02_btn.setOnClickListener(v -> {
            Intent intent = new Intent("Page");
            intent.putExtra("page",PageAddrModel.PaySelectFgm_r);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        });

        charge_select_left_charge_detail_btn.setOnClickListener(v -> {
            Intent intent = new Intent("Page");
            intent.putExtra("page", PageAddrModel.ChargeInfoFgm_l);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        });

        charge_select_right_charge_detail_btn.setOnClickListener(v -> {
            Intent intent = new Intent("Page");
            intent.putExtra("page", PageAddrModel.ChargeInfoFgm_r);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        });




        return view;
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(Tag,"onViewCreated");


        veiwModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);



        //livedata 바뀔시에 이벤트
        final Observer<Integer> left_charge_per = b -> {
            charge_select_left_charge_persent_tv.setText(String.valueOf(b));
        };
        final Observer<String> left_charge_time = b -> {
            charge_select_left_time_tv.setText(b);
        };
        final Observer<Integer> left_setting_pay = b -> {
            charge_select_left_setting_pay_tv.setText(String.valueOf(b));
        };
        final Observer<Integer> left_input_pay = b -> {
            charge_select_left_input_pay_tv.setText(String.valueOf(b));
        };
        final Observer<Float> left_charge_amount = b -> {
            charge_select_left_charge_amount_tv.setText(String.valueOf(b));
        };


        //livedata 바뀔시에 이벤트
        final Observer<Integer> right_charge_per = b -> {
            charge_select_right_charge_persent_tv.setText(String.valueOf(b));
        };
        final Observer<String> right_charge_time = b -> {
            charge_select_right_time_tv.setText(b);
        };
        final Observer<Integer> right_setting_pay = b -> {
            charge_select_right_setting_pay_tv.setText(String.valueOf(b));
        };
        final Observer<Integer> right_input_pay = b -> {
            charge_select_right_input_pay_tv.setText(String.valueOf(b));
        };
        final Observer<Float> right_charge_amount = b -> {
            charge_select_right_charge_amount_tv.setText(String.valueOf(b));
        };


        //livedata 바뀔시에 이벤트
        final Observer<Boolean> left_device_charging = b -> {
            if(b){
                rootcontainer_left_charging.setVisibility(View.VISIBLE);
                rootcontainer_left_noncharging.setVisibility(View.GONE);
                rootcontainer_left_unablecharging.setVisibility(View.GONE);

                veiwModel.getleft_charge_persent().observe(getActivity(),left_charge_per);
                veiwModel.getleft_charge_time().observe(getActivity(),left_charge_time);
                veiwModel.getleft_payValue().observe(getActivity(),left_setting_pay);
                veiwModel.getleft_input_pay().observe(getActivity(),left_input_pay);
                veiwModel.getleft_charge_amount().observe(getActivity(),left_charge_amount);
            }else{
                rootcontainer_left_noncharging.setVisibility(View.VISIBLE);
                rootcontainer_left_charging.setVisibility(View.GONE);
                rootcontainer_left_unablecharging.setVisibility(View.GONE);
            }

        };


        final Observer<Boolean> right_device_charging = b -> {
            if(b){
                rootcontainer_right_charging.setVisibility(View.VISIBLE);
                rootcontainer_right_noncharging.setVisibility(View.GONE);
                rootcontainer_right_unablecharging.setVisibility(View.GONE);

                veiwModel.getright_charge_persent().observe(getActivity(),right_charge_per);
                veiwModel.getright_charge_time().observe(getActivity(),right_charge_time);
                veiwModel.getright_payValue().observe(getActivity(),right_setting_pay);
                veiwModel.getright_input_pay().observe(getActivity(),right_input_pay);
                veiwModel.getright_charge_amount().observe(getActivity(),right_charge_amount);
            }else{
                rootcontainer_right_noncharging.setVisibility(View.VISIBLE);
                rootcontainer_right_charging.setVisibility(View.GONE);
                rootcontainer_right_unablecharging.setVisibility(View.GONE);
            }

        };


        //livedata 사용가능 바뀔시에
        final Observer<Boolean> left_device_charging_enable = b -> {
            if(!b){
                rootcontainer_left_charging.setVisibility(View.GONE);
                rootcontainer_left_noncharging.setVisibility(View.GONE);
                rootcontainer_left_unablecharging.setVisibility(View.VISIBLE);
            }else{
                rootcontainer_left_noncharging.setVisibility(View.VISIBLE);
                rootcontainer_left_charging.setVisibility(View.GONE);
                rootcontainer_left_unablecharging.setVisibility(View.GONE);
            }

        };


        final Observer<Boolean> right_device_charging_enable = b -> {
            if(!b){
                rootcontainer_right_charging.setVisibility(View.GONE);
                rootcontainer_right_noncharging.setVisibility(View.GONE);
                rootcontainer_right_unablecharging.setVisibility(View.VISIBLE);
            }else{
                rootcontainer_right_noncharging.setVisibility(View.VISIBLE);
                rootcontainer_right_charging.setVisibility(View.GONE);
                rootcontainer_right_unablecharging.setVisibility(View.GONE);
            }

        };

        //livedata 등록
        veiwModel.getleft_device_charging().observe(getActivity(),left_device_charging);
        veiwModel.getright_device_charging().observe(getActivity(),right_device_charging);

        veiwModel.getleft_charge_enable().observe(getActivity(),left_device_charging_enable);
        veiwModel.getright_charge_enable().observe(getActivity(),right_device_charging_enable);






    }
}
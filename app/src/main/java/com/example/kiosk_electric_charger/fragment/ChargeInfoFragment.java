package com.example.kiosk_electric_charger.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.model.MainViewModel;
import com.example.kiosk_electric_charger.model.PageAddrModel;

public class ChargeInfoFragment extends Fragment {

    private String Tag = "ChargeInfoFragment";

    private boolean Type;

    public ChargeInfoFragment(boolean type) {
        this.Type = type;
    }

    ImageButton close_btn,charge_pause_btn;
    
    TextView charge_persent_tv,charge_time_tv,setting_pay_tv,input_pay_tv,charge_amount_tv;

    ConstraintLayout root_container;

    MainViewModel veiwModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tag += " "+ Type;

        Log.d(Tag,"onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(Tag,"onCreateView");

        View view = inflater.inflate(R.layout.fragment_charge_info, container, false);

        root_container = view.findViewById(R.id.charge_info_container);
        
        charge_persent_tv = view.findViewById(R.id.charge_persent_tv);      //충전 퍼센트
        
        charge_time_tv = view.findViewById(R.id.charge_time_tv);        //충전 시간

        setting_pay_tv = view.findViewById(R.id.setting_pay_tv);        //설정 금액
        
        input_pay_tv = view.findViewById(R.id.input_pay_tv);            //충전 금액
        
        charge_amount_tv = view.findViewById(R.id.charge_amount_tv);    // 충전량

        charge_pause_btn = view.findViewById(R.id.charge_pause_btn);    //충전 중지
        charge_pause_btn.setOnClickListener(v -> {
            if(Type){
                veiwModel.getleft_device_charging().setValue(false);
                Intent intent = new Intent("ChargeTimer");
                intent.putExtra("type",true );
                getActivity().sendBroadcast(intent);
            }else{
                veiwModel.getright_device_charging().setValue(false);
                Intent intent = new Intent("ChargeTimer");
                intent.putExtra("type",false );
                getActivity().sendBroadcast(intent);
            }

            Intent intent = new Intent("Page");
            intent.putExtra("page", PageAddrModel.ChargeSelectFgm);
            getActivity().sendBroadcast(intent);
        });

        close_btn = view.findViewById(R.id.close_btn);
        close_btn.setOnClickListener(v -> {
            Intent intent = new Intent("Page");
            if(Type){
                intent.putExtra("page", PageAddrModel.ChargeSelectFgm);
            }else{
                intent.putExtra("page", PageAddrModel.ChargeSelectFgm);
            }

            getActivity().sendBroadcast(intent);
        });



        ConstraintSet set = new ConstraintSet();
        set.clone(root_container);

        // 방향에 따른 페이지 변환
        if(Type){

            root_container.setBackground(getActivity().getDrawable(R.drawable.charge_info_fragment_left_bg));

            set.setHorizontalBias(charge_persent_tv.getId(), 0.352F);

            set.setHorizontalBias(charge_time_tv.getId(), 0.8F);
            set.setVerticalBias(charge_time_tv.getId(),0.545f);

            set.setHorizontalBias(setting_pay_tv.getId(), 0.8F);
            set.setVerticalBias(setting_pay_tv.getId(),0.618f);

            set.setHorizontalBias(input_pay_tv.getId(), 0.8F);
            set.setVerticalBias(input_pay_tv.getId(),0.69f);

            set.setHorizontalBias(charge_amount_tv.getId(), 0.8F);
            set.setVerticalBias(charge_amount_tv.getId(),0.76f);

            set.setHorizontalBias(charge_pause_btn.getId(), 0.9F);

            set.setHorizontalBias(close_btn.getId(), 0.95F);

            set.applyTo(root_container);


        }else{

            root_container.setBackground(getActivity().getDrawable(R.drawable.charge_info_fragment_right_bg));

            set.setHorizontalBias(charge_persent_tv.getId(), 0.18F);

            set.setHorizontalBias(charge_time_tv.getId(), 0.6F);
            set.setVerticalBias(charge_time_tv.getId(),0.535f);

            set.setHorizontalBias(setting_pay_tv.getId(), 0.6F);
            set.setVerticalBias(setting_pay_tv.getId(),0.61f);

            set.setHorizontalBias(input_pay_tv.getId(), 0.6F);
            set.setVerticalBias(input_pay_tv.getId(),0.676f);

            set.setHorizontalBias(charge_amount_tv.getId(), 0.6F);
            set.setVerticalBias(charge_amount_tv.getId(),0.75f);

            set.setHorizontalBias(charge_pause_btn.getId(), 0.65F);

            set.setHorizontalBias(close_btn.getId(), 0.05F);

            set.applyTo(root_container);
        }





        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        veiwModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        //livedata 바뀔시에 이벤트
        final Observer<Integer> charge_per = b -> {
            charge_persent_tv.setText(String.valueOf(b));
        };

        final Observer<String> charge_time = b -> {
            charge_time_tv.setText(b);
        };

        final Observer<Integer> setting_pay = b -> {
            setting_pay_tv.setText(String.valueOf(b));
        };

        final Observer<Integer> input_pay = b -> {
            input_pay_tv.setText(String.valueOf(b));
        };

        final Observer<Float> charge_amount = b -> {
            charge_amount_tv.setText(String.valueOf(b));
        };



        if(Type){
            veiwModel.getleft_charge_persent().observe(getActivity(),charge_per);
            veiwModel.getleft_charge_time().observe(getActivity(),charge_time);
            veiwModel.getleft_payValue().observe(getActivity(),setting_pay);
            veiwModel.getleft_input_pay().observe(getActivity(),input_pay);
            veiwModel.getleft_charge_amount().observe(getActivity(),charge_amount);
        }else{
            veiwModel.getright_charge_persent().observe(getActivity(),charge_per);
            veiwModel.getright_charge_time().observe(getActivity(),charge_time);
            veiwModel.getright_payValue().observe(getActivity(),setting_pay);
            veiwModel.getright_input_pay().observe(getActivity(),input_pay);
            veiwModel.getright_charge_amount().observe(getActivity(),charge_amount);
        }





    }


}
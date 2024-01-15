package com.example.kiosk_electric_charger.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.model.CardStateModel;
import com.example.kiosk_electric_charger.model.MainViewModel;
import com.example.kiosk_electric_charger.model.PageAddrModel;


public class PaySelectFragment extends Fragment {

    private String Tag = "Pay_Select_Fragment";

    private boolean Type;

    public PaySelectFragment(boolean type) {
        // Required empty public constructor
        this.Type = type;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tag += " "+ Type;

        Log.d(Tag,"onCreate");

    }

    ImageButton pay_card_btn,pay_code_btn,pay_rfid_btn,back_btn;

    ConstraintLayout root_container;
    
    MainViewModel veiwModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.d(Tag,"onCreateView");

        View view = inflater.inflate(R.layout.fragment_pay_select, container, false);

        veiwModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        root_container = view.findViewById(R.id.pay_select_container);

        pay_card_btn = view.findViewById(R.id.pay_card);

        pay_card_btn.setOnClickListener(v -> {
            Intent intent = new Intent("Page");

            veiwModel.getCard_read_state().setValue(CardStateModel.NFC_IC_State);          // 결제유형 저장

            if(Type){
                intent.putExtra("page",PageAddrModel.InputPayFgm_l);
            }else{
                intent.putExtra("page",PageAddrModel.InputPayFgm_r);
            }

            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        });


        pay_code_btn = view.findViewById(R.id.pay_code);

        pay_code_btn.setOnClickListener(v -> {
//            Intent intent = new Intent("Page");
//
//            veiwModel.getCard_read_state().setValue(CardStateModel.BarCode_STATE);          // 결제유형 저장
//
//            if(Type){
//                intent.putExtra("page",PageAddrModel.InputPayFgm_l);
//            }else{
//                intent.putExtra("page",PageAddrModel.InputPayFgm_r);
//            }
//
//            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

            unDevelopment();
        });



        pay_rfid_btn = view.findViewById(R.id.pay_rfid);

        pay_rfid_btn.setOnClickListener(v -> {
//            Intent intent = new Intent("Page");
//
//            veiwModel.getCard_read_state().setValue(CardStateModel.RFID_STATE);          // 결제유형 저장
//
//            if(Type){
//                intent.putExtra("page",PageAddrModel.InputPayFgm_l);
//            }else{
//                intent.putExtra("page",PageAddrModel.InputPayFgm_r);
//            }
//
//            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

            unDevelopment();
        });

        back_btn = view.findViewById(R.id.back_btn);

        back_btn.setOnClickListener(v -> {
            Log.d(Tag,"back btn");

            Intent intent = new Intent("Page");
            intent.putExtra("page", PageAddrModel.ChargeSelectFgm);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        });


        ConstraintSet set = new ConstraintSet();
        set.clone(root_container);


        // 방향에 따른 페이지 변환
        if(Type){

            root_container.setBackground(getActivity().getDrawable(R.drawable.pay_select_fragment_left_bg));
            back_btn.setBackground(getActivity().getDrawable(R.drawable.bottomarrow_left));

            set.setHorizontalBias(pay_card_btn.getId(), 0.35F);
            set.setHorizontalBias(pay_code_btn.getId(), 0.65F);
            set.setHorizontalBias(pay_rfid_btn.getId(), 0.95F);
            set.setHorizontalBias(back_btn.getId(), 0.95F);

            set.applyTo(root_container);


        }else{

            root_container.setBackground(getActivity().getDrawable(R.drawable.pay_select_fragment_right_bg));
            back_btn.setBackground(getActivity().getDrawable(R.drawable.bottomarrow_right));

            set.setHorizontalBias(pay_card_btn.getId(), 0.05F);
            set.setHorizontalBias(pay_code_btn.getId(), 0.35F);
            set.setHorizontalBias(pay_rfid_btn.getId(), 0.65F);
            set.setHorizontalBias(back_btn.getId(), 0.05F);

            set.applyTo(root_container);
        }

        return view;
    }



    public void unDevelopment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("미개발").setMessage("개발진행중입니다.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
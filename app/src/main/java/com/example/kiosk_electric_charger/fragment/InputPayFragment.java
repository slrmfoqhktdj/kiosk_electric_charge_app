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

public class InputPayFragment extends Fragment {

    private String Tag = "Input_Pay_Fragment";

    private boolean Type;

    public InputPayFragment(boolean type) {
        this.Type = type;
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Tag += " "+ Type;

        Log.d(Tag,"onCreate");

    }

    TextView pay_tv,amount_tv,value_tv;
    ImageButton back_btn,pay_next_btn;

    ImageButton pay_500_btn,pay_1000_btn,pay_2000_btn,pay_3000_btn,pay_4000_btn,pay_5000_btn,pay_10000_btn,pay_reset_btn;

    ConstraintLayout root_container;

    MainViewModel viewModel;

    int value_pay = 0;        // 결제금액

    float pre_charge_amount = 0;      // 예상 충전량


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(Tag,"onCreateView");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_input_pay, container, false);


        pay_tv = view.findViewById(R.id.pay_tv);    //서비스 단가
        amount_tv = view.findViewById(R.id.amount_tv);      //예상충전량
        value_tv = view.findViewById(R.id.value_tv);        //결제금액







        pay_next_btn = view.findViewById(R.id.pay_next_btn);    //결제 버튼
        pay_next_btn.setOnClickListener(v -> {

            if(pre_charge_amount != 0f && value_pay != 0){          // 값들이 0이 아니면


                Intent intent = new Intent("Page");
                if(Type){
                    viewModel.getleft_pre_charge_amount().setValue(pre_charge_amount);
                    viewModel.getleft_payValue().setValue(value_pay);

                    intent.putExtra("page", PageAddrModel.PayFgm_l);
                }else{
                    viewModel.getright_pre_charge_amount().setValue(pre_charge_amount);
                    viewModel.getright_payValue().setValue(value_pay);

                    intent.putExtra("page", PageAddrModel.PayFgm_r);
                }

                getActivity().sendBroadcast(intent);
            }
            
            
        });

        root_container = view.findViewById(R.id.input_pay_container);

        pay_500_btn = view.findViewById(R.id.pay_500_btn);
        pay_500_btn.setOnClickListener(v -> {
            value_pay += 500;
            setValueTextView();
        });

        pay_1000_btn = view.findViewById(R.id.pay_1000_btn);
        pay_1000_btn.setOnClickListener(v -> {
            value_pay += 1000;
            setValueTextView();
        });

        pay_2000_btn = view.findViewById(R.id.pay_2000_btn);
        pay_2000_btn.setOnClickListener(v -> {
            value_pay += 2000;
            setValueTextView();
        });

        pay_3000_btn = view.findViewById(R.id.pay_3000_btn);
        pay_3000_btn.setOnClickListener(v -> {
            value_pay += 3000;
            setValueTextView();
        });

        pay_4000_btn = view.findViewById(R.id.pay_4000_btn);
        pay_4000_btn.setOnClickListener(v -> {
            value_pay += 4000;
            setValueTextView();
        });

        pay_5000_btn = view.findViewById(R.id.pay_5000_btn);
        pay_5000_btn.setOnClickListener(v -> {
            value_pay += 5000;
            setValueTextView();
        });

        pay_10000_btn = view.findViewById(R.id.pay_10000_btn);
        pay_10000_btn.setOnClickListener(v -> {
            value_pay += 10000;
            setValueTextView();
        });

        pay_reset_btn = view.findViewById(R.id.pay_reset_btn);  // 리셋
        pay_reset_btn.setOnClickListener(v -> {
            value_pay = 0;
            setValueTextView();
        });


        back_btn = view.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(v -> {

            Intent intent = new Intent("Page");
            if(Type){
                intent.putExtra("page", PageAddrModel.PaySelectFgm_l);
            }else{
                intent.putExtra("page", PageAddrModel.PaySelectFgm_r);
            }

            getActivity().sendBroadcast(intent);
        });




        ConstraintSet set = new ConstraintSet();
        set.clone(root_container);

        // 방향에 따른 페이지 변환
        if(Type){

            root_container.setBackground(getActivity().getDrawable(R.drawable.input_pay_fragment_left_bg));
            back_btn.setBackground(getActivity().getDrawable(R.drawable.bottomarrow_left));

            set.setHorizontalBias(pay_tv.getId(), 0.495F);
            set.setHorizontalBias(amount_tv.getId(), 0.495F);
            set.setHorizontalBias(value_tv.getId(), 0.495F);

            set.setHorizontalBias(pay_next_btn.getId(), 0.86F);

            set.setHorizontalBias(pay_500_btn.getId(), 0.35F);
            set.setHorizontalBias(pay_1000_btn.getId(), 0.525F);
            set.setHorizontalBias(pay_2000_btn.getId(), 0.7F);
            set.setHorizontalBias(pay_3000_btn.getId(), 0.875F);
            set.setHorizontalBias(pay_4000_btn.getId(), 0.35F);
            set.setHorizontalBias(pay_5000_btn.getId(), 0.525F);
            set.setHorizontalBias(pay_10000_btn.getId(), 0.7F);
            set.setHorizontalBias(pay_reset_btn.getId(), 0.875F);

            set.setHorizontalBias(back_btn.getId(), 0.95F);

            set.applyTo(root_container);


        }else{

            root_container.setBackground(getActivity().getDrawable(R.drawable.input_pay_fragment_right_bg));
            back_btn.setBackground(getActivity().getDrawable(R.drawable.bottomarrow_right));

            set.setHorizontalBias(pay_tv.getId(), 0.295F);
            set.setHorizontalBias(amount_tv.getId(), 0.295F);
            set.setHorizontalBias(value_tv.getId(), 0.295F);

            set.setHorizontalBias(pay_next_btn.getId(), 0.66F);

            set.setHorizontalBias(pay_500_btn.getId(), 0.15F);
            set.setHorizontalBias(pay_1000_btn.getId(), 0.325F);
            set.setHorizontalBias(pay_2000_btn.getId(), 0.5F);
            set.setHorizontalBias(pay_3000_btn.getId(), 0.675F);
            set.setHorizontalBias(pay_4000_btn.getId(), 0.15F);
            set.setHorizontalBias(pay_5000_btn.getId(), 0.325F);
            set.setHorizontalBias(pay_10000_btn.getId(), 0.5F);
            set.setHorizontalBias(pay_reset_btn.getId(), 0.675F);

            set.setHorizontalBias(back_btn.getId(), 0.05F);

            set.applyTo(root_container);
        }


        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        //        //livedata 바뀔시에 이벤트
        final Observer<Float> payprice = b -> {
            pay_tv.setText(String.valueOf(b));
            setValueTextView();
        };


        //livedata 등록
        viewModel.getPayPrice().observe(getActivity(),payprice);

    }



    private void setValueTextView(){
        value_tv.setText(String.valueOf(value_pay));
        pre_charge_amount = value_pay/Float.parseFloat(String.valueOf( pay_tv.getText()));
        amount_tv.setText(String.valueOf(pre_charge_amount));


    }


    //중지시 초기화
    @Override
    public void onPause(){
        super.onPause();
        value_pay = 0;
        setValueTextView();
    }
}
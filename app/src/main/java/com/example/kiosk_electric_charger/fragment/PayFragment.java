package com.example.kiosk_electric_charger.fragment;

import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.kiosk_electric_charger.R;
import com.example.kiosk_electric_charger.model.CardStateModel;
import com.example.kiosk_electric_charger.model.MainViewModel;
import com.example.kiosk_electric_charger.model.PageAddrModel;

import java.util.concurrent.atomic.AtomicReference;


public class PayFragment extends Fragment {

    private String Tag = "PayFragment";

    private boolean Type;

    public PayFragment(boolean type) {
        this.Type = type;
    }

    ImageButton back_btn;

    ConstraintLayout root_container;

    ImageView card_state_iv;

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

        View view = inflater.inflate(R.layout.fragment_pay, container, false);




        card_state_iv = view.findViewById(R.id.card_state_iv);

        root_container = view.findViewById(R.id.pay_container);


        back_btn = view.findViewById(R.id.back_btn);

        back_btn.setOnClickListener(v -> {
            Intent intent = new Intent("Page");
            if(Type){
                intent.putExtra("page", PageAddrModel.InputPayFgm_l);
            }else{
                intent.putExtra("page", PageAddrModel.InputPayFgm_r);
            }

            getActivity().sendBroadcast(intent);
        });


        ConstraintSet set = new ConstraintSet();
        set.clone(root_container);

        // 방향에 따른 페이지 변환
        if(Type){       // 왼

            root_container.setBackground(getActivity().getDrawable(R.drawable.pay_fragment_left_bg));
            back_btn.setBackground(getActivity().getDrawable(R.drawable.bottomarrow_left));

            set.setHorizontalBias(card_state_iv.getId(), 0.78F);
            set.setHorizontalBias(back_btn.getId(), 0.95F);

            set.applyTo(root_container);


        }else{      // 오

            root_container.setBackground(getActivity().getDrawable(R.drawable.pay_fragment_right_bg));
            back_btn.setBackground(getActivity().getDrawable(R.drawable.bottomarrow_right));

            set.setHorizontalBias(card_state_iv.getId(), 0.21F);
            set.setHorizontalBias(back_btn.getId(), 0.05F);

            set.applyTo(root_container);
        }



        return view;
    }


    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        veiwModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);




        //        //livedata 바뀔시에 이벤트
        final Observer<Integer> cardstate = b -> {
            switch (b){
                case CardStateModel.NFC_IC_State:       // nfc,ic 카드 결제
                    card_state_iv.setBackgroundResource(R.drawable.card_ic_animation);
                    break;

                case CardStateModel.BarCode_STATE:      // 바코드 결제
                    card_state_iv.setBackground(getActivity().getDrawable(R.drawable.card_qr_animation));
                    break;

                case CardStateModel.RFID_STATE:         // rfid 결제
                    card_state_iv.setBackground(getActivity().getDrawable(R.drawable.card_rfid_animation));
                    break;

                case CardStateModel.Paying:         //결제중
                    card_state_iv.setBackground(null);
                    break;

                case CardStateModel.Pay_Complete:       // 결제완료
                    card_state_iv.setBackground(getActivity().getDrawable(R.drawable.pay_fragment_pay_complete));
                    break;

                case CardStateModel.Pay_Fail:       //결제실패, 다시시도요청
                    card_state_iv.setBackground(getActivity().getDrawable(R.drawable.pay_fragment_pay_fail));
                    break;

                case CardStateModel.Card_Out:       // 카드 제거 요청
                    card_state_iv.setBackground(getActivity().getDrawable(R.drawable.card_outcard_animation));
                    break;

                default:                        // 0일때 오류.
                    card_state_iv.setBackground(null);
                    break;
            }


        };

        //livedata 등록
        veiwModel.getCard_read_state().observe(getActivity(),cardstate);

        super.onViewCreated(view,savedInstanceState);
    }
}
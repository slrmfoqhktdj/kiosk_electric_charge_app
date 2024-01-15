package com.example.kiosk_electric_charger.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kiosk_electric_charger.SharedPreferences.PrefsHelper;
import com.example.kiosk_electric_charger.SharedPreferences.model.KeyModel;

public class MainViewModel extends ViewModel {

    private MutableLiveData<Float> payPrice;  // 가격,단가
    private MutableLiveData<Boolean> pageDirection; // 페이지 방향
    private MutableLiveData<Integer> card_read_state;  // 카드 읽는 상태, 방식
    
    private  MutableLiveData<Float> left_pre_charge_amount;   // 왼쪽 예상 충전량
    private  MutableLiveData<Integer> left_payValue;        // 왼쪽 결제 금액

    private  MutableLiveData<Float> right_pre_charge_amount;   // 오른쪽 예상 충전량
    private  MutableLiveData<Integer> right_payvalue;        // 오른쪽 결제 금액
    
    private MutableLiveData<Boolean> left_device_charging;  //왼쪽 충전기 충전 진행 여부
    private MutableLiveData<Boolean> right_device_charging;  //오른쪽 충전기 충전 진행 여부

    private MutableLiveData<String> left_pay_number;  //왼쪽 충전기 충전 승인 번호
    private MutableLiveData<String> right_pay_number;  //왼쪽 충전기 충전 승인 번호

    private MutableLiveData<Integer> left_charge_persent;   //왼쪽 충전 퍼센트
    private MutableLiveData<String> left_charge_time;      //왼쪽 충전 시간
    private MutableLiveData<Integer> left_input_pay;         //왼쪽 실 결제 비용
    private MutableLiveData<Float> left_charge_amount;    //왼쪽 충전량

    private MutableLiveData<Integer> right_charge_persent;   //오른쪽 충전 퍼센트
    private MutableLiveData<String> right_charge_time;      //오른쪽 충전 시간
    private MutableLiveData<Integer> right_input_pay;         //오른쪽 실 결제 비용
    private MutableLiveData<Float> right_charge_amount;    //오른쪽 충전량

    private MutableLiveData<Boolean> left_charge_enable;    //왼쪽 사용가능

    private MutableLiveData<Boolean> right_charge_enable;    //오른쪽 사용가능

    //data binding시 필요
    public MutableLiveData<Float> getPayPrice()
    {
        if(payPrice == null)
            payPrice = new MutableLiveData<>(0f);
        return payPrice;
    }

    public MutableLiveData<Boolean> getpageDirection()
    {
        if(pageDirection == null)
            pageDirection = new MutableLiveData<>(false);
        return pageDirection;
    }


    public MutableLiveData<Integer> getCard_read_state()
    {
        if(card_read_state == null)
            card_read_state = new MutableLiveData<>(0);
        return card_read_state;
    }

    public MutableLiveData<Float> getleft_pre_charge_amount()
    {
        if(left_pre_charge_amount == null)
            left_pre_charge_amount = new MutableLiveData<>(0f);
        return left_pre_charge_amount;
    }

    public MutableLiveData<Integer> getleft_payValue()
    {
        if(left_payValue == null)
            left_payValue = new MutableLiveData<>(0);
        return left_payValue;
    }

    public MutableLiveData<Float> getright_pre_charge_amount()
    {
        if(right_pre_charge_amount == null)
            right_pre_charge_amount = new MutableLiveData<>(0f);
        return right_pre_charge_amount;
    }

    public MutableLiveData<Integer> getright_payValue()
    {
        if(right_payvalue == null)
            right_payvalue = new MutableLiveData<>(0);
        return right_payvalue;
    }


    public MutableLiveData<Boolean> getleft_device_charging()
    {
        if(left_device_charging == null)
            left_device_charging = new MutableLiveData<>(false);
        return left_device_charging;
    }


    public MutableLiveData<Boolean> getright_device_charging()
    {
        if(right_device_charging == null)
            right_device_charging = new MutableLiveData<>(false);
        return right_device_charging;
    }

    public MutableLiveData<String> getLeft_pay_number()
    {
        if(left_pay_number == null)
            left_pay_number = new MutableLiveData<>("00");
        return left_pay_number;
    }

    public MutableLiveData<String> getRight_pay_number()
    {
        if(right_pay_number == null)
            right_pay_number = new MutableLiveData<>("00");
        return right_pay_number;
    }

    public MutableLiveData<Integer> getleft_charge_persent()
    {
        if(left_charge_persent == null)
            left_charge_persent = new MutableLiveData<>(0);
        return left_charge_persent;
    }

    public MutableLiveData<String> getleft_charge_time()
    {
        if(left_charge_time == null)
            left_charge_time = new MutableLiveData<>("00:00:00");
        return left_charge_time;
    }


    public MutableLiveData<Integer> getleft_input_pay()
    {
        if(left_input_pay == null)
            left_input_pay = new MutableLiveData<>(0);
        return left_input_pay;
    }

    public MutableLiveData<Float> getleft_charge_amount()
    {
        if(left_charge_amount == null)
            left_charge_amount = new MutableLiveData<>(0f);
        return left_charge_amount;
    }

    public MutableLiveData<Integer> getright_charge_persent()
    {
        if(right_charge_persent == null)
            right_charge_persent = new MutableLiveData<>(0);
        return right_charge_persent;
    }

    public MutableLiveData<String> getright_charge_time()
    {
        if(right_charge_time == null)
            right_charge_time = new MutableLiveData<>("00:00:00");
        return right_charge_time;
    }

    public MutableLiveData<Integer> getright_input_pay()
    {
        if(right_input_pay == null)
            right_input_pay = new MutableLiveData<>(0);
        return right_input_pay;
    }

    public MutableLiveData<Float> getright_charge_amount()
    {
        if(right_charge_amount == null)
            right_charge_amount = new MutableLiveData<>(0f);
        return right_charge_amount;
    }


    public MutableLiveData<Boolean> getleft_charge_enable()
    {
        if(left_charge_enable == null)
            left_charge_enable = new MutableLiveData<>(PrefsHelper.read(KeyModel.getLeftChargingEnabled(),true));
        return left_charge_enable;
    }

    public MutableLiveData<Boolean> getright_charge_enable()
    {
        if(right_charge_enable == null)
            right_charge_enable = new MutableLiveData<>(PrefsHelper.read(KeyModel.getRightChargingEnabled(),true));
        return right_charge_enable;
    }

}

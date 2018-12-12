package com.example.fingerprint.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.fingerprint.MainActivity;
import com.example.fingerprint.R;
import com.example.fingerprint.tools.StringUtils;
import com.example.fingerprint.tools.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class FingerprintSetFragment extends Fragment {
    private static final String TAG = "FingerprintSetFragment";

    private MainActivity mContext;

    @ViewInject(R.id.btnSetThreshold)
    Button btnSetThreshold;
    @ViewInject(R.id.btnGetThreshold)
    Button btnGetThreshold;
    @ViewInject(R.id.btnSetPacketSize)
    Button btnSetPacketSize;
    @ViewInject(R.id.btnGetPacketSize)
    Button btnGetPacketSize;
    @ViewInject(R.id.btnGetBaud)
    Button btnGetBaud;
    @ViewInject(R.id.btnSetBaud)
    Button btnSetBaud;
    @ViewInject(R.id.btnReInit)
    Button btnReInit;


    @ViewInject(R.id.spThreshold)
    Spinner spThreshold;
    @ViewInject(R.id.spPacketSize)
    Spinner spPacketSize;
    @ViewInject(R.id.spBaud)
    Spinner spBaud;

    @ViewInject(R.id.etPSW)
    EditText etPSW;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fingerprint_set,
                container, false);
        ViewUtils.inject(this, v);


        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (MainActivity) getActivity();


    }


    @OnClick(R.id.btnSetThreshold)
    public void btnSetThreshold_onClick(View v) {
        if (mContext.mFingerprint.setReg(5, spThreshold.getSelectedItemPosition() + 1)) {

            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_set_threshold_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_set_threshold_fail);
        }
    }

    @OnClick(R.id.btnSetPacketSize)
    public void btnSetPacketSize_onClick(View v) {

        if (mContext.mFingerprint.setReg(6, spPacketSize.getSelectedItemPosition())) {

            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_set_PacketSize_succ);
        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_set_PacketSize_fail);
        }


    }


    @OnClick(R.id.btnSetBaud)
    public void btnSetBaud_onClick(View v) {

        int baudrate = StringUtils.toInt(spBaud.getSelectedItem().toString(), -1);


        if (mContext.mFingerprint.setReg(4, baudrate / 9600)) {

            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_set_Baudrate_succ);

            mContext.freeFingerprint();
            mContext.initFingerprint(baudrate);


        } else {
            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_set_Baudrate_fail);
        }


    }


    @OnClick(R.id.btnReInit)
    public void btnReInit_onClick(View v) {

        int baudrate = StringUtils.toInt(spBaud.getSelectedItem().toString(), -1);


        mContext.freeFingerprint();
        mContext.initFingerprint(baudrate);


    }


    @OnClick(R.id.btnGetThreshold)
    public void btnGetThreshold_onClick(View v) {


    }


    @OnClick(R.id.btnGetPacketSize)
    public void btnGetPacketSize_onClick(View v) {


    }


    @OnClick(R.id.btnSetPSW)
    public void btnSetPSW_onClick(View v) {

        String strPWD = etPSW.getText().toString().trim();
        if (!StringUtils.isNotEmpty(strPWD)) {
            UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nopwd);

            return;

        }

        if (!mContext.vailHexInput(strPWD)) {
            UIHelper.ToastMessage(mContext,
                    R.string.rfid_mgs_error_nohex);

            return;
        }


//        if (mContext.mFingerprint.setPSW(strPWD)) {
//
//            UIHelper.ToastMessage(mContext,
//                    R.string.fingerprint_msg_set_PSW_succ);
//        } else {
//            UIHelper.ToastMessage(mContext,
//                    R.string.fingerprint_msg_set_PSW_fail);
//        }


    }


    @OnClick(R.id.btnVfyPSW)
    public void btnVfyPSW_onClick(View v) {

        String strPWD = etPSW.getText().toString().trim();
        if (!StringUtils.isNotEmpty(strPWD)) {
            UIHelper.ToastMessage(mContext, R.string.rfid_mgs_error_nopwd);

            return;

        }


        if (strPWD.length() != 8) {
            UIHelper.ToastMessage(mContext,
                    R.string.uhf_msg_addr_must_len8);
            return;
        }


        if (!mContext.vailHexInput(strPWD)) {
            UIHelper.ToastMessage(mContext,
                    R.string.rfid_mgs_error_nohex);

            return;
        }

//        if (mContext.mFingerprint.vfyPSW(strPWD)) {
//
//            UIHelper.ToastMessage(mContext,
//                    R.string.fingerprint_msg_verify_PSW_succ);
//        } else {
//            UIHelper.ToastMessage(mContext,
//                    R.string.fingerprint_msg_verify_PSW_fail);
//        }


    }


}

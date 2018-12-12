package com.example.fingerprint.fragment;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.bean.Fingerprint;
import com.example.bean.FingerprintSDDao;
import com.example.fingerprint.MainActivity;
import com.example.fingerprint.R;
import com.example.fingerprint.tools.StringUtils;
import com.example.fingerprint.tools.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rscja.deviceapi.Fingerprint.BufferEnum;

public class AcquisitionFragment extends Fragment {

    private static final String TAG = "AcquisitionFragment";

    private MainActivity mContext;

    @ViewInject(R.id.etPageId)
    EditText etPageId;
    @ViewInject(R.id.etName)
    EditText etName;
    @ViewInject(R.id.btnSave)
    Button btnSave;
    @ViewInject(R.id.cbShowImg)
    CheckBox cbShowImg;
    @ViewInject(R.id.cbShowImgIso)
    CheckBox cbShowImgIso;
    @ViewInject(R.id.ivFinger)
    ImageView ivFinger;
    @ViewInject(R.id.cbContinuous)
    CheckBox cbContinuous;
    @ViewInject(R.id.et_between)
    EditText et_between;
    @ViewInject(R.id.ll_stat)
    LinearLayout ll_stat;
    @ViewInject(R.id.tv_scan_count)
    TextView tv_scan_count;
    @ViewInject(R.id.tv_succ_count)
    TextView tv_succ_count;
    @ViewInject(R.id.tv_fail_count)
    TextView tv_fail_count;
    @ViewInject(R.id.rbDef)
    RadioButton rbDef;
    @ViewInject(R.id.rbIso)
    RadioButton rbIso;

    int sussCount = 0;
    int failCount = 0;
    int total = 0;

    private FingerprintSDDao dao;

    private AutoThread autoThread = null;
    private ProgressDialog mDialog;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.arg1) {
                case 0:
                    mDialog = new ProgressDialog(mContext);
                    mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

                    ivFinger.setImageBitmap(null);
                    break;

                case 1:
                    UIHelper.ToastMessage(mContext,
                            R.string.fingerprint_msg_acq_fail);
//                    mContext.playSound(2);

                    failCount += 1;

                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }

                    break;

                case 2:

                    UIHelper.ToastMessage(mContext,
                            R.string.fingerprint_title_get_img_fail);
                    mContext.playSound(2);

                    failCount += 1;

                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }

                    break;
                case 3:

                    Bitmap bitmap = BitmapFactory.decodeFile(mContext.getFilesDir()
                            + "/finger.bmp");
                    ivFinger.setImageBitmap(bitmap);
                    mContext.playSound(1);

                    sussCount += 1;

                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }

                    break;

                case 4:
                    UIHelper.ToastMessage(mContext,
                            R.string.fingerprint_msg_acq_succ);
                    mContext.playSound(1);

                    sussCount += 1;

                    if (mDialog != null) {
                        mDialog.cancel();
                        mDialog = null;
                    }

                    break;

                default:
                    break;
            }

            if (autoThread != null) {
                etPageId.setText(total + "");
                etName.setText("name" + total);
            } else {
                etPageId.setText("");
                etName.setText("");
            }
            stat();

        }

    };

    private void stat() {
        total = sussCount + failCount;

        tv_fail_count.setText(failCount + "");
        tv_succ_count.setText(sussCount + "");
        tv_scan_count.setText(total + "");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_acquisition_fragment,
                container, false);
        ViewUtils.inject(this, v);


        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = (MainActivity) getActivity();

        init();

    }

    @Override
    public void onStart() {
        super.onStart();
        cbContinuous.setChecked(false);
    }

    @Override
    public void onPause() {

        super.onPause();


        cancelAutoThread();

    }

    private void init() {

        dao = new FingerprintSDDao(mContext);

    }

    @OnClick(R.id.cbShowImg)
    public void cbShowImg_onClick(View v) {
        if (cbShowImg.isChecked()) {
            cbShowImgIso.setChecked(false);
        }
    }

    @OnClick(R.id.cbShowImgIso)
    public void cbShowImgIso_onClick(View v) {
        if (cbShowImgIso.isChecked()) {
            cbShowImg.setChecked(false);
        }
    }

    @OnClick(R.id.btnSave)
    public void btnSave_onClick(View v) {
        Log.i(TAG, "btnSave_onClick()");

        String pageId = etPageId.getText().toString().trim();
        String name = etName.getText().toString().trim();

        if (cbContinuous.isChecked()) {
            pageId = "0";
            name = "name0";
            ll_stat.setVisibility(View.VISIBLE);
        } else {
            ll_stat.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(pageId)) {
            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_page_id_not_null);
            return;
        }

        if (TextUtils.isEmpty(name)) {
            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_name_not_null);
            return;
        }

        if (!TextUtils.isDigitsOnly(pageId)) {
            UIHelper.ToastMessage(mContext,
                    R.string.fingerprint_msg_page_id_need_digits);
            return;
        }

        int iPageId = Integer.parseInt(pageId);

        // if (iPageId < 0 || iPageId > 254) {
        // UIHelper.ToastMessage(mContext,
        // R.string.fingerprint_msg_page_id_need_0_to_254);
        // return;
        // }

        if (cbContinuous.isChecked()) {

            if (btnSave.getText().equals(
                    getString(R.string.fingerprint_btn_save))) {

                sussCount = 0;
                failCount = 0;
                total = 0;

                cbContinuous.setEnabled(false);

                btnSave.setText(R.string.fingerprint_btn_save_stop);

                int sleep = StringUtils.toInt(et_between.getText().toString(),
                        0);

                autoThread = new AutoThread(0, name, cbShowImg.isChecked(),
                        cbShowImgIso.isChecked(), sleep);

                autoThread.start();
            } else {
                cancelAutoThread();
            }

        } else {
            new AcqTask(iPageId, name, cbShowImg.isChecked(),
                    cbShowImgIso.isChecked()).execute(iPageId,
                    cbShowImg.isChecked() ? 1 : 0);

        }
    }


    private void cancelAutoThread() {
        cbContinuous.setEnabled(true);
        btnSave.setText(R.string.fingerprint_btn_save);

        etPageId.setText("");
        etName.setText("");

        if (autoThread != null) {
            autoThread.interrupt();
            autoThread = null;
        }

        if (mDialog != null) {
            mDialog.cancel();
            mDialog = null;
        }
    }

    @OnClick(R.id.cbContinuous)
    public void cbContinuous_onClick(View v) {
        if (cbContinuous.isChecked()) {
            ll_stat.setVisibility(View.VISIBLE);

        } else {
            ll_stat.setVisibility(View.GONE);
        }
    }

    class AutoThread extends Thread {

        private int mSleep = 0;
        private int mPageId = 0;
        private String mName;
        private boolean mShowImg = false;
        private boolean isShowImgISO = false;

        public AutoThread(int pageId, String name, boolean showImg,
                          boolean showImgISO, int sleep) {
            mSleep = sleep * 1000;
            mPageId = pageId;
            mName = name;
            mShowImg = showImg;
            isShowImgISO = showImgISO;
        }

        @Override
        public void run() {
            super.run();

            Message msg = null;
            boolean exeSucc = false;

            while (!Thread.interrupted()) {

                mName = "name" + mPageId;

                exeSucc = false;

                try {
                    sleep(mSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    Thread.currentThread().interrupt();
                }

                msg = mHandler.obtainMessage();
                msg.arg1 = 0;
                mHandler.sendMessage(msg);

                // 采集指纹
                if (!mContext.mFingerprint.getImage()) {
                    msg = mHandler.obtainMessage();
                    msg.arg1 = 1;
                    mHandler.sendMessage(msg);
                    continue;
                }

                // // 判断是否定制模块
                // if (isShowImgISO) {
                // // 生成特征值到B11
                // if (mContext.mFingerprint.genChar(BufferEnum.B11)) {
                // exeSucc = true;
                // }
                // } else {
                // 生成特征值到B1
                if (mContext.mFingerprint.genChar(BufferEnum.B1)) {
                    exeSucc = true;
                } else {
                    msg = mHandler.obtainMessage();
                    msg.arg1 = 1;
                    mHandler.sendMessage(msg);
                    continue;
                }
                // }

                // 再次采集指纹
                if (!mContext.mFingerprint.getImage()) {
                    msg = mHandler.obtainMessage();
                    msg.arg1 = 1;
                    mHandler.sendMessage(msg);
                    continue;
                }

                // // 判断是否定制模块
                // if (isShowImgISO) {
                // // 生成特征值到B12
                // if (mContext.mFingerprint.genChar(BufferEnum.B12)) {
                // exeSucc = true;
                // }
                // } else {
                // 生成特征值到B2
                if (mContext.mFingerprint.genChar(BufferEnum.B2)) {
                    exeSucc = true;
                } else {
                    msg = mHandler.obtainMessage();
                    msg.arg1 = 1;
                    mHandler.sendMessage(msg);
                    continue;
                }
                // }

                // 合并两个缓冲区到B1
                if (mContext.mFingerprint.regModel()) {
                    exeSucc = true;
                } else {
                    msg = mHandler.obtainMessage();
                    msg.arg1 = 1;
                    mHandler.sendMessage(msg);
                    continue;
                }

                if (exeSucc) {
                    if (mContext.mFingerprint.storChar(BufferEnum.B1, mPageId)) {

                        mPageId++;// 递增索引

                        // 判断是否定制模块
                        if (rbIso.isChecked()) {
                            mContext.mFingerprint.upChar(BufferEnum.B11);
                        } else {
                            mContext.mFingerprint.upChar(BufferEnum.B1);
                        }

                        if (mShowImg || isShowImgISO) {
                            // 显示指纹图片
                            if (mContext.mFingerprint.getImage()) {

                                // // 判断是否定制模块
                                // if (isShowImgISO) {
                                // if (mContext.mFingerprint.upImageISO(1,
                                // mContext.getFilesDir()
                                // + "/finger.bmp") != -1) {
                                // msg = mHandler.obtainMessage();
                                // msg.arg1 = 3;
                                // mHandler.sendMessage(msg);
                                // continue;
                                //
                                // } else {
                                // msg = mHandler.obtainMessage();
                                // msg.arg1 = 2;
                                // mHandler.sendMessage(msg);
                                // continue;
                                // }
                                // } else {
                                if (mContext.mFingerprint.upImage(1,
                                        mContext.getFilesDir() + "/finger.bmp") != -1) {
                                    msg = mHandler.obtainMessage();
                                    msg.arg1 = 3;
                                    mHandler.sendMessage(msg);
                                    continue;

                                } else {
                                    msg = mHandler.obtainMessage();
                                    msg.arg1 = 2;
                                    mHandler.sendMessage(msg);
                                    continue;
                                }
                                // }

                            } else {
                                msg = mHandler.obtainMessage();
                                msg.arg1 = 2;
                                mHandler.sendMessage(msg);
                                continue;
                            }
                        }
                        msg = mHandler.obtainMessage();
                        msg.arg1 = 4;
                        mHandler.sendMessage(msg);
                        continue;
                    } else {

                        msg = mHandler.obtainMessage();
                        msg.arg1 = 1;
                        mHandler.sendMessage(msg);
                        continue;
                    }
                } else {
                    msg = mHandler.obtainMessage();
                    msg.arg1 = 1;
                    mHandler.sendMessage(msg);
                    continue;
                }

            }
        }

    }

    class AcqTask extends AsyncTask<Integer, Integer, String> {
        ProgressDialog mypDialog;

        int pid;
        String uname;
        boolean isShowImg;
        boolean isShowImgISO;
        String data;
        boolean isExec = false;

        public AcqTask(int pageId, String name, boolean showImg,
                       boolean showImgISO) {
            pid = pageId;
            uname = name;
            isShowImg = showImg;
            isShowImgISO = showImgISO;
        }

        @Override
        protected String doInBackground(Integer... params) {

            boolean exeSucc = false;

            // 采集指纹
            if (!mContext.mFingerprint.getImage()) {
                return null;
            }

            // // 判断是否定制模块
            // if (isShowImgISO) {
            //
            // // 生成特征值到B1
            // if (mContext.mFingerprint.genChar(BufferEnum.B11)) {
            // exeSucc = true;
            // }
            //
            // } else {
            // 生成特征值到B1
            if (mContext.mFingerprint.genChar(BufferEnum.B1)) {
                exeSucc = true;
            }
            // }

            // 再次采集指纹
            if (!mContext.mFingerprint.getImage()) {
                return null;
            }

            // // 判断是否定制模块
            // if (isShowImgISO) {
            // // 生成特征值到B12
            // if (mContext.mFingerprint.genChar(BufferEnum.B12)) {
            // exeSucc = true;
            // }
            // } else {
            // 生成特征值到B2
            if (mContext.mFingerprint.genChar(BufferEnum.B2)) {
                exeSucc = true;
            }
            // }

            // 合并两个缓冲区到B1
            if (mContext.mFingerprint.regModel()) {
                exeSucc = true;
            }

            if (exeSucc) {
                if (mContext.mFingerprint.storChar(BufferEnum.B1, pid)) {

                    // 判断是否定制模块
                    if (rbIso.isChecked()) {
                        data = mContext.mFingerprint.upChar(BufferEnum.B11);
                    } else {
                        data = mContext.mFingerprint.upChar(BufferEnum.B1);
                    }

                    if (isShowImg || isShowImgISO) {
                        // 显示指纹图片
                        if (mContext.mFingerprint.getImage()) {

                            // // 判断是否定制模块
                            // if (isShowImgISO) {
                            // if (mContext.mFingerprint.upImageISO(1,
                            // mContext.getFilesDir() + "/finger.bmp") != -1) {
                            // return "img-ok";
                            //
                            // } else {
                            // return "img-fail";
                            // }
                            // } else {
                            if (mContext.mFingerprint.upImage(1,
                                    mContext.getFilesDir() + "/finger.bmp") != -1) {
                                return "img-ok";

                            } else {
                                return "img-fail";
                            }
                            // }

                        } else {
                            return "img-fail";
                        }
                    }
                    return "ok";
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (TextUtils.isEmpty(result)) {
                UIHelper.ToastMessage(mContext,
                        R.string.fingerprint_msg_acq_fail);
                mContext.playSound(2);
                return;
            } else if (result.equals("img-fail")) {
                UIHelper.ToastMessage(mContext,
                        R.string.fingerprint_title_get_img_fail);
                mContext.playSound(2);
                return;

            } else if (result.equals("img-ok")) {
                Bitmap bitmap = BitmapFactory.decodeFile(mContext.getFilesDir()
                        + "/finger.bmp");
                ivFinger.setImageBitmap(bitmap);
            }

            dao.startReadableDatabase(false);

            Fingerprint f = dao.query(pid);

            if (f == null) {
                f = new Fingerprint();
                f.set_id(0);
                f.setPageId(pid);
                f.setName(uname);
                f.setFeatures(data);
                f.setCreateTime(StringUtils.getTimeFormat(System
                        .currentTimeMillis()));
                dao.insert(f);

            } else {

                Log.i("AcquisitionFragment", "f.get_id=" + f.get_id());

                f.setPageId(pid);
                f.setName(uname);
                f.setFeatures(data);
                f.setCreateTime(StringUtils.getTimeFormat(System
                        .currentTimeMillis()));
                dao.update(f);

            }
            dao.closeDatabase(false);

            UIHelper.ToastMessage(mContext, R.string.fingerprint_msg_acq_succ);
            mContext.playSound(1);

            isExec = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mypDialog = new ProgressDialog(mContext);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();

            ivFinger.setImageBitmap(null);

            isExec = true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        public boolean getExecStatus() {
            return isExec;
        }

    }

    ;
}

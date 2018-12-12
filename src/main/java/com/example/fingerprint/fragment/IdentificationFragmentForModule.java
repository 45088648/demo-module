package com.example.fingerprint.fragment;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.example.bean.Fingerprint;
import com.example.bean.FingerprintSDDao;
import com.example.module.MainActivity;
import com.example.fingerprint.R;
import com.example.fingerprint.tools.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rscja.deviceapi.Fingerprint.BufferEnum;

public class IdentificationFragmentForModule extends Fragment {
    private MainActivity mContext;

    private static final String TAG = "IdentificationFragment";

    @ViewInject(R.id.etPageId)
    EditText etPageId;
    @ViewInject(R.id.etName)
    EditText etName;
    @ViewInject(R.id.btnIdent)
    Button btnIdent;
    @ViewInject(R.id.cbShowImg)
    CheckBox cbShowImg;
    @ViewInject(R.id.cbShowImgIso)
    CheckBox cbShowImgIso;
    @ViewInject(R.id.ivFinger)
    ImageView ivFinger;
    @ViewInject(R.id.etScore)
    EditText etScore;

    private FingerprintSDDao dao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView()");

        View v = inflater.inflate(R.layout.fingerprint_identification_fragment,
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

    @OnClick(R.id.btnIdent)
    public void btnIdent_onClick(View v) {
        new IdentTask(cbShowImg.isChecked(), cbShowImgIso.isChecked())
                .execute();
    }

    class IdentTask extends AsyncTask<Integer, Integer, String> {
        ProgressDialog mypDialog;

        int searchPageID = -1;
        int searchScore = -1;
        String searchName = "";
        boolean isShowImg;
        boolean isShowImgISO;

        public IdentTask(boolean showImg, boolean showImgISO) {
            isShowImg = showImg;
            isShowImgISO = showImgISO;
        }

        @Override
        protected String doInBackground(Integer... params) {
          return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (TextUtils.isEmpty(result)) {

                UIHelper.ToastMessage(mContext,
                        R.string.fingerprint_msg_ident_fail);
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

            etPageId.setText(searchPageID + "");
            etScore.setText(searchScore + "");

            dao.startReadableDatabase(false);
            Fingerprint f = dao.query(searchPageID);
            dao.closeDatabase(false);

            if (f != null) {
                etName.setText(f.getName());
            }

            UIHelper.ToastMessage(mContext, R.string.fingerprint_msg_ident_succ);
            mContext.playSound(1);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mypDialog = new ProgressDialog(mContext);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();

            etName.setText(null);
            etPageId.setText(null);
            etScore.setText(null);

            ivFinger.setImageBitmap(null);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

    }

    ;

}

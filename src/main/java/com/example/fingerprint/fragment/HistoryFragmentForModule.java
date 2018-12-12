package com.example.fingerprint.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.adapter.FingerprintAdapter;
import com.example.bean.Fingerprint;
import com.example.bean.FingerprintSDDao;
import com.example.module.MainActivity;
import com.example.fingerprint.R;
import com.example.fingerprint.tools.UIHelper;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.rscja.deviceapi.Fingerprint.BufferEnum;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragmentForModule extends Fragment {

    private static final String TAG = "HistoryFragment";

    private MainActivity mContext;

    private FingerprintSDDao dao;
    // 列表数据
    private List<Fingerprint> fList = new ArrayList<Fingerprint>();

    // 每一页显示的行数
    public int pageSize = 10;
    // 当前页数
    public int pageNum = 1;

    private FingerprintAdapter adapter;

    @ViewInject(R.id.lvData)
    ListView lvData;
    @ViewInject(R.id.btnImport)
    Button btnImport;
    @ViewInject(R.id.btnClear)
    Button btnClear;
    @ViewInject(R.id.tvLocalNum)
    TextView tvLocalNum;
    @ViewInject(R.id.tvModelNum)
    TextView tvModelNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fingerprint_history_fragment,
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
    public void onResume() {
        super.onResume();

        Log.i(TAG, "HistoryFragment onResume()");

        initData();

    }

    @OnClick(R.id.btnImport)
    public void btnImport_onClick(View v) {
        new ImportTask().execute();
    }

    @OnClick(R.id.btnClear)
    public void btnClear_onClick(View v) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.rfid_msg_confirm_title)
                .setMessage(R.string.fingerprint_msg_sure_clear)
                .setPositiveButton(R.string.rfid_msg_confirm_true,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                new EmptyTask().execute();

                            }
                        })
                .setNegativeButton(R.string.rfid_msg_confirm_flase,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                            }
                        }).show();
    }

    private void init() {

        registerForContextMenu(lvData);

    }

    private void initData() {

        dao = new FingerprintSDDao(mContext);
        // (1)获取数据库
        dao.startReadableDatabase(false);
        // (2)执行查询
        fList = dao.queryList(null, null, null, null, null,
                "create_time desc limit " + String.valueOf(pageSize)
                        + " offset " + 0, null);
        // (3)关闭数据库
        dao.closeDatabase(false);

        adapter = new FingerprintAdapter(mContext, fList);
        lvData.setAdapter(adapter);

        tvLocalNum.setText(fList.size() + "");

        new ModelCountTask().execute();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = mContext.getMenuInflater();
        inflater.inflate(R.menu.fingerprint, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_finger_del) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                    .getMenuInfo();
            delFingerprint(Integer.parseInt(info.id + ""));
            return true;
        } else if (item.getItemId() == R.id.action_finger_delall) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.fingerprint_dailog_title);
            builder.setMessage(R.string.fingerprint_dailog_msg);
            builder.setPositiveButton(R.string.fingerprint_dailog_ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            delAllFingerprint();

                        }
                    });

            builder.setNegativeButton(R.string.fingerprint_dailog_no,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();

                        }
                    });
            builder.show();

            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void delFingerprint(int index) {
        Fingerprint f = fList.get(index);

        if (fList.remove(f)) {
            Log.i(TAG, "onContextItemSelected PageId:" + f.getPageId());
            dao.startWritableDatabase(false);
            dao.delete(" pageId=? ", new String[]{f.getPageId() + ""});
            dao.closeDatabase(false);
            adapter.notifyDataSetChanged();

            tvLocalNum.setText(fList.size() + "");
        }
    }

    private void delAllFingerprint() {

        dao.startWritableDatabase(false);
        dao.deleteAll();
        dao.closeDatabase(false);
        fList.clear();
        adapter.notifyDataSetChanged();

        tvLocalNum.setText("0");
    }

    class ModelCountTask extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {

            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result < 0) {
                result = 0;
            }
            tvModelNum.setText(result + "");
        }

    }

    class EmptyTask extends AsyncTask<Integer, Integer, Integer> {
        ProgressDialog mypDialog;

        @Override
        protected Integer doInBackground(Integer... params) {


            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            mypDialog.cancel();
            if (result == 0) {
                new ModelCountTask().execute();
//                mContext.playSound(1);
                UIHelper.ToastMessage(mContext,
                        R.string.fingerprint_msg_clear_succ);
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.fingerprint_msg_clear_fail);
//                mContext.playSound(2);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mypDialog = new ProgressDialog(mContext);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();

        }

    }

    class ImportTask extends AsyncTask<Integer, Integer, String> {
        ProgressDialog mypDialog;

        int iSucc = 0;
        int iFail = 0;

        @Override
        protected String doInBackground(Integer... params) {

            if (fList == null || fList.size() < 1) {
                return null;

            }

            for (Fingerprint item : fList) {


            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (iSucc > 0) {
                new ModelCountTask().execute();
            }
            if (iFail == 0) {

//                mContext.playSound(1);
                UIHelper.ToastMessage(mContext,
                        R.string.fingerprint_msg_import_succ);
            } else {
                UIHelper.ToastMessage(mContext, String.format(mContext
                                .getString(R.string.fingerprint_msg_import_fail),
                        iSucc, iFail));
//                mContext.playSound(2);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mypDialog = new ProgressDialog(mContext);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

    }

    ;
}

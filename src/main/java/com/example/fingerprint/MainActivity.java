package com.example.fingerprint;


import com.example.fingerprint.fragment.AcquisitionFragment;
import com.example.fingerprint.fragment.FingerprintSetFragment;
import com.example.fingerprint.fragment.HistoryFragment;
import com.example.fingerprint.fragment.IdentificationFragment;
import com.example.fingerprint.tools.UIHelper;
import com.rscja.deviceapi.Fingerprint;
//import com.rscja.deviceapi.RFIDWithUHF;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.utility.StringUtility;

import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

/**
 * 指纹模块使用demo
 * 
 * 1、使用前请确认您的机器已安装此模块。 
 * 2、要正常使用模块需要在\libs\armeabi\目录放置libDeviceAPI.so文件，同时在\libs\目录下放置DeviceAPIver20160728.jar文件。 
 * 3、在操作设备前需要调用 init()打开设备，使用完后调用 free() 关闭设备
 * 
 * 
 * 更多函数的使用方法请查看API说明文档
 * 
 * @author wushengjun
 * 更新于2016年8月10日
 */
public class MainActivity extends FragmentActivity {

	private final static String TAG = "MainActivity";

	public Fingerprint mFingerprint;

	private FragmentTabHost mTabHost;
	private FragmentManager fm;

	private BaseInitTask mBaseInitTask;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.activity_main);

		fm = getSupportFragmentManager();
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, fm, R.id.realtabcontent);

		mTabHost.addTab(
				mTabHost.newTabSpec("identification").setIndicator(
						getString(R.string.fingerprint_tab_identification)),
				IdentificationFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("acquisition").setIndicator(
						getString(R.string.fingerprint_tab_acquisition)),
				AcquisitionFragment.class, null);
		mTabHost.addTab(
				mTabHost.newTabSpec("history").setIndicator(
						getString(R.string.fingerprint_tab_history)),
				HistoryFragment.class, null);
//		mTabHost.addTab(
//				mTabHost.newTabSpec("fingerprintset").setIndicator(
//						getString(R.string.fingerprint_tab_fingerprintset)),
//				FingerprintSetFragment.class, null);
		

		try {

			mFingerprint = Fingerprint.getInstance();

		} catch (Exception ex) {
			Toast.makeText(MainActivity.this, ex.getMessage(),
					Toast.LENGTH_SHORT).show();
			return;
		}

	}
	
	 public void playSound(int id) {
//	        appContext.playSound(id);
	 }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		if (mFingerprint != null) {
			mFingerprint.free();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

//		new InitTask().execute();
		
		initFingerprint(-1);
	}

	/**
	 * 设备上电异步类
	 * 
	 * @author liuruifeng 
	 */	
	public class InitTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;
		
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			return mFingerprint.init();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mypDialog.cancel();

			if (!result) {
				Toast.makeText(MainActivity.this, "init fail",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mypDialog = new ProgressDialog(MainActivity.this);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("init...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}

	}
	
	public void freeFingerprint() {

        Log.i(TAG,"freeFingerprint() ");


        if (mBaseInitTask.getStatus() == AsyncTask.Status.FINISHED && mFingerprint != null) {
            Log.i(TAG,"freeFingerprint() free");
            mFingerprint.free();
        }
    }
	
	public void initFingerprint(final int baudrate) {
        mBaseInitTask = new BaseInitTask(this) {

            @Override
            protected Boolean doInBackground(String... params) {
                // TODO Auto-generated method stub


                boolean result = false;

                if (mFingerprint != null) {

                    if(baudrate==-1) {
                        result = mFingerprint.init();
                    } else {
                        result = mFingerprint.init(baudrate);
                    }
                }

                return result;
            }

        };
        mBaseInitTask.execute();
    }

    /**
     * 验证十六进制输入是否正确
     *
     * @param str
     * @return
     */
    public boolean vailHexInput(String str) {

        if (str == null || str.length() == 0) {
            return false;
        }

        // 长度必须是偶数
        if (str.length() % 2 == 0) {
            return StringUtility.isHexNumberRex(str);
        }

        return false;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.fingerprintmain, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.action_finger_ver) {
            String strVer = "";

            if (mFingerprint != null) {

                strVer = mFingerprint.getVersion();

                if (!StringUtility.isEmpty(strVer)) {
                    char[] arr = StringUtility.hexString2Chars(strVer);

                    strVer = new String(arr);
                }

            }

            UIHelper.alert(this,
                    R.string.action_fingerprint_ver, strVer, R.drawable.webtext);
        }
        return true;
    }
	
}

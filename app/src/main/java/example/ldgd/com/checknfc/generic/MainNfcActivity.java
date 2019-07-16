/*
  * @author STMicroelectronics MMY Application team
  *
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2017 STMicroelectronics</center></h2>
  *
  * Licensed under ST MIX_MYLIBERTY SOFTWARE LICENSE AGREEMENT (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at:
  *
  *        http://www.st.com/Mix_MyLiberty
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  * AND SPECIFICALLY DISCLAIMING THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************
*/

package example.ldgd.com.checknfc.generic;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ldgd.videoediting.R;
import com.example.ldgd.videoediting.act.MainVideoActivity;
import com.example.ldgd.videoediting.entity.FtpConfig;
import com.example.ldgd.videoediting.util.FtpUtil;
import com.google.gson.Gson;
import com.st.st25sdk.MultiAreaInterface;
import com.st.st25sdk.NFCTag;
import com.st.st25sdk.STException;
import com.st.st25sdk.TagCache;
import com.st.st25sdk.TagHelper;
import com.st.st25sdk.ndef.NDEFRecord;
import com.st.st25sdk.type4a.Type4Tag;
import com.st.st25sdk.type5.Type5Tag;
import com.st.st25sdk.type5.st25dv.ST25DVTag;

import example.ldgd.com.checknfc.activity.MyPwdDialogFragment;
import example.ldgd.com.checknfc.fragment.PwdDialogFragment;
import example.ldgd.com.checknfc.generic.util.TagDiscovery;
import example.ldgd.com.checknfc.generic.util.UIHelper;


public class MainNfcActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TagDiscovery.onTagDiscoveryCompletedListener, MyPwdDialogFragment.STType5PwdDialogListener {

    private static final String TAG = "MainNfcActivity";
    private static final boolean DBG = true;

    static public Resources mResources;

    static private NFCTag mTag;

    public static final String NEW_TAG = "new_tag";

    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    private TextView mNfcWarningTextView;
    private Button mEnableNfcButton;
    private FragmentManager mFragmentManager;

    private ProgressDialog mProgress;

    @Override
    public void onSTType5PwdDialogFinish(int result) {
        Log.v(TAG, "onSTType5PwdDialogFinish. result = " + result);
        if (result == PwdDialogFragment.RESULT_OK) {
            // 读取nfc中的数据
            ReadTheBytes(0, 508);
            showToast(this.getResources().getText(R.string.present_pwd_succeeded), Toast.LENGTH_SHORT);
        } else {
            Log.e(TAG, "Action failed! Tag not updated!");
        }
    }


    public interface NfcIntentHook {
        void newNfcIntent(Intent intent);
    }

    private static NfcIntentHook mNfcIntentHook;

    public MainNfcActivity() {
    /*    if (BuildConfig.DEBUG) {
            enableDebugCode();
        }*/
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 去掉窗口标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏顶部的状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.default_layout);

        mResources = getResources();
        // Inflate content of FrameLayout
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_content);
        View childView = getLayoutInflater().inflate(R.layout.activity_nfc_main, null);
        frameLayout.addView(childView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.inflateMenu(R.menu.menu_main_activity);
        navigationView.inflateMenu(R.menu.menu_help);


        // 初始化NFC-onResume处理
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        mNfcWarningTextView = (TextView) findViewById(R.id.nfcWarningTextView);
        mEnableNfcButton = (Button) findViewById(R.id.enableNfcButton);

        mEnableNfcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                }
            }
        });

        mFragmentManager = getSupportFragmentManager();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds read_list_items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.preferred_application:
              /*   Intent intent = new Intent(this, PreferredApplicationActivity.class);
                 startActivityForResult(intent, 1);*/
                break;
            case R.id.about:
                super.onOptionsItemSelected(item);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    void processIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Log.e(TAG, "processIntent " + intent);

        if (mNfcIntentHook != null) {
            // NFC Intent hook used only for test purpose!
            mNfcIntentHook.newNfcIntent(intent);
            return;
        }

        Tag androidTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (androidTag != null) {
            // A tag has been taped

            // Perform tag discovery in an asynchronous task
            // onTagDiscoveryCompleted() will be called when the discovery is completed.
            new TagDiscovery(this).execute(androidTag);

            // This intent has been processed. Reset it to be sure that we don't process it again
            // if the MainActivity is resumed
            setIntent(null);
        }
    }

    static public void setNfcIntentHook(NfcIntentHook nfcIntentHook) {
        mNfcIntentHook = nfcIntentHook;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            Log.v(TAG, "disableForegroundDispatch");
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void onResume() {
        Intent intent = getIntent();
        Log.d(TAG, "Resume mainActivity intent: " + intent);
        super.onResume();

        processIntent(intent);

        if (mNfcAdapter != null) {
            Log.v(TAG, "enableForegroundDispatch");
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null /*nfcFiltersArray*/, null /*nfcTechLists*/);

            if (mNfcAdapter.isEnabled()) {
                // NFC enabled
                mNfcWarningTextView.setVisibility(View.INVISIBLE);
                mEnableNfcButton.setVisibility(View.INVISIBLE);
            } else {
                // NFC disabled
                mNfcWarningTextView.setText(R.string.nfc_currently_disabled);
                mNfcWarningTextView.setVisibility(View.VISIBLE);
                mEnableNfcButton.setVisibility(View.VISIBLE);
            }

        } else {
            // NFC not available on this phone!!!
            mNfcWarningTextView.setText(R.string.nfc_not_available);
            mNfcWarningTextView.setVisibility(View.VISIBLE);
            mEnableNfcButton.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        Log.d(TAG, "onNewIntent " + intent);
        setIntent(intent);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.preferred_application:
           /*     intent = new Intent(this, PreferredApplicationActivity.class);
                startActivityForResult(intent, 1);*/
                break;
            case R.id.about:
                UIHelper.displayAboutDialogBox(this);
                break;
            case R.id.activity_menu:

                // Check if an intent has been associated to this menuItem
                intent = item.getIntent();

                if (intent != null) {
                    startActivityForResult(intent, 1);
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    static public NFCTag getTag() {
        return mTag;
    }

    @Override
    public void onTagDiscoveryCompleted(NFCTag nfcTag, TagHelper.ProductID productId, STException e) {
        //Toast.makeText(getApplication(), "onTagDiscoveryCompleted. productId:" + productId, Toast.LENGTH_LONG).show();
        if (e != null) {
            Log.e(TAG, e.toString());
            Toast.makeText(getApplication(), R.string.error_while_reading_the_tag, Toast.LENGTH_LONG).show();
            return;
        }

        mTag = nfcTag;


        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        Menu menu = navigationView.getMenu();

        MenuItem menuItem = menu.findItem(R.id.activity_menu);
        Log.e("aaa productId =", "productId = " + productId);
        switch (productId) {
            case PRODUCT_ST_ST25DV64K_I:
            case PRODUCT_ST_ST25DV64K_J:
            case PRODUCT_ST_ST25DV16K_I:
            case PRODUCT_ST_ST25DV16K_J:
            case PRODUCT_ST_ST25DV04K_I:
            case PRODUCT_ST_ST25DV04K_J:
                checkMailboxActivation();

                //    startTagActivity(ST25DVActivity.class, R.string.st25dv_menus);

                // 读取nfc中的数据
                ReadTheBytes(0, 508);

                break;

         /*   case PRODUCT_ST_LRi512:
            case PRODUCT_ST_LRi1K:
            case PRODUCT_ST_LRi2K:
                startTagActivity(STLRiActivity.class, R.string.lri_menus);
                break;

            case PRODUCT_ST_LRiS2K:
                startTagActivity(STLRiS2kActivity.class, R.string.lriS2k_menus);
                break;

            case PRODUCT_ST_LRiS64K:
                startTagActivity(STLRiS64kActivity.class, R.string.lriS64k_menus);
                break;

            case PRODUCT_ST_M24SR02_Y:
            case PRODUCT_ST_M24SR04_Y:
            case PRODUCT_ST_M24SR04_G:
            case PRODUCT_ST_M24SR16_Y:
            case PRODUCT_ST_M24SR64_Y:
                startTagActivity(STM24SRActivity.class, R.string.m24sr64_menus);
                break;

            case PRODUCT_ST_ST25TA16K:
            case PRODUCT_ST_ST25TA64K:
                startTagActivity(ST25TAHighDensityActivity.class, R.string.m24sr64_menus);
                break;

            case PRODUCT_ST_ST25TV64K:
                startTagActivity(ST25DVActivity.class, R.string.st25tv64k_menus);
                break;

            case PRODUCT_ST_ST25TV02K:
            case PRODUCT_ST_ST25TV512:
                startTagActivity(ST25TVActivity.class, R.string.st25tv_menus);
                break;
            case PRODUCT_ST_ST25DV02K_W1:
            case PRODUCT_ST_ST25DV02K_W2:
                startTagActivity(ST25DVWActivity.class, R.string.st25dv02kw_menus);
                break;
            case PRODUCT_ST_M24LR16E_R:
            case PRODUCT_ST_M24LR64E_R:
            case PRODUCT_ST_M24LR64_R:
                startTagActivity(STM24LRActivity.class, R.string.m24lr64_menus);
                break;
            case PRODUCT_ST_M24LR04E_R:
                startTagActivity(STM24LR04Activity.class, R.string.m24lr04_menus);
                break;
            case PRODUCT_ST_ST25TA02K:
            case PRODUCT_ST_ST25TA02KB:
            case PRODUCT_ST_ST25TA02K_P:
            case PRODUCT_ST_ST25TA02K_D:
            case PRODUCT_ST_ST25TA512:
            case PRODUCT_ST_ST25TA512B:
            case PRODUCT_ST_ST25TA512_K:
            case PRODUCT_ST_ST25TA02KB_P:
            case PRODUCT_ST_ST25TA02KB_D:
                startTagActivity(ST25TAActivity.class, R.string.st25ta_menus);
                break;

            case PRODUCT_GENERIC_TYPE5:
                startTagActivity(GenericType5TagActivity.class, R.string.type5_menus);
                break;

            case PRODUCT_GENERIC_TYPE5_AND_ISO15693:
                startTagActivity(GenericType5TagActivity.class, R.string.type5_menus);
                break;

            case PRODUCT_GENERIC_TYPE4:
                startTagActivity(GenericType4TagActivity.class, R.string.type4_menus);
                break;*/

            default:
                menuItem.setTitle(R.string.product_unknown);
                Toast.makeText(getApplication(), getResources().getString(R.string.unknown_tag), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Product not recognized");
                break;
        }
    }


    private int mStartAddress;
    private int mNumberOfBytes;
    private ContentViewAsync contentView;

    private void ReadTheBytes(int startAddress, int numberOfBytes) {

        if (getTag() instanceof Type5Tag) {

            mStartAddress = startAddress;
            mNumberOfBytes = numberOfBytes;
            contentView = new ContentViewAsync(getTag());
            contentView.execute();
        }

    }

    private void presentPassword() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                MyPwdDialogFragment.STPwdAction pwdAction = MyPwdDialogFragment.STPwdAction.PRESENT_CURRENT_PWD;
                String message = " 输入区密码";

                int passwordNumber = ST25DVTag.ST25DV_CONFIGURATION_PASSWORD_ID;
                try {
                    ST25DVTag myTag = (ST25DVTag) mTag;
                    passwordNumber = myTag.getPasswordNumber(MultiAreaInterface.AREA4);
                } catch (STException e) {
                    e.printStackTrace();
                }
                // 参数 pwdAction : Dialog标识，passwordNumber ：得到的当前密码，message ： Dialog提示消息
                MyPwdDialogFragment pwdDialogFragment = MyPwdDialogFragment.newInstance(pwdAction, passwordNumber, message);
                pwdDialogFragment.show(mFragmentManager, "pwdDialogFragment");

            }
        }.start();

    }

    /**
     * 异步读取NFC数组
     */
    class ContentViewAsync extends AsyncTask<Void, Integer, Boolean> {
        byte mBuffer[] = null;
        NFCTag mTag;
        int mArea;

        public ContentViewAsync(NFCTag myTag) {
            mTag = myTag;
        }

        public ContentViewAsync(NFCTag myTag, int myArea) {
            mTag = myTag;
            mArea = myArea;
        }

        public ContentViewAsync(byte[] buffer) {
            mBuffer = buffer;
        }

        protected Boolean doInBackground(Void... arg0) {
            if (mBuffer == null) {
                try {
                    if (mTag instanceof Type4Tag) {
                      /*  // Tag type 4
                        int size = getMemoryAreaSizeInBytes(((Type4Tag) mTag), mArea);
                        mNumberOfBytes = size;
                        mStartAddress = 0;

                        int fileId = UIHelper.getType4FileIdFromArea(mArea);

                        // inform user that a read will be performed
                        snackBarUiThread();

                        mBuffer = ((Type4Tag) mTag).readBytes(fileId, 0, size);
                        int nbrOfBytesRead = 0;
                        if (mBuffer != null) {
                            nbrOfBytesRead = mBuffer.length;
                        }
                        if (nbrOfBytesRead != mNumberOfBytes) {
                            showToast(R.string.error_during_read_operation, nbrOfBytesRead);
                        }*/
                    } else {
                        showProgress();
                        // Type 5
                        mBuffer = mTag.readBytes(mStartAddress, mNumberOfBytes);
                        // Warning: readBytes() may return less bytes than requested
                        int nbrOfBytesRead = 0;
                        if (mBuffer != null) {
                            nbrOfBytesRead = mBuffer.length;

                            // 获取网址，读取文件到本地
                            String strData = new String(mBuffer);
                            String jsonData = strData.substring(0, strData.lastIndexOf("}") + 1);
                            Gson gson = new Gson();
                            FtpConfig config = gson.fromJson(jsonData, FtpConfig.class);

                            // 登录 ftp 服务器中下载配置文件到本地中
                            FtpUtil fipUtil = new FtpUtil(config.getIpaddr(), config.getPort(), config.getUser(), config.getPwd());
                            String serviceCatalog = "configs/" + config.getUuid() + ".json";
                            String saveCatalog = MainNfcActivity.this.getFilesDir() + "/" +  config.getUuid() +".json";
                            fipUtil.getFile(serviceCatalog, saveCatalog);

                            // 关闭加载框
                            stopProgress();
                            // 关闭 fip 服务器连接
                            fipUtil.clear();

                            // 跳转到视屏列表界面
                            Intent intent = new Intent(MainNfcActivity.this, MainVideoActivity.class);
                            intent.putExtra("ftpconfig",config);
                            startActivity(intent);

                            // 关闭当前界面
                         //   MainNfcActivity.this.finish();

                        }

                    }
                } catch (STException e) {
                    Log.e(TAG, " STException = " + e.getMessage());
                    switch (e.getError()) {
                        case ISO15693_BLOCK_PROTECTED:
                            // 输入密码
                            presentPassword();
                            break;
                        default:
                            e.printStackTrace();
                            break;
                    }
                    // 关闭加载框
                    stopProgress();
                }

            } else {
                // buffer already initialized by constructor - no need to read Tag.
                // Nothing to do
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (mBuffer != null) {
               /* LogUtil.e("mBuffer  ==  " + Arrays.toString(mBuffer));
                mAdapter = new ReadFragmentActivity.CustomListAdapter(mBuffer);
                lv = (ListView) findViewById(R.id.readBlocksListView);
                lv.setAdapter(mAdapter);*/
            }

        }

    }


    private void checkMailboxActivation() {
        new Thread(new Runnable() {
            public void run() {
                ST25DVTag st25DVTag = (ST25DVTag) mTag;

                try {
                    if (st25DVTag.isMailboxEnabled(true)) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainNfcActivity.this, getString(R.string.mailbox_enabled_eeprom_cannot_be_written), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (STException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startTagActivity(Class<?> cls, int menuTitle) {

        // We are about to start the activity related to a tag so mTag should be non null
        if (getTag() == null) {
            Log.e(TAG, "Error! Trying to start a TagActivity with a null tag!");
            return;
        }

        Log.v(TAG, "startTagActivity: " + cls.getName());

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.activity_menu);
        menuItem.setTitle(menuTitle);
        menuItem.setVisible(true);

        Intent st_intent = new Intent(this, cls);
        st_intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Flag indicating that we are displaying the information of a new tag
        st_intent.putExtra(NEW_TAG, true);

        // Save in the menuItem the intent that should be called when this menuItem is clicked
        // It allows to open the same activity with low efforts
        menuItem.setIntent(st_intent);

        startActivityForResult(st_intent, 1);
    }

    private void enableDebugCode() {

        try {
            // Put here the debug features that you want to enable
            TagCache.class.getField("DBG_CACHE_MANAGER").set(null, true);

            NDEFRecord.class.getField("DBG_NDEF_RECORD").set(null, true);


        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void showToast(final CharSequence context, final int length) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), context, length).show();
            }
        });
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress = ProgressDialog.show(MainNfcActivity.this, "", "Loading...");
            }
        });

    }

    private void stopProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mProgress != null){
                    mProgress.cancel();
                }
            }
        });
    }





}

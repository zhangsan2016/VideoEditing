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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ldgd.videoediting.R;
import com.st.st25sdk.Helper;
import com.st.st25sdk.MultiAreaInterface;
import com.st.st25sdk.STException;
import com.st.st25sdk.STLog;
import com.st.st25sdk.type5.st25dv.ST25DVTag;

import example.ldgd.com.checknfc.activity.MyPwdDialogFragment;
import example.ldgd.com.checknfc.fragment.PwdDialogFragment;
import example.ldgd.com.checknfc.fragment.STFragment;
import example.ldgd.com.checknfc.generic.util.CacheUtils;
import example.ldgd.com.checknfc.generic.util.Common;

public class WriteFragmentActivity extends STFragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, STFragment.STFragmentListener, View.OnClickListener  , MyPwdDialogFragment.STType5PwdDialogListener{

    // Set here the Toolbar to use for this activity
  //  private ST25Menu mMenu;
    private int toolbar_res = R.menu.toolbar_empty;

    // Address at which we will write the Value
    private int mByteAddress;
    private byte[] mValue;

    // Start address for the memory dump
    private int mDumpStartAddress;

    // Number of bytes shown after writing a byte
    private int mNumberOfBytes;

    private EditText mByteAddressEditText;
    private EditText mByteValueEditText;

    // The data are now read by Byte but we will still format the display by raw of 4 Bytes
    private final  int NBR_OF_BYTES_PER_RAW = 4;

    private static final String TAG = "WriteFragmentActivity";
    private ListView lv;
    private Handler mHandler;
    private CustomListAdapter mAdapter;
    private Thread mThread;
    private Button btWriteByPositionBytes;


    private  ST25DVTag myTag;
    FragmentManager mFragmentManager;

    protected void onCreate(Bundle savedInstanceState) {
        // 去掉窗口标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏顶部的状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_layout);

        // Inflate content of FrameLayout
        FrameLayout frameLayout=(FrameLayout) findViewById(R.id.frame_content);
        View childView = getLayoutInflater().inflate(R.layout.fragment_write_memory, null);
        frameLayout.addView(childView);

        myTag = (ST25DVTag) MainActivity.getTag();
        if (super.getTag() == null) {
            showToast(R.string.invalid_tag);
            goBackToMainActivity();
            return;
        }

        mFragmentManager = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

    /*    mMenu = ST25Menu.newInstance(super.getTag());
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        mMenu.inflateMenu(navigationView);*/

        mByteAddressEditText = (EditText) findViewById(R.id.byteAddressEditText);
        mByteAddressEditText.setText("0");

        mByteValueEditText = (EditText) findViewById(R.id.byteValueEditText);
        mByteValueEditText.setText("0");

        mHandler = new Handler();

        toolbar.setTitle(Common.MYLD_DEVICE_NAME);

        btWriteByPositionBytes = (Button) this.findViewById(R.id.bt_write_by_position_bytes);
        btWriteByPositionBytes.setOnClickListener(this);

        presentPassword();
    }


    private void presentPassword() {
        new Thread(){
            @Override
            public void run() {
                super.run();
                MyPwdDialogFragment.STPwdAction pwdAction = MyPwdDialogFragment.STPwdAction.PRESENT_CURRENT_PWD;
                String message = " 输入区密码";

                int passwordNumber = ST25DVTag.ST25DV_CONFIGURATION_PASSWORD_ID;
                try {
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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds read_list_items to the action bar if it is present.
        getMenuInflater().inflate(toolbar_res, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long

        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
    }

    /**
     * Parses the NdefSTMessage Message from the intent and prints to the TextView
     */
    TextView textView;

    void processIntent(Intent intent) {
        Log.d(TAG, "Process Intent");
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
       // return mMenu.selectItem(this, item);
    return false;
    }

    @Override
    public void onSTType5PwdDialogFinish(int result) {
        Log.v(TAG, "onSTType5PwdDialogFinish. result = " + result);
        if (result == PwdDialogFragment.RESULT_OK) {
            showToast(R.string.present_pwd_succeeded);
        } else {
            Log.e(TAG, "Action failed! Tag not updated!");
        }
    }

    class ContentView implements Runnable {
        public void run() {
            byte buffer[] = null;
            lv = (ListView) findViewById(R.id.writeBlockListView);

            try {
                // 写入数据到NFC
                getTag().writeBytes(mByteAddress,mValue);


                // The data has been written
                // Display 4 raws including the byte written
                mNumberOfBytes = 4 * NBR_OF_BYTES_PER_RAW;

                // Round mByteAddress to the lower multiple of NBR_OF_BYTES_PER_RAW
                mDumpStartAddress = (mByteAddress / NBR_OF_BYTES_PER_RAW) * NBR_OF_BYTES_PER_RAW;

                buffer = getTag().readBytes(mDumpStartAddress, mNumberOfBytes);

                // Warning: readBytes() may return less bytes than requested
                if(buffer.length != mNumberOfBytes) {
                    showToast(R.string.error_during_read_operation, buffer.length);
                }

            } catch (STException e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                } else {
                    Log.e(TAG, "Command failed");
                }
                showToast(R.string.Command_failed);
            }

            if (buffer != null) {
                mAdapter = new CustomListAdapter(buffer);


                if (mHandler != null && lv != null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            lv.setAdapter(mAdapter);
                        }
                    });
                }
                ;
            }
        }
    }


    class ContentView2 implements Runnable {
        public void run() {
            byte buffer[] = null;
            lv = (ListView) findViewById(R.id.writeBlockListView);

            try {
                // 写入数据到NFC
                getTag().writeBytes(0,mValue);
               getTag().writeBytes(500,new byte[]{-84});
              //  getTag().writeCCFile(mValue);
                mNumberOfBytes = mValue.length;


                // The data has been written
                // Display 4 raws including the byte written

                buffer = getTag().readBytes(0, 508);

                stopProgress();


            } catch (STException e) {
                if (e.getMessage() != null) {
                    Log.e(TAG, e.getMessage());
                } else {
                    Log.e(TAG, "Command failed");
                }
                showToast(R.string.Command_failed);
                stopProgress();
            }

            if (buffer != null) {
                mAdapter = new CustomListAdapter(buffer);


                if (mHandler != null && lv != null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            lv.setAdapter(mAdapter);
                        }
                    });
                }
                ;
            }
        }
    }

    private ProgressDialog mProgress;
    private void showProgress() {
        mProgress = ProgressDialog.show(this, "", "Loading...");

    }

    private void stopProgress() {
        mProgress.cancel();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.fab:
                showProgress();
                Toast.makeText(this,"fab " ,Toast.LENGTH_SHORT).show();
                try {
                    // 获取nfc读取的数据
                    mValue =  CacheUtils.getString(WriteFragmentActivity.this,"nfcdata");


                } catch (Exception e) {
                    STLog.e("Bad Value" + e.getMessage());
                    showToast(R.string.bad_value);
                    return;
                }
                mThread = new Thread(new ContentView2());
                mThread.start();
                break;
            case R.id.bt_write_by_position_bytes:
                // Hide Soft Keyboard (关闭键盘显示)
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                try {
                    mByteAddress = Integer.parseInt(mByteAddressEditText.getText().toString());
                }  catch (Exception e) {
                    STLog.e("Bad Address" + e.getMessage());
                    showToast(R.string.bad_address);
                    return;
                }

                try {

                    mValue =new byte[]{((byte)Integer.parseInt(mByteValueEditText.getText().toString()))};
                } catch (Exception e) {
                    STLog.e("Bad Value" + e.getMessage());
                    showToast(R.string.bad_value);
                    return;
                }

                Snackbar snackbar = Snackbar.make(v, "", Snackbar.LENGTH_LONG);

                snackbar.setAction("Writing  @ " + mByteAddress + " the value " + mValue[0], this);
                snackbar.setActionTextColor(getResources().getColor(R.color.white));

                mThread = new Thread(new ContentView());
                snackbar.show();
                mThread.start();
                break;
        }

    }

    public void onPause() {
        if (mThread != null)
            try {
                mThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Issue joining thread");
            }
        super.onPause();
    }

    class CustomListAdapter extends BaseAdapter {

        byte[] mBuffer;

        public CustomListAdapter(byte[] buffer) {

            mBuffer = buffer;
        }

        //get read_list_items count
        @Override
        public int getCount() {
            try {
                return Helper.divisionRoundedUp(mBuffer.length, NBR_OF_BYTES_PER_RAW);
            } catch (STException e) {
                e.printStackTrace();
                return 0;
            }
        }

        //get read_list_items position
        @Override
        public Object getItem(int position) {
            return position;
        }

        //get read_list_items id at selected position
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            View listItem = convertView;
            String data;
            Byte myByte;
            int address;
            char char1 = ' ';
            char char2 = ' ';
            char char3 = ' ';
            char char4 = ' ';
            String byte1Str = "  ";
            String byte2Str = "  ";
            String byte3Str = "  ";
            String byte4Str = "  ";

            // The data are now read by Byte but we will still format the display by raw of 4 Bytes

            // Get the 4 Bytes to display on this raw
            address = pos * NBR_OF_BYTES_PER_RAW;
            if(address < mBuffer.length) {
                myByte = mBuffer[address];
                byte1Str = Helper.convertByteToHexString(myByte).toUpperCase();
                char1 = getChar(myByte);
            }

            address = pos * NBR_OF_BYTES_PER_RAW + 1;
            if(address < mBuffer.length) {
                myByte = mBuffer[address];
                byte2Str = Helper.convertByteToHexString(myByte).toUpperCase();
                char2 = getChar(myByte);
            }

            address = pos * NBR_OF_BYTES_PER_RAW + 2;
            if(address < mBuffer.length) {
                myByte = mBuffer[address];
                byte3Str = Helper.convertByteToHexString(myByte).toUpperCase();
                char3 = getChar(myByte);
            }

            address = pos * NBR_OF_BYTES_PER_RAW + 3;
            if(address < mBuffer.length) {
                myByte = mBuffer[address];
                byte4Str = Helper.convertByteToHexString(myByte).toUpperCase();
                char4 = getChar(myByte);
            }

            if (listItem == null) {
                //set the main ListView's layout
                listItem = getLayoutInflater().inflate(R.layout.read_fragment_item, parent, false);
            }
            TextView addresssTextView = (TextView) listItem.findViewById(R.id.addrTextView);
            TextView hexValuesTextView = (TextView) listItem.findViewById(R.id.hexValueTextView);
            TextView asciiValueTextView = (TextView) listItem.findViewById(R.id.asciiValueTextView);

            String startAddress = String.format("%s %3d: ", getResources().getString(R.string.addr), mDumpStartAddress + pos * NBR_OF_BYTES_PER_RAW);
            addresssTextView.setText(startAddress);

            data = String.format("%s %s %s %s", byte1Str, byte2Str, byte3Str, byte4Str);
            hexValuesTextView.setText(data);

            data = String.format("  %c%c%c%c", char1, char2, char3, char4);
            asciiValueTextView.setText(data);

            return listItem;
        }
    }

    private char getChar(byte myByte) {
        char myChar = ' ';

        if(myByte > 0x20) {
            myChar = (char) (myByte & 0xFF);
        }

        return myChar;
    }

}


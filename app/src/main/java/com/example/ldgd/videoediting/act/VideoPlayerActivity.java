package com.example.ldgd.videoediting.act;


import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.ldgd.videoediting.R;
import com.xmic.tvonvif.finder.CameraDevice;

public class VideoPlayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        CameraDevice device = (CameraDevice) getIntent().getSerializableExtra("CameraDevice");
        if(device!= null){
            Toast.makeText(this,"device = " + device.getIpAddress() , Toast.LENGTH_LONG).show();

        }

    }
}

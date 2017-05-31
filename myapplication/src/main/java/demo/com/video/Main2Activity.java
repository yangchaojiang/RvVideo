package demo.com.video;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vrvideo.Vr360VideoActivity;

import demo.com.video.R;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Vr360VideoActivity.startActivity(this, Environment.getExternalStorageDirectory().getAbsolutePath() + "/vr_video_1.mp4", "222");
    }
}

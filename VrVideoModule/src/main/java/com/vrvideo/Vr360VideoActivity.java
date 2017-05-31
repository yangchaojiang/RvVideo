/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vrvideo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.vrvideo.bean.DataBean;
import com.vrvideo.config.Constant;
import com.vrvideo.in.MyVrVideoEventLister;
import com.vrvideo.utils.Utils;
import com.yutils.JsonManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import org.gearvrf.GVRActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import okhttp3.Call;
import okhttp3.MediaType;

/****
 * 视频播放页面
 * ****/
public class Vr360VideoActivity extends GVRActivity implements MyVrVideoEventLister {

    private static final String TAG = "Minimal360VideoActivity";
    private static final int USE_EXO_PLAYER = 0; //0 1系統 2 IjkMediaPlayer 3 exoplay
    Minimal360Video videoMain;
    public static boolean isRun;//是否运行

    /****
     * @param context 上下文
     *@param path 路径
     * ***/
    public static void startActivity(Context context, String path, String playGuid) {
        Intent intent = new Intent(context, Vr360VideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("path", path);
        intent.putExtra("playGuid", playGuid);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoMain = new Minimal360Video(this, getIntent().getStringExtra("path"),getIntent().getStringExtra("playGuid"));
        setMain(videoMain);
        isRun = true;
        videoMain.setMyVrVideoEventLister(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        videoMain.state(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoMain.state(false);
    }

    @Override
    protected void onDestroy() {
        isRun = false;
        super.onDestroy();
        videoMain.onDestroy();
    }


    @Override
    public void onCompletion() {
        postData(true);

    }

    @Override
    public void onLoadError(String errorMessage) {

    }

    @Override
    public void onPlayVideo(String uuid) {
        postData(false);
    }

    /*****
     * 提交数据
     *******/
    public void postData(boolean isPlay) {
        SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getTimeInstance();
        formatter.applyLocalizedPattern("HH:mm");
        DataBean dataBean = new DataBean();
        dataBean.setV_deviceMac(Utils.getLocalMacAddressFromWifiInfo(this));
        dataBean.setV_deviceSN(Utils.getLocaSerialNumber());
        dataBean.setV_playTime(formatter.format(new Date()));
        dataBean.setV_playState(isPlay ? "0" : "1");
        dataBean.setV_playGuid(videoMain.getPlayGuid());
        Log.d(TAG, JsonManager.beanToJson(dataBean));
        OkHttpUtils
                .postString()
                .url(Constant.URL)
                .content(JsonManager.beanToJson(dataBean))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.d(TAG, "onError:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d(TAG, "onResponse:" + response);
                    }
                });

    }
}

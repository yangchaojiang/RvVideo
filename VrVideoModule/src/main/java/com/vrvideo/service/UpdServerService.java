package com.vrvideo.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.vrvideo.Vr360VideoActivity;
import com.vrvideo.bean.DataUDpBean;
import com.vrvideo.config.Constant;
import com.yutils.JsonManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/*****
 * 与后台线程通信服务
 *
 * *****/
public class UpdServerService extends Service {
    private static final String TAG = UpdServerService.class.getName();
    private DatagramSocket xSocket = null;
    private boolean xStop = false;
    private String path;

    public static void startService(Context context, String path) {
        Intent intent = new Intent(context, UpdServerService.class);
        intent.putExtra("path", path);
        context.startService(intent);
    }

    public void sendCommand(DataUDpBean dataBaseBean) {
        if (dataBaseBean.getState() == Constant.VALUE_0 && !Vr360VideoActivity.isRun) {
            Vr360VideoActivity.startActivity(this, path, dataBaseBean.getGuid());
            return;
        }
        Log.d(TAG, "sendCommand");
        Intent intent = new Intent();
        intent.putExtra("data", dataBaseBean);
        intent.setAction(Constant.ACTION);
        sendBroadcast(intent);


    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void udpRecvStop() {
        xStop = true;
    }

    private void udpRecvStart() {
        xStop = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                byte[] readbuf = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(readbuf, readbuf.length);
                while (true) {
                    if (xStop) {
                        if ((xSocket != null) && (!xSocket.isClosed())) {
                            xSocket.close();
                            xSocket = null;
                        }
                        return;
                    }
                    if (xSocket == null) {
                        try {
                            xSocket = new DatagramSocket(18080);
                            Log.d(TAG, "链接服务器");
                        } catch (SocketException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                            //   return; //理论上这种情况不会出现的
                        }
                    }
                    try {
                        xSocket.receive(datagramPacket);
                        String str = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                        Log.d(TAG, str);
                        if (!str.isEmpty()) {
                            JSONObject jsonObject = new JSONObject(str);
                            sendCommand(JsonManager.jsonToBean(jsonObject.getJSONObject("result").toString(), DataUDpBean.class));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                        if (xSocket != null) {
                            if (!xSocket.isClosed()) {
                                xSocket.close();
                            }
                            xSocket = null;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }.start();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        path = intent.getStringExtra("path");
        udpRecvStart();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        udpRecvStop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}

package com.vrvideo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yangc on 2017/5/20.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated:
 */
public class DataBean implements Parcelable {
    private String v_playState; //--0:开始播放，1：结束播放 2

    private String v_deviceMac; //--本机的mac地址

    private String v_deviceSN;  //,--本机的SN号，

    private String v_playTime;  //--本次操作时间

    private String v_playGuid;  //,--视频一次完整播放的唯一ID uuid

    private  Integer  correct;// 矫正的时间 毫秒

   private   String  path;//路径
    public String getV_deviceMac() {
        return v_deviceMac;
    }

    public void setV_deviceMac(String v_deviceMac) {
        this.v_deviceMac = v_deviceMac;
    }

    public String getV_deviceSN() {
        return v_deviceSN;
    }

    public void setV_deviceSN(String v_deviceSN) {
        this.v_deviceSN = v_deviceSN;
    }

    public String getV_playState() {
        return v_playState;
    }

    public void setV_playState(String v_playState) {
        this.v_playState = v_playState;
    }

    public String getV_playGuid() {
        return v_playGuid;
    }

    public void setV_playGuid(String v_playGuid) {
        this.v_playGuid = v_playGuid;
    }

    public String getV_playTime() {
        return v_playTime;
    }

    public void setV_playTime(String v_playTime) {
        this.v_playTime = v_playTime;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public DataBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.v_playState);
        dest.writeString(this.v_deviceMac);
        dest.writeString(this.v_deviceSN);
        dest.writeString(this.v_playTime);
        dest.writeString(this.v_playGuid);
        dest.writeValue(this.correct);
        dest.writeString(this.path);
    }

    protected DataBean(Parcel in) {
        this.v_playState = in.readString();
        this.v_deviceMac = in.readString();
        this.v_deviceSN = in.readString();
        this.v_playTime = in.readString();
        this.v_playGuid = in.readString();
        this.correct = (Integer) in.readValue(Integer.class.getClassLoader());
        this.path = in.readString();
    }

    public static final Creator<DataBean> CREATOR = new Creator<DataBean>() {
        public DataBean createFromParcel(Parcel source) {
            return new DataBean(source);
        }

        public DataBean[] newArray(int size) {
            return new DataBean[size];
        }
    };
}

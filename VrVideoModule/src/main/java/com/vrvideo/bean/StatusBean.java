package com.vrvideo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yangc on 2017/5/20.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated:
 */
public class StatusBean implements Parcelable {
    private String code;
    private String Msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.Msg);
    }

    public StatusBean() {
    }

    protected StatusBean(Parcel in) {
        this.code = in.readString();
        this.Msg = in.readString();
    }

    public static final Parcelable.Creator<StatusBean> CREATOR = new Parcelable.Creator<StatusBean>() {
        public StatusBean createFromParcel(Parcel source) {
            return new StatusBean(source);
        }

        public StatusBean[] newArray(int size) {
            return new StatusBean[size];
        }
    };
}

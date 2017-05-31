package com.vrvideo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yangc on 2017/5/22.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated:
 */
public class DataUDpBean implements Parcelable {
    public static final String TAG = "DataUDpBean";
    private String date;
    private  String state;
    private  String Guid;//

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getGuid() {
        return Guid;
    }

    public void setGuid(String guid) {
        Guid = guid;
    }

    public DataUDpBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.date);
        dest.writeString(this.state);
        dest.writeString(this.Guid);
    }

    protected DataUDpBean(Parcel in) {
        this.date = in.readString();
        this.state = in.readString();
        this.Guid = in.readString();
    }

    public static final Creator<DataUDpBean> CREATOR = new Creator<DataUDpBean>() {
        public DataUDpBean createFromParcel(Parcel source) {
            return new DataUDpBean(source);
        }

        public DataUDpBean[] newArray(int size) {
            return new DataUDpBean[size];
        }
    };
}

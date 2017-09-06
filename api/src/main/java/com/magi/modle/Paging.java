package com.magi.modle;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-29-2017
 */

public class Paging implements Parcelable {

    private String mPrevious;

    private String mNext;

    public boolean isEnd;

    public String beforeId;

    public String afterId;

    public String limit;

    public String sinceId;

    public Long totals;

    protected Paging(Parcel in) {
        mPrevious = in.readString();
        mNext = in.readString();
        isEnd = in.readByte() != 0;
        beforeId = in.readString();
        afterId = in.readString();
        limit = in.readString();
        sinceId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPrevious);
        dest.writeString(mNext);
        dest.writeByte((byte) (isEnd ? 1 : 0));
        dest.writeString(beforeId);
        dest.writeString(afterId);
        dest.writeString(limit);
        dest.writeString(sinceId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Paging> CREATOR = new Creator<Paging>() {
        @Override
        public Paging createFromParcel(Parcel in) {
            return new Paging(in);
        }

        @Override
        public Paging[] newArray(int size) {
            return new Paging[size];
        }
    };
}
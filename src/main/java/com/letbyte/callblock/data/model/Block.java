package com.letbyte.callblock.data.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import com.letbyte.callblock.BR;


/**
 * Created by nuc on 9/29/2015.
 */

public class Block extends BaseObservable implements Parcelable {
    private int id;
    private long time;
    private String number;
    private String displayName;

    public Block() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeLong(time);
        dest.writeString(number);
        dest.writeString(displayName);
    }

    protected Block(Parcel in) {
        id = in.readInt();
        time = in.readLong();
        number = in.readString();
        displayName = in.readString();
    }

    public static final Creator<Block> CREATOR = new Creator<Block>() {
        @Override
        public Block createFromParcel(Parcel in) {
            return new Block(in);
        }

        @Override
        public Block[] newArray(int size) {
            return new Block[size];
        }
    };

    public int getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    @Bindable
    public String getNumber() {
        return number;
    }

    @Bindable
    public String getDisplayName() {
        return displayName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setNumber(String number) {
        this.number = number;
        notifyPropertyChanged(BR.number);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName; notifyPropertyChanged(BR.displayName);
    }

}

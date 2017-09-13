package com.example.leesanghoon.newepubreader.Model;

import android.os.Parcel;
import android.os.Parcelable;


public class BookItem implements Parcelable {
    public String name;
    public String path;

    public BookItem(String name, String path) {
        this.name = name;
        this.path = path;
    }

    private BookItem(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
    }

    public static final Parcelable.Creator<BookItem> CREATOR = new Parcelable.Creator<BookItem>() {
        public BookItem createFromParcel(Parcel in) {
            return new BookItem(in);
        }

        public BookItem[] newArray(int size) {
            return new BookItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.path);
    }
}

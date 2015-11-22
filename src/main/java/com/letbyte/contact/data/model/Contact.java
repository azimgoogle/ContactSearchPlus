package com.letbyte.contact.data.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spanned;

import com.letbyte.contact.BR;
import com.letbyte.contact.control.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuc on 8/2/2015.
 */

public class Contact extends BaseObservable implements Parcelable {

    private long id;
    private String namePrimary;
    private String subText;
    private Spanned subTextSpanned;
    private String imageUri;
    private List<String> names;
    private List<String> numbers;
    private List<String> emails;
    private List<String> addresses;
    private List<String> notes;
    private List<String> companies;
    private List<String> relationShips;
    private List<List<String>> dataIndices;
    private boolean frequent;

    private Contact() {
    }

    public Contact(long id, String namePrimary, String imageUri, List<String> names, boolean frequent) {
        this(id, namePrimary, imageUri, names);
        this.frequent = frequent;
    }

    public Contact(long id, String namePrimary, String imageUri, List<String> names) {
        this.id = id;
        this.namePrimary = namePrimary;
        this.imageUri = imageUri;

        this.names = names;

        this.numbers = new ArrayList<>(3);
        this.emails = new ArrayList<>(1);
        this.notes = new ArrayList<>(0);
        this.addresses = new ArrayList<>(0);
        this.companies = new ArrayList<>(0);
        this.relationShips = new ArrayList<>(0);
        this.dataIndices = new ArrayList<>();
        this.dataIndices.add(Constant.DISPLAY_NAME, names);
        this.dataIndices.add(Constant.PHONE_NUMBER, numbers);
        this.dataIndices.add(Constant.MAIL_ADDRESS, emails);
        this.dataIndices.add(Constant.ADDRESS, addresses);
        this.dataIndices.add(Constant.NOTES, notes);
        this.dataIndices.add(Constant.ORGANIZATION, companies);
        this.dataIndices.add(Constant.RELATION, relationShips);
    }


    public long getId() {
        return id;
    }

    @Bindable
    public String getNamePrimary() {
        return namePrimary;
    }

    @Bindable
    public String getSubText() {
        return subText;
    }

    @Bindable
    public Spanned getSubTextSpanned() {
        return subTextSpanned;
    }

    public String getImageUri() {
        return imageUri;
    }

    @Bindable
    public boolean getFrequent() {
        return frequent;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public List<String> getEmails() {
        return emails;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public List<String> getNotes() {
        return notes;
    }

    public List<String> getCompanies() {
        return companies;
    }

    public List<String> getRelationShips() {
        return relationShips;
    }

    public List<List<String>> getDataIndices() {
        return dataIndices;
    }

    public List<String> getDataIndicesByDataIndex(int index) {
        return dataIndices.get(index);
    }


    public void setId(long id) {
        this.id = id;
    }

    public void setNamePrimary(String namePrimary) {
        this.namePrimary = namePrimary;
        notifyPropertyChanged(BR.namePrimary);
    }

    public void setSubText(String subText) {
        this.subText = subText;
        notifyPropertyChanged(BR.subText);
    }

    public void setSubTextSpanned(Spanned subTextSpanned) {
        this.subTextSpanned = subTextSpanned;
        notifyPropertyChanged(BR.subTextSpanned);

        setSubText(subTextSpanned.toString());
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public void setCompanies(List<String> companies) {
        this.companies = companies;
    }

    public void setRelationShips(List<String> relationShips) {
        this.relationShips = relationShips;
    }

    public void setDataIndices(List<List<String>> dataIndices) {
        this.dataIndices = dataIndices;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(namePrimary);
        dest.writeString(subText);
        dest.writeString(imageUri);
        dest.writeStringList(names);
        dest.writeStringList(numbers);
        dest.writeStringList(emails);
        dest.writeStringList(addresses);
        dest.writeStringList(notes);
        dest.writeStringList(companies);
        dest.writeStringList(relationShips);
        dest.writeInt(dataIndices.size());
        for (List<String> dataIndex : dataIndices) {
            dest.writeStringList(dataIndex);
        }
    }

    protected Contact(Parcel in) {
        id = in.readLong();
        namePrimary = in.readString();
        subText = in.readString();
        imageUri = in.readString();
        names = in.createStringArrayList();
        numbers = in.createStringArrayList();
        emails = in.createStringArrayList();
        addresses = in.createStringArrayList();
        notes = in.createStringArrayList();
        companies = in.createStringArrayList();
        relationShips = in.createStringArrayList();
        int indices = in.readInt();
        for (int i = 0; i < indices; i++) {
            List<String> dataIndex = in.createStringArrayList();
            dataIndices.add(dataIndex);
        }
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

}
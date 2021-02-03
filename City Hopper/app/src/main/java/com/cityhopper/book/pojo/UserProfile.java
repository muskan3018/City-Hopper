package com.cityhopper.book.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class UserProfile implements Parcelable{
    private String name, contact, address, cuisine, places, vegetarian, drinker, gender, age;

    public UserProfile(@NonNull String UName,@NonNull String UContact, @NonNull String UAge
                    ,@NonNull String UGender,@NonNull String UVeg,@NonNull String UDrinker
                    , @NonNull String UAddress,@NonNull String UCuisine,@NonNull String UPlaces){
        setName(UName);
        setContact(UContact);
        setAge(UAge);
        setGender(UGender);
        setVegetarian(UVeg);
        setDrinker(UDrinker);
        setAddress(UAddress);
        setCuisine(UCuisine);
        setPlaces(UPlaces);
    }

    protected UserProfile(Parcel in) {
        name = in.readString();
        contact = in.readString();
        address = in.readString();
        vegetarian = in.readString();
        drinker = in.readString();
        cuisine = in.readString();
        places = in.readString();
    }

    public static final Creator<UserProfile> CREATOR = new Creator<UserProfile>() {
        @Override
        public UserProfile createFromParcel(Parcel in) {
            return new UserProfile(in);
        }

        @Override
        public UserProfile[] newArray(int size) {
            return new UserProfile[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(String vegetarian) {
        this.vegetarian = vegetarian;
    }

    public String getDrinker() {
        return drinker;
    }

    public void setDrinker(String drinker) {
        this.drinker = drinker;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getPlaces() {
        return places;
    }

    public void setPlaces(String places) {
        this.places = places;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(contact);
        dest.writeString(address);
        dest.writeString(vegetarian);
        dest.writeString(drinker);
        dest.writeString(cuisine);
        dest.writeString(places);
    }
}

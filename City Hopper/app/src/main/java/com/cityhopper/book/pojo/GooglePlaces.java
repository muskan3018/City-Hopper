package com.cityhopper.book.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class GooglePlaces implements Parcelable{

    private String gPlaceName, gPlaceAddress, gPlaceId, gPlaceReview, gType;
    private float gPlaceRating;
    private LatLng gPlaceLocation;
    private double distanceFromSource;
    private int timeToTravel, timeSpent, srno, dayno;
    private boolean gPlaceOpen;

    public GooglePlaces(){

    }

    public GooglePlaces(Parcel in) {
        gPlaceName = in.readString();
        gPlaceAddress = in.readString();
        gPlaceId = in.readString();
        gPlaceReview = in.readString();
        gType = in.readString();
        gPlaceRating = in.readFloat();
        gPlaceLocation = in.readParcelable(LatLng.class.getClassLoader());
        distanceFromSource = in.readDouble();
        timeToTravel = in.readInt();
        timeSpent = in.readInt();
        srno = in.readInt();
        dayno = in.readInt();
        gPlaceOpen = in.readByte() != 0;
    }

    public static final Creator<GooglePlaces> CREATOR = new Creator<GooglePlaces>() {
        @Override
        public GooglePlaces createFromParcel(Parcel in) {
            return new GooglePlaces(in);
        }

        @Override
        public GooglePlaces[] newArray(int size) {
            return new GooglePlaces[size];
        }
    };

    public String getgPlaceName() {
        return gPlaceName;
    }

    public void setgPlaceName(String gPlaceName) {
        this.gPlaceName = gPlaceName;
    }

    public String getgPlaceAddress() {
        return gPlaceAddress;
    }

    public void setgPlaceAddress(String gPlaceAddress) {
        this.gPlaceAddress = gPlaceAddress;
    }

    public float getgPlaceRating() {
        return gPlaceRating;
    }

    public void setgPlaceRating(float gPlaceRating) {
        this.gPlaceRating = gPlaceRating;
    }

    public LatLng getgPlaceLocation() {
        return gPlaceLocation;
    }

    public void setgPlaceLocation(LatLng gPlaceLocation) {
        this.gPlaceLocation = gPlaceLocation;
    }

    public String getgPlaceId() {
        return gPlaceId;
    }

    public void setgPlaceId(String gPlaceId) {
        this.gPlaceId = gPlaceId;
    }

    public double getDistanceFromSource() {
        return distanceFromSource;
    }

    public void setDistanceFromSource(double distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
    }

    public int getTimeToTravel() {
        return timeToTravel;
    }

    public void setTimeToTravel(int timeToTravel) {
        this.timeToTravel = timeToTravel;
    }


    public String getgPlaceReview() {
        return gPlaceReview;
    }

    public void setgPlaceReview(String gPlaceReview) {
        this.gPlaceReview = gPlaceReview;
    }

    public boolean isgPlaceOpen() {
        return gPlaceOpen;
    }

    public void setgPlaceOpen(boolean gPlaceOpen) {
        this.gPlaceOpen = gPlaceOpen;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getgType() {
        return gType;
    }

    public void setgType(String gType) {
        this.gType = gType;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public int getDayno() {
        return dayno;
    }

    public void setDayno(int dayno) {
        this.dayno = dayno;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(gPlaceName);
        dest.writeString(gPlaceAddress);
        dest.writeString(gPlaceId);
        dest.writeString(gPlaceReview);
        dest.writeString(gType);
        dest.writeFloat(gPlaceRating);
        dest.writeParcelable(gPlaceLocation, flags);
        dest.writeDouble(distanceFromSource);
        dest.writeInt(timeToTravel);
        dest.writeInt(timeSpent);
        dest.writeInt(srno);
        dest.writeInt(dayno);
        dest.writeByte((byte) (gPlaceOpen ? 1 : 0));
    }
}

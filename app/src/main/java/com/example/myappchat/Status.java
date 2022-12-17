package com.example.myappchat;

public class Status
{

    private String imageurl;
    private long timeStamp;

    public Status()
    {

    }

    public Status(String imageurl,Long timeStamp)
    {
        this.imageurl=imageurl;
        this.timeStamp=timeStamp;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

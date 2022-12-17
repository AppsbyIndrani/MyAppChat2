package com.example.myappchat;

public class Messages {

    private String messageID, message, senderID, type, imageurl,pdfurl,videourl;
    private boolean isseen;
    private long timestamp;
    private int feeling = -1;

    public Messages() {

    }

    public Messages(String message, String senderID, String type, long timestamp,boolean isseen) {

        this.message = message;
        this.senderID = senderID;
        this.type = type;
        this.timestamp = timestamp;
        this.isseen=isseen;


    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getPdfurl() {
        return pdfurl;
    }

    public void setPdfurl(String pdfurl) {
        this.pdfurl = pdfurl;
    }

    public boolean getisseen() {
        return isseen;
    }

    public void setisseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }
}

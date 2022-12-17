package com.example.myappchat;

public class Contacts
{

    public String phoneno,name,status,image,token;

    public Contacts ()
    {

    }

    public Contacts(String phoneno,String name,String status,String image,String token) {
        this.phoneno=phoneno;
        this.name=name;
        this.status=status;
        this.image=image;
        this.token=token;

    }

    public String getPhoneno() {
        return phoneno;
    }

    public void setPhoneno(String phoneno) {
        this.phoneno = phoneno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status=status;
    }
    public String getImage(){
        return image;
    }
    public void setImage(String image){
        this.image=image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

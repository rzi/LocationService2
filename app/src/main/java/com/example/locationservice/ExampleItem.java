package com.example.locationservice;

public class ExampleItem {
    private int mImageResource;
    private String mText1;
    private String mText2;


    public ExampleItem(int ImageResource,String text1, String text2 ) {
        this.mImageResource = mImageResource;
        mText1 = text1;
        mText2 = text2;
    }
    public int getmImageResource(){
        return mImageResource;
    }
    public String getmText1(){
        return mText1;
    }

    public String getmText2 (){
        return mText2;
    }
}

package com.example.wifihttpokdemo;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class wifiManager {
    public static String getUrl(String url) //request the given url from client
    {
        OkHttpClient client=new OkHttpClient(); //set client
        Request request=new Request.Builder().url(url).build(); //request url
        try{
            Response response= client.newCall(request).execute();   //get response, catching IOException
            return response.body().string();    //return response if exception isn't thrown
        }catch (IOException e)
        {
            return e.toString();    //return error message when exception is caught
        }
    }
}

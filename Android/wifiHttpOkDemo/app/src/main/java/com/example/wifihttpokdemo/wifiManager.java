package com.example.wifihttpokdemo;

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
            String[] eStr = e.toString().split(" ");

            return eStr[eStr.length-1];    //return error message when exception is caught
        }
    }


    public static boolean isIpValid(String ipAddress)
    {
        String response;
        response = getUrl("http://" + ipAddress);   //check connection test without sending any /url
        if (response!="ConnectionError") //Valid if error was not thrown
        {
            return true;
        }
        else    //invalid if error was thrown
        {
            return false;
        }

    }


}

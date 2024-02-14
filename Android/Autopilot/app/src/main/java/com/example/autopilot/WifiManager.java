package com.example.autopilot;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WifiManager {
    static ExecutorService wifiExecutor = Executors.newSingleThreadExecutor();
    public static String getUrl(String url) //request the given url from client
    {
        OkHttpClient client=new OkHttpClient(); //set client
        Request request=new Request.Builder().url(url).build(); //request url
        try{
            Response response= client.newCall(request).execute();   //get response, catching IOException
            return response.body().string();    //return response if exception isn't thrown
        }catch (IOException e)
        {
            e.printStackTrace();
            return "ConnectionError";    //return error message when exception is caught
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
    public static void requestToUrl(String command,Context context){      //make request from given command
        ConnectivityManager connectivityManager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  //Get Connectivity Service
        Network network=connectivityManager.getActiveNetwork();     //Get the Active network ->needs to be non-null
        NetworkCapabilities networkCapabilities= connectivityManager.getNetworkCapabilities(network); //get Capabilities, needs to be non-null with WiFi Transport
        if(network!=null && networkCapabilities!=null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ){ //validate connection case
            wifiExecutor.execute(new Runnable() {   //set execution on executor thread
                @Override
                public void run() {     //execute when running
                    getUrl("http://" + "192.168.4.1" + "/" + command);   //format command by given ip address on previous intent
                }
            });
        }
        else {
            Toast.makeText(context, "Device Not Connected!", Toast.LENGTH_SHORT).show();    //give connection error via pop-up toast
        }
    }

}

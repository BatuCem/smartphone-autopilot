package com.example.autopilot;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WifiManager {
    static ExecutorService wifiExecutor = Executors.newSingleThreadExecutor();
    public static int distanceLiDAR;
    public static int angleLiDAR;
    public static int [] lidarMap = new int[180];
    private static final String TAG = "WiFiManager";


    public static String getUrl(String url) //request the given url from client
    {
        OkHttpClient client=new OkHttpClient(); //set client
        Request request=new Request.Builder().url(url).build(); //request url
        try{
            Response response= client.newCall(request).execute();  //get response, catching IOException
            String responseStr = response.body().string();
            String[] responseStrSplit = responseStr.split(" ");
            Log.i(TAG, "getUrl: "+ responseStr);
            if(responseStrSplit.length >= 2)
            {
                try {
                    distanceLiDAR = Integer.parseInt(responseStrSplit[0]);
                    angleLiDAR = Integer.parseInt(responseStrSplit[1]);
                    if(angleLiDAR > 0 && angleLiDAR < 180)
                    {
                        lidarMap[angleLiDAR] = distanceLiDAR;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return responseStr;    //return response if exception isn't thrown
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

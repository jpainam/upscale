package com.copell.upscale.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.copell.upscale.R;


import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    private static final String TAG = "Utils";
    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= 24;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info : networkInfos) {
            if (info.getTypeName().equalsIgnoreCase("WIFI"))
                if (info.isConnected())
                    return true;
            if (info.getTypeName().equalsIgnoreCase("MOBILE"))
                if (info.isConnected())
                    return true;


        }
        return false;
    }

    /*public static void sendRegistrationToPhpServer(Context context, String token, String phone_number){
        Log.i(TAG, "Send Token to Server " + token);
        AsyncTask<String, Void, Boolean> asyncTask = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                String mToken = "";
                if(params.length > 0) {
                    mToken = params[0];
                }
                String phone = "";
                if(params.length > 1 && params[1] != null) {
                    phone = params[1];
                }
                String url = context.getString(R.string.update_token_url);
                OkHttpClient client =new OkHttpClient();
                Log.e(TAG, url);
                RequestBody body =new FormBody.Builder()
                        .add("phone_number", phone)
                        .add("token", mToken)
                        .build();
                Request newReq=new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(newReq).execute();
                    String jsonData = response.body().string();
                    Log.e(TAG, jsonData);
                }catch (Exception ex){
                    ex.printStackTrace();
                    Log.e(TAG, ex.getMessage());
                }
                return  true;
            }

        };
        //asyncTask.execute(token, phone_number);
    }*/
}

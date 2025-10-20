package org.telegram.ext.respository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.telegram.ext.model.IpApiResponse;
import org.telegram.messenger.AndroidUtilities;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataRepository {

    public DataRepository() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.callTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        client = builder.build();
    }

    private static DataRepository sInstance;

    public static synchronized DataRepository getInstance() {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository();
                }
                return sInstance;
            }
        }
        return sInstance;
    }

    private OkHttpClient client;

    public void getIpAddress(SimpleCallback<IpApiResponse> callback) {
        String url = "http://ip-api.com/json?fields=status,message,query,country,city,isp,lat,lon&key=jEaAGgR2oJKjvvN";
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("getIpAddress", "error  -------> " + e.toString());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = Objects.requireNonNull(response.body()).string();
                Gson gson = new Gson();
                IpApiResponse info = gson.fromJson(body, IpApiResponse.class);
                AndroidUtilities.runOnUIThread(() -> {
                    if (null != info) {
                        Log.e("getIpAddress", "info  -------> " + info);
                        callback.onResp(info);
                    }
                });
            }
        });
    }

}

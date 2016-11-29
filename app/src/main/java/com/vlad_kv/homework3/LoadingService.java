package com.vlad_kv.homework3;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadingService extends Service implements Runnable {
    private static final String LOG_TAG = "my_service_tag";

    private static final String IMAGE_URL = "http://st.gdefon.com/wallpapers_original/s/574936_doroga_luchi_solntse_kaliforniya_zabor_ssha_derevy_2048x1365_(www.GdeFon.ru).jpg";

    private File file;

    public static boolean isItRunningNow = false;
    public static Thread it = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        if (!isItRunningNow) {
            isItRunningNow = true;

            Log.d(LOG_TAG, "new_tread");

            file = new File(getFilesDir(), MainActivity.FILE_NAME);
            it = new Thread(this);
            it.start();
        }
        return START_STICKY;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        boolean isDownloadingSuccessful = false;

        try {
            Thread.sleep(2000);
        } catch (Exception ex) {};

        HttpURLConnection connection = null;

        try {
            outputStream = new FileOutputStream(file);

            try {
                connection = (HttpURLConnection) new URL(IMAGE_URL).openConnection();
                inputStream = connection.getInputStream();
            } catch (Exception ex) {
                Log.d(LOG_TAG, "Error in creation new connection " + ex.getCause());
                throw ex;
            }

            byte[] buffer = new byte[1024 * 8];
            int size = inputStream.read(buffer);
            while (size >= 0) {
                Log.d(LOG_TAG, String.valueOf(size));

                outputStream.write(buffer, 0, size);

                Log.d(LOG_TAG, "center");

                Log.d(LOG_TAG, "num_av : " + String.valueOf(inputStream.available()));


                size = inputStream.read(buffer);

                Log.d(LOG_TAG, "edt");
            }
            connection.disconnect();

            isDownloadingSuccessful = true;
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Error in downloading");
            isDownloadingSuccessful = false;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception ex) {
                Log.d(LOG_TAG, "Error in closing");
                isDownloadingSuccessful = false;
            }
        }

        Log.d(LOG_TAG, "pre stop");

        isItRunningNow = false;

        if (isDownloadingSuccessful) {
            sendBroadcast(new Intent(MainActivity.DOWNLOADING_RESULT).putExtra("RES", MainActivity.DOWNLOADING_SUCCESSFUL));
        } else {
            sendBroadcast(new Intent(MainActivity.DOWNLOADING_RESULT).putExtra("RES", MainActivity.DOWNLOADING_FALLING));
        }

        Log.d(LOG_TAG, " stop");

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isItRunningNow = false;
        Log.d(LOG_TAG, "onDestroy!!!");
        it = null;

    }

}



















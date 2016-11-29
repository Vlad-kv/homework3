package com.vlad_kv.homework3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String FILE_NAME = "myImage.jpg";

    public static final int DOWNLOADING_SUCCESSFUL = 1;
    public static final int DOWNLOADING_FALLING = 0;
    public static final String DOWNLOADING_RESULT = "DownloadingResult";
    private final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    public static final String LOG_TAG = "log_from_main_activity";

    private BroadcastReceiver downloadReceiver;
    private BroadcastReceiver connectivityChangeReceiver;

    private ImageView myImageView;
    private ImageView noImageIcon;
    private TextView informText;
    private ProgressBar progressBar;

    void initViews() {
        myImageView = (ImageView) findViewById(R.id.myImageView);
        noImageIcon = (ImageView) findViewById(R.id.noImageIcon);
        informText = (TextView) findViewById(R.id.informText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    void initReceivers() {
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(DOWNLOADING_RESULT)) {
                    if (intent.getIntExtra("RES", DOWNLOADING_FALLING) == DOWNLOADING_SUCCESSFUL) {
                        tryToShow();
                    } else {
                        setErrorState();
                    }
                }
            }
        };

        connectivityChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(CONNECTIVITY_CHANGE)) {
                    final ConnectivityManager connectivityManager =
                            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    final android.net.NetworkInfo wifi = connectivityManager
                            .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    final android.net.NetworkInfo mobile = connectivityManager
                            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    if (wifi.isAvailable() || mobile.isAvailable()) {
                        tryToShow();
                    }
                }
            }
        };

        registerReceivers();
    }

    void readFromFile() {
        File file = new File(getFilesDir(), FILE_NAME);
        MyApplication.savedImage = null;

        if (file.exists()) {
            MyApplication.savedImage = BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        setNormalState();
    }

    void registerReceivers() {
        registerReceiver(downloadReceiver, new IntentFilter(DOWNLOADING_RESULT));
        registerReceiver(connectivityChangeReceiver, new IntentFilter(CONNECTIVITY_CHANGE));
    }

    void unregisterReceivers() {
        this.unregisterReceiver(downloadReceiver);
        this.unregisterReceiver(connectivityChangeReceiver);
    }

    void setNoInternetState() {
        myImageView.setVisibility(View.INVISIBLE);
        noImageIcon.setVisibility(View.VISIBLE);
        informText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        informText.setText("No file, no internet");
    }

    void setErrorState() {
        setNoInternetState();
        informText.setText("No file, error in downloading");
    }

    void setInDownloadingState() {
        myImageView.setVisibility(View.INVISIBLE);
        noImageIcon.setVisibility(View.INVISIBLE);
        informText.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        informText.setText("Downloading");
    }

    void setNormalState() {
        myImageView.setVisibility(View.VISIBLE);
        noImageIcon.setVisibility(View.INVISIBLE);
        informText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        myImageView.setImageBitmap(MyApplication.savedImage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initViews();
        initReceivers();


        tryToShow();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    void tryToShow() {
        if (MyApplication.savedImage != null) {
            Log.d(LOG_TAG, "showing existing image");
            setNormalState();
            return;
        }
        if (LoadingService.isItRunningNow) {
            return;
        }
        File file = new File(getFilesDir(), FILE_NAME);
        if (file.exists()) {
            Log.d(LOG_TAG, "reading_from_file");
            readFromFile();
        } else {
            if (!isConnectionAvailable(this, false)) {
                setNoInternetState();
            } else {
                setInDownloadingState();
                startService(new Intent(this, LoadingService.class));
            }
        }
    }

    void reset() {
        if (LoadingService.isItRunningNow) {
            //LoadingService.it.stop();
            stopService(new Intent(""));
        }
        File file = new File(getFilesDir(), FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        MyApplication.savedImage = null;

        tryToShow();
    }

    public void onClickBottom(View view) {
        reset();
    }

    public static boolean isConnectionAvailable(@NonNull Context context, boolean defaultValue) {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return defaultValue;
        }

        final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
}
package com.goroya.kotlinfelicaexample;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.goroya.kotlinfelicalib.FelicaLib;
import com.goroya.kotlinfelicalib.FelicaLibException;
import com.goroya.kotlinfelicalib.command.PollingCC;
import com.goroya.kotlinfelicalib.command.PollingRC;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger.addLogAdapter(new AndroidLogAdapter());
        Logger.d("onCreate");

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        FelicaLib felica = new FelicaLib(tag);
        try{
            PollingRC res = felica.polling(
                    0xFFFF,
                    PollingCC.RequestCode.SystemCodeRequest,
                    PollingCC.TimeSlot.MaximumNumberOfSlot1
            );
            Logger.d(res);
        }catch (FelicaLibException ex){
            Logger.d("FelicaLibException");
            Logger.d(ex.getMessage());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

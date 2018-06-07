package com.goroya.kotlinfelicaexample;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.goroya.kotlinfelicalib.FelicaLib;
import com.goroya.kotlinfelicalib.FelicaLibException;
import com.goroya.kotlinfelicalib.command.BlockElement;
import com.goroya.kotlinfelicalib.command.PollingCC;
import com.goroya.kotlinfelicalib.command.PollingRC;
import com.goroya.kotlinfelicalib.command.ReadWithoutEncryptionRC;
import com.goroya.kotlinfelicalib.command.RequestResponseRC;
import com.goroya.kotlinfelicalib.command.RequestServiceRC;
import com.goroya.kotlinfelicalib.command.RequestSystemCodeRC;
import com.goroya.kotlinfelicalib.command.SearchServiceCodeRC;
import com.goroya.kotlinfelicalib.command.WriteWithoutEncryptionCC;
import com.goroya.kotlinfelicalib.command.WriteWithoutEncryptionRC;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

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
        try {
            PollingRC pollingRes = felica.polling(
                    0xFFFF,
                    PollingCC.RequestCode.SystemCodeRequest,
                    PollingCC.TimeSlot.MaximumNumberOfSlot1
            );
            Logger.d("Polling");
            Logger.d(pollingRes);

            RequestServiceRC requestServiceRes = felica.requestService(
                    pollingRes.getIdm(),
                    new int[]{0x000F, 0x000F}
            );
            Logger.d("RequestServiceRes");
            Logger.d(requestServiceRes);

            RequestResponseRC requestResponseRC = felica.requestResponse(
                    pollingRes.getIdm()
            );
            Logger.d("RequestResponse");
            Logger.d(requestResponseRC);

            RequestSystemCodeRC requestSystemCode = felica.requestSystemCode(
                    pollingRes.getIdm()
            );
            Logger.d("RequestSystemCode");
            Logger.d(requestSystemCode);

            int[] a = {0x090f};
            BlockElement bl1 = new BlockElement(
                    BlockElement.Length.BlockListElementOf2Byte,
                    BlockElement.AccessMode.ReadOperationOrWriteOperation,
                    0,
                    0);
            BlockElement bl2 = new BlockElement(
                    BlockElement.Length.BlockListElementOf2Byte,
                    BlockElement.AccessMode.ReadOperationOrWriteOperation,
                    0,
                    1);
            ArrayList<BlockElement> bl = new ArrayList<>();
            bl.add(bl1);
            bl.add(bl2);

            ReadWithoutEncryptionRC readWithoutEncryptionCC = felica.readWithoutEncryption(
                    pollingRes.getIdm(), a, bl
            );
            Logger.d("readWithoutEncryption");
            Logger.d(readWithoutEncryptionCC);

            ArrayList<BlockElement> blw = new ArrayList<>();
            blw.add(bl1);

            /*
            byte[][] bb = {
                    {
                            0x11, 0x22, 0x33, 0x44,
                            0x55, 0x66, 0x77, (byte)0x88,
                            (byte) 0x99, (byte)0xAA, (byte)0xBB, (byte)0xCC,
                            (byte)0xDD, (byte)0xEE, (byte)0xFF, (byte)0xFE
                    }
            };
            WriteWithoutEncryptionRC writeWithoutEncryptionRC = felica.writeWithoutEncryption(
                    pollingRes.getIdm(), a, blw, bb
            );
            Logger.d("writeWithoutEncryption");
            Logger.d(writeWithoutEncryptionRC);
            */

            for(int i = 0; i < 255; i++){
                SearchServiceCodeRC searchServiceCodeRC = felica.searchServiceCode(
                        pollingRes.getIdm(), i
                );
                Logger.d(searchServiceCodeRC);
                if(searchServiceCodeRC.getArea().size() == 0 && searchServiceCodeRC.getServiceCode().length == 0){
                    break;
                }
            }
            /*
            RequestSpecificationVersionRC requestSpecificationVersion = felica.requestSpecificationVersion(
                    pollingRes.getIdm()
            );
            Logger.d("RequestSpecificationVersion");
            Logger.d(requestSpecificationVersion);
            */

            /*
            ResetModeRC resetMode = felica.resetMode(
                    pollingRes.getIdm()
            );
            Logger.d("ResetMode");
            Logger.d(resetMode);
            */
        } catch (FelicaLibException ex) {
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

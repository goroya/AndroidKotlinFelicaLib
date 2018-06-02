package com.goroya.kotlinfelicalib

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.app.PendingIntent
import android.content.IntentFilter.MalformedMimeTypeException
import android.content.IntentFilter
import com.goroya.kotlinfelicalib.command.PollingCC
import android.R.attr.tag
import android.app.Activity
import android.nfc.tech.NfcF
import com.goroya.kotlinfelicalib.command.PollingRC
import java.io.IOException


/**
 * This class supports greeting people by name.
 *
 * @property name The name of the person to be greeted.
 */
class FelicaLib(val tag: Tag) {
    val nfcf: NfcF
        @Throws(FelicaLibException::class)
        get() {
            val nfcf = NfcF.get(this.tag)
            nfcf ?: throw FelicaLibException("tag is not Felica")
            return nfcf
        }

    companion object {
        @JvmStatic
        fun haveNfc(context: Context): Boolean {
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            return nfcAdapter != null
        }

        @JvmStatic
        fun isEnableNFC(context: Context): Boolean {
            val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            if (nfcAdapter != null) {
                return nfcAdapter.isEnabled
            } else {
                return false
            }
        }

        @JvmStatic
        @Throws(FelicaLibException::class)
        fun enableForegroundDispatchFelica(context: Context, activity: Activity) {
            try {
                val intent = Intent(context, activity.javaClass)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                val nfcPendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                val intentFilter = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
                val techList = arrayOf(arrayOf(android.nfc.tech.NfcF::class.java.name))

                val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
                nfcAdapter.enableForegroundDispatch(activity, nfcPendingIntent, intentFilter, techList)
            } catch (ex: IllegalStateException) {
                throw FelicaLibException(ex)
            } catch (ex: UnsupportedOperationException) {
                throw FelicaLibException(ex)
            }
        }

        @JvmStatic
        @Throws(FelicaLibException::class)
        fun disableForegroundDispatchFelica(context: Context, activity: Activity) {
            try {
                val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
                nfcAdapter ?: throw FelicaLibException("no NFC adapter")
                nfcAdapter.disableForegroundDispatch(activity)
            } catch (ex: IllegalStateException) {
                throw FelicaLibException(ex)
            } catch (ex: UnsupportedOperationException) {
                throw FelicaLibException(ex)
            }
        }

    }

    @Throws(FelicaLibException::class)
    fun polling(systemCode: Int,
                requestCode: PollingCC.RequestCode,
                timeSlot: PollingCC.TimeSlot): PollingRC {
        try {
            this.nfcf.connect()
            val cmdData = PollingCC(systemCode, requestCode, timeSlot)
            val receiveData = this.nfcf.transceive(cmdData.rawData)
            this.nfcf.close()
            return PollingRC(receiveData)
        } catch (ex: IOException) {
            throw FelicaLibException(ex)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }
}

package com.goroya.kotlinfelicalib

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.app.PendingIntent
import android.content.IntentFilter
import android.app.Activity
import android.nfc.tech.NfcF
import com.goroya.kotlinfelicalib.command.*
import com.goroya.kotlinfelicalib.util.Util
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
    private fun transfer(rawData: ByteArray): ByteArray{
        try {
            this.nfcf.connect()
            val receiveData = this.nfcf.transceive(rawData)
            this.nfcf.close()
            return receiveData
        } catch (ex: IOException) {
            throw FelicaLibException(ex)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }

    @Throws(FelicaLibException::class)
    fun polling(systemCode: Int,
                requestCode: PollingCC.RequestCode,
                timeSlot: PollingCC.TimeSlot): PollingRC {
        try {
            val cmdData = PollingCC(systemCode, requestCode, timeSlot)
            val receiveData = this.transfer(cmdData.rawData)
            return PollingRC(receiveData)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }

    @Throws(FelicaLibException::class)
    fun requestService(idm: ByteArray, nodeCodeList: IntArray): RequestServiceRC {
        try {
            val cmdData = RequestServiceCC(idm, nodeCodeList.size, nodeCodeList)
            val receiveData = this.transfer(cmdData.rawData)
            return RequestServiceRC(receiveData)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }

    @Throws(FelicaLibException::class)
    fun requestResponse(idm: ByteArray): RequestResponseRC {
        try {
            val cmdData = RequestResponseCC(idm)
            val receiveData = this.transfer(cmdData.rawData)
            return RequestResponseRC(receiveData)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }

    @Throws(FelicaLibException::class)
    fun readWithoutEncryption( idm: ByteArray,
                               serviceCodeList: IntArray,
                               blockList: ArrayList<BlockElement>): ReadWithoutEncryptionRC {
        try {
            val cmdData = ReadWithoutEncryptionCC(
                    idm,
                    serviceCodeList.size, serviceCodeList,
                    blockList.size, blockList)
            val receiveData = this.transfer(cmdData.rawData)
            return ReadWithoutEncryptionRC(receiveData)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }

    @Throws(FelicaLibException::class)
    fun requestSystemCode(idm: ByteArray): RequestSystemCodeRC {
        try {
            val cmdData = RequestSystemCodeCC(idm)
            val receiveData = this.transfer(cmdData.rawData)
            return RequestSystemCodeRC(receiveData)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }

    /*
    @Throws(FelicaLibException::class)
    fun requestSpecificationVersion(idm: ByteArray): RequestSpecificationVersionRC {
        try {
            val cmdData = RequestSpecificationVersionCC(idm)
            val receiveData = this.transfer(cmdData.rawData)
            println(Util.getByte2HexString(receiveData))
            return RequestSpecificationVersionRC(receiveData)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }
    */

    /*
    @Throws(FelicaLibException::class)
    fun resetMode(idm: ByteArray): ResetModeRC {
        try {
            val cmdData = ResetModeCC(idm)
            val receiveData = this.transfer(cmdData.rawData)
            return ResetModeRC(receiveData)
        } catch (ex: FelicaLibException) {
            throw FelicaLibException(ex)
        }
    }
    */
}

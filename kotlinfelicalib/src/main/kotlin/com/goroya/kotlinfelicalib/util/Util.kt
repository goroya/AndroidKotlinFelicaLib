package com.goroya.kotlinfelicalib.util

class Util {
    companion object {
        @JvmStatic
        fun getByte2HexString(data: ByteArray): String {
            var str = ""
            for (byte in data) {
                str += String.format("%02X", byte)

            }
            return str
        }
    }
}
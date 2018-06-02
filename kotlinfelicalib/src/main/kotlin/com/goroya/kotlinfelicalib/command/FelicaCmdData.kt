package com.goroya.kotlinfelicalib.command

import java.io.ByteArrayOutputStream

abstract class FelicaCmdData {
    abstract val cmdCode: Int
    abstract val payload: ByteArray
    val rawData: ByteArray
        get() {
            val streamData = ByteArrayOutputStream()
            streamData.write(0x00)
            streamData.write(this.cmdCode)
            streamData.write(this.payload)
            val data = streamData.toByteArray()
            data[0] = data.size.toByte()
            return data
        }
}

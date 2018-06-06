package com.goroya.kotlinfelicalib.command

import android.util.Log
import com.goroya.kotlinfelicalib.util.Util
import java.io.ByteArrayOutputStream

class CommandCode {
    companion object {
        const val Polling = 0x00
        const val RequestService = 0x02
        const val RequestResponse = 0x04
        const val ReadWithoutEncryption = 0x06
        const val WriteWithoutEncryption = 0x08
        const val SearchServiceCode = 0x0A
        const val RequestSystemCode = 0x0C
        const val Authentication1 = 0x10
        const val Authentication2 = 0x12  //
        const val Read = 0x14  //
        const val Write = 0x16  //
        const val RequestServiceV2 = 0x32
        const val GetSystemStatus = 0x38
        const val RequestSpecificationVersion = 0x3C
        const val ResetMode = 0x3E
        const val Authentication1V2 = 0x40
        const val Authentication2V2 = 0x42
        const val ReadV2 = 0x44  //
        const val WriteV2 = 0x46  //
        const val UpdateRandomID = 0x4C
    }
}

class ResponseCode {
    companion object {
        const val Polling = 0x01
        const val RequestService = 0x03
        const val RequestResponse = 0x05
        const val ReadWithoutEncryption = 0x07
        const val WriteWithoutEncryption = 0x09
        const val SearchServiceCode = 0x0B
        const val RequestSystemCode = 0x0D
        const val Authentication1 = 0x11
        const val Authentication2 = 0x13
        const val Read = 0x15
        const val Write = 0x17
        const val RequestServiceV2 = 0x33
        const val GetSystemStatus = 0x39
        const val RequestSpecificationVersion = 0x3D
        const val ResetMode = 0x3F
        const val Authentication1V2 = 0x41
        const val Authentication2V2 = 0x43
        const val ReadV2 = 0x45
        const val WriteV2 = 0x47
        const val UpdateRandomID = 0x4D
    }
}

class PollingCC(
        private val systemCode: Int,
        private val requestCode: RequestCode,
        private val timeSlot: TimeSlot) : FelicaCmdData() {

    override val cmdCode: Int
        get() = CommandCode.Polling

    override val payload: ByteArray
        get() {
            return byteArrayOf(
                    ((systemCode shr 8) and 0xFF).toByte(),
                    (systemCode and 0xFF).toByte(),
                    requestCode.value,
                    timeSlot.value)
        }

    enum class RequestCode(val value: Byte) {
        NoRequest(0x00),
        SystemCodeRequest(0x01),
        CommunicationPerformanceRequest(0x02),
    }

    enum class TimeSlot(val value: Byte) {
        MaximumNumberOfSlot1(0x00),
        MaximumNumberOfSlot2(0x01),
        MaximumNumberOfSlot4(0x03),
        MaximumNumberOfSlot8(0x07),
        MaximumNumberOfSlot16(0x0F),
    }
}

class PollingRC(val data: ByteArray) {
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val pmm: ByteArray
    val requestData: Int

    init {
        if (data.isEmpty()) {
            this.length = 0
        } else {
            this.length = data[0].toInt()
        }
        if (data.size < 2) {
            this.responseCode = 0
        } else {
            this.responseCode = data[1].toInt()
        }
        if (data.size < 10) {
            this.idm = byteArrayOf()
        } else {
            this.idm = data.slice(2..9).toByteArray()
        }
        if (data.size < 18) {
            this.pmm = byteArrayOf()
        } else {
            this.pmm = data.slice(10..17).toByteArray()
        }
        if (data.size < 20) {
            this.requestData = 0
        } else {
            this.requestData = (data[18].toInt() and 0xFF shl 8) or (data[19].toInt() and 0xFF)

        }

    }

    override fun toString(): String {
        return """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode}
            idm: ${Util.getByte2HexString(this.idm)}
            pmm: ${Util.getByte2HexString(this.pmm)}
            requestData: ${this.requestData.toString(16)}
        """.trimIndent()
    }
}

class RequestServiceCC(
        private val idm: ByteArray,
        private val numberofNode: Int,
        private val nodeCodeList: IntArray) : FelicaCmdData() {

    override val cmdCode: Int
        get() = CommandCode.RequestService

    override val payload: ByteArray
        get() {
            val byteStream = ByteArrayOutputStream()
            byteStream.also {
                it.write(this.idm)
                it.write(numberofNode)
                for (nodeCode in nodeCodeList) {
                    it.write(nodeCode and 0xFF)
                    it.write((nodeCode ushr 8) and 0xFF)
                }
            }
            return byteStream.toByteArray()
        }
}

class RequestServiceRC(val data: ByteArray) {
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val numberOfNode: Int
    val nodeKeyVersionList: IntArray

    init {
        if (data.isEmpty()) {
            this.length = 0
        } else {
            this.length = data[0].toInt()
        }
        if (data.size < 2) {
            this.responseCode = 0
        } else {
            this.responseCode = data[1].toInt()
        }
        if (data.size < 10) {
            this.idm = byteArrayOf()
        } else {
            this.idm = data.slice(2..9).toByteArray()
        }
        if (data.size < 11) {
            this.numberOfNode = 0
            this.nodeKeyVersionList = intArrayOf()
        } else {
            this.numberOfNode = data[10].toInt()
            val tempIntArray = mutableListOf<Int>()
            for (index in 0 until this.numberOfNode) {
                if ((10 + index * 2 + 1) < data.size) {
                    val nodeCode = ((data[11 + index * 2 + 1].toInt() and 0xFF) shl 8) or (data[11 + index * 2].toInt() and 0xFF)
                    tempIntArray.add(nodeCode)
                }
            }
            this.nodeKeyVersionList = tempIntArray.toIntArray()
        }

    }

    override fun toString(): String {
        var str = """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode.toString(16)}
            idm: ${Util.getByte2HexString(this.idm)}
            numberOfNode: ${this.numberOfNode}
        """.trimIndent()
        str += "%n".format()
        for ((index, nodeKeyVersion) in this.nodeKeyVersionList.withIndex()) {
            str += "nodeKeyVersionList[$index] :$nodeKeyVersion%n".format()
        }
        return str
    }
}

class RequestResponseCC(private val idm: ByteArray) : FelicaCmdData() {
    override val cmdCode: Int
        get() = CommandCode.RequestResponse

    override val payload: ByteArray
        get() {
            val byteStream = ByteArrayOutputStream()
            byteStream.also {
                it.write(this.idm)
            }
            return byteStream.toByteArray()
        }
}

class RequestResponseRC(val data: ByteArray) {
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val mode: Int

    init {
        if (data.isEmpty()) {
            this.length = 0
        } else {
            this.length = data[0].toInt()
        }
        if (data.size < 2) {
            this.responseCode = 0
        } else {
            this.responseCode = data[1].toInt()
        }
        if (data.size < 10) {
            this.idm = byteArrayOf()
        } else {
            this.idm = data.slice(2..9).toByteArray()
        }
        if (data.size < 11) {
            this.mode = 0xFFFF
        } else {
            this.mode = data[10].toInt()
        }
    }

    override fun toString(): String {
        return """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode.toString(16)}
            idm: ${Util.getByte2HexString(this.idm)}
            mode: ${this.mode}
        """.trimIndent()
    }
}

class BlockElement(
        length: Length = Length.BlockListElementOf2Byte,
        accessMode: AccessMode = AccessMode.ReadOperationOrWriteOperation,
        serviceCodeListOrder: Int,
        blockNumber: Int) {
    enum class Length(val value: Int) {
        BlockListElementOf2Byte(0b1),
        BlockListElementOf3Byte(0b0),
    }

    enum class AccessMode(val value: Int) {
        ReadOperationOrWriteOperation(0b000),
        CashBackAccessToPurseService(0b001),
    }
    val rawData: ByteArray
    init {
        val byteStream = ByteArrayOutputStream()
        byteStream.write(
                (length.value shl 7) or (accessMode.value shl 4) or serviceCodeListOrder)
        if (length == Length.BlockListElementOf2Byte) {
            byteStream.write(blockNumber and 0xFF)
        } else if (length == Length.BlockListElementOf3Byte) {
            byteStream.write(blockNumber and 0xFF)
            byteStream.write((blockNumber ushr 8) and 0xFF)
        }
        this.rawData = byteStream.toByteArray()
    }
}

class ReadWithoutEncryptionCC(
        private val idm: ByteArray,
        private val numberOfService: Int,
        private val serviceCodeList: IntArray,
        private val numberOfBlock: Int,
        private val blockList: ArrayList<BlockElement>
) : FelicaCmdData() {
    override val cmdCode: Int
        get() = CommandCode.ReadWithoutEncryption

    override val payload: ByteArray
        get() {
            val byteStream = ByteArrayOutputStream()
            byteStream.also {
                it.write(this.idm)
                it.write(this.numberOfService)
                for(serviceCode in this.serviceCodeList){
                    it.write(serviceCode and 0xFF)
                    it.write((serviceCode ushr 8) and 0xFF)
                }
                it.write(this.numberOfBlock)
                for(blockElement in this.blockList){
                    for(elem in blockElement.rawData){
                        it.write(elem.toInt())
                    }
                }
            }
            return byteStream.toByteArray()
        }
}

class ReadWithoutEncryptionRC(val data: ByteArray) {
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val statusFlag1: Int
    val statusFlag2: Int
    val numberOfBlock: Int
    val blockData: MutableList<ByteArray> = mutableListOf()

    init {
        if (data.isEmpty()) {
            this.length = 0
        } else {
            this.length = data[0].toInt()
        }
        if (data.size < 2) {
            this.responseCode = 0
        } else {
            this.responseCode = data[1].toInt()
        }
        if (data.size < 10) {
            this.idm = byteArrayOf()
        } else {
            this.idm = data.slice(2..9).toByteArray()
        }
        if (data.size < 11) {
            this.statusFlag1 = 0
        } else {
            this.statusFlag1 = data[10].toInt()
        }
        if (data.size < 12) {
            this.statusFlag2 = 0
        } else {
            this.statusFlag2 = data[11].toInt()
        }
        if (data.size < 13) {
            this.numberOfBlock = 0
        }else{
            this.numberOfBlock = data[12].toInt()
            for(i in 0 until this.numberOfBlock){
                blockData.add( data.slice((13 + i * 16)..(13 + i * 16 + 15)).toByteArray())
            }
        }
    }

    override fun toString(): String {
        var str = """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode.toString(16)}
            idm: ${Util.getByte2HexString(this.idm)}
            statusFlag1: ${this.statusFlag1}
            statusFlag2: ${this.statusFlag2}
            numberOfBlock: ${this.numberOfBlock}
        """.trimIndent()
        str += "%n".format()
        for ((index, blockDataElm) in this.blockData.withIndex()) {
            str += "nodeKeyVersionList[$index] :${Util.getByte2HexString(blockDataElm)}%n".format()
        }
        return str
    }
}

class WriteWithoutEncryptionCC(
        private val idm: ByteArray,
        private val numberofServic: Int,
        private val serviceCodeList: IntArray,
        private val numberofBlock: Int,
        private val blockList: IntArray,
        private val blockData: Array<ByteArray>
) : FelicaCmdData() {
    override val cmdCode: Int
        get() = CommandCode.WriteWithoutEncryption

    override val payload: ByteArray
        get() {
            val byteStream = ByteArrayOutputStream()
            byteStream.also {
                it.write(this.idm)
            }
            return byteStream.toByteArray()
        }
}


class RequestSystemCodeCC(private val idm: ByteArray) : FelicaCmdData() {
    override val cmdCode: Int
        get() = CommandCode.RequestSystemCode

    override val payload: ByteArray
        get() {
            val byteStream = ByteArrayOutputStream()
            byteStream.also {
                it.write(this.idm)
            }
            return byteStream.toByteArray()
        }
}

class RequestSystemCodeRC(val data: ByteArray) {
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val numberOfSystemCode: Int
    val systemCodeList: IntArray

    init {
        if (data.isEmpty()) {
            this.length = 0
        } else {
            this.length = data[0].toInt()
        }
        if (data.size < 2) {
            this.responseCode = 0
        } else {
            this.responseCode = data[1].toInt()
        }
        if (data.size < 10) {
            this.idm = byteArrayOf()
        } else {
            this.idm = data.slice(2..9).toByteArray()
        }
        if (data.size < 11) {
            this.numberOfSystemCode = 0
            this.systemCodeList = intArrayOf()
        } else {
            this.numberOfSystemCode = data[10].toInt()
            this.systemCodeList = IntArray(this.numberOfSystemCode)
            for(i in 0 until this.numberOfSystemCode){
                this.systemCodeList[i] = (data[11 + i * 2].toInt() and 0xFF shl 8) or (data[11 + i * 2 + 1].toInt() and 0xFF)
            }
        }
    }

    override fun toString(): String {
        var str =  """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode.toString(16)}
            idm: ${Util.getByte2HexString(this.idm)}
            numberOfSystemCode: ${this.numberOfSystemCode}
        """.trimIndent()
        str += "%n".format()
        for ((index, blockDataElm) in this.systemCodeList.withIndex()) {
            str += "systemCodeList[$index] :${String.format("%04X", blockDataElm)}%n".format()
        }
        return str
    }
}

/*
class RequestSpecificationVersionCC(private val idm: ByteArray) : FelicaCmdData() {
    override val cmdCode: Int
        get() = CommandCode.RequestSpecificationVersion

    override val payload: ByteArray
        get() {
            val byteStream = ByteArrayOutputStream()
            byteStream.also {
                it.write(this.idm)
            }
            // Reserved
            byteStream.write(0x00)
            byteStream.write(0x00)
            return byteStream.toByteArray()
        }
}

class RequestSpecificationVersionRC(val data: ByteArray) {
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val statusFlag1: Int
    val statusFlag2: Int
    val formatVersion: Int
    val basicVersion: Int
    val numberofOption: Int
    val optionVersionList: IntArray
    init {
        if (data.isEmpty()) {
            this.length = 0
        } else {
            this.length = data[0].toInt()
        }
        if (data.size < 2) {
            this.responseCode = 0
        } else {
            this.responseCode = data[1].toInt()
        }
        if (data.size < 10) {
            this.idm = byteArrayOf()
        } else {
            this.idm = data.slice(2..9).toByteArray()
        }
        if (data.size < 11) {
            this.statusFlag1 = 0xFFFF
        } else {
            this.statusFlag1 = data[10].toInt()
        }
        if (data.size < 12) {
            this.statusFlag2 = 0xFFFF
        } else {
            this.statusFlag2 = data[11].toInt()
        }
        var offset = 0
        if (data.size < 14) {
            this.formatVersion = 0
            this.basicVersion = 0
            this.numberofOption = 0
            this.optionVersionList = intArrayOf()
        }else{
            if(this.statusFlag1 == 0x00){
                offset += 3
                this.formatVersion = data[12].toInt()
                this.basicVersion =  (data[14].toInt() and 0xFF shl 8) or (data[13].toInt() and 0xFF)
            }else{
                this.formatVersion = 0
                this.basicVersion = 0
            }
            this.numberofOption = data[12 + offset].toInt()
            this.optionVersionList = IntArray(this.numberofOption)
            for(i in 0 until this.numberofOption){
                this.optionVersionList[i] =
                        (data[14 + offset + i * 2].toInt() and 0xFF shl 8) or (data[13 + offset + i * 2].toInt() and 0xFF)
            }
        }

    }

    override fun toString(): String {
        var str =  """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode.toString(16)}
            idm: ${Util.getByte2HexString(this.idm)}
            statusFlag1: ${this.statusFlag1}
            statusFlag2: ${this.statusFlag2}
            formatVersion: ${this.formatVersion}
            formatVersion: ${this.basicVersion}
            numberofOption: ${this.numberofOption}
        """.trimIndent()
        str += "%n".format()
        for ((index, optionVersionElm) in this.optionVersionList.withIndex()) {
            str += "optionVersionList[$index] :${String.format("%04X", optionVersionElm)}%n".format()
        }
        return str
    }
}
*/


/*
class ResetModeCC(private val idm: ByteArray) : FelicaCmdData() {
    override val cmdCode: Int
        get() = CommandCode.ResetMode

    override val payload: ByteArray
        get() {
            val byteStream = ByteArrayOutputStream()
            byteStream.also {
                it.write(this.idm)
            }
            // Reserved
            byteStream.write(0x00)
            byteStream.write(0x00)
            return byteStream.toByteArray()
        }
}

class ResetModeRC(val data: ByteArray) {
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val statusFlag1: Int
    val statusFlag2: Int

    init {
        if (data.isEmpty()) {
            this.length = 0
        } else {
            this.length = data[0].toInt()
        }
        if (data.size < 2) {
            this.responseCode = 0
        } else {
            this.responseCode = data[1].toInt()
        }
        if (data.size < 10) {
            this.idm = byteArrayOf()
        } else {
            this.idm = data.slice(2..9).toByteArray()
        }
        if (data.size < 11) {
            this.statusFlag1 = 0
        } else {
            this.statusFlag1 = data[10].toInt()
        }
        if (data.size < 12) {
            this.statusFlag2 = 0
        } else {
            this.statusFlag2 = data[11].toInt()
        }
    }

    override fun toString(): String {
        return """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode.toString(16)}
            idm: ${Util.getByte2HexString(this.idm)}
            statusFlag1: ${this.statusFlag1}
            statusFlag2: ${this.statusFlag2}
        """.trimIndent()
    }
}
*/

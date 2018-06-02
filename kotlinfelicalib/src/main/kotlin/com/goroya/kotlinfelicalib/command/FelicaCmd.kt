package com.goroya.kotlinfelicalib.command

import com.goroya.kotlinfelicalib.util.Util

class CommandCode{
    companion object {
        const val Polling = 0x00
        const val RequestService = 0x02
        const val RequestResponse = 0x04
        const val ReadWithoutEncryption = 0x06
        const val WriteWithoutEncryption = 0x08
        const val SearchServiceCode = 0x0A
        const val RequestSystemCode = 0x0C
        const val Authentication1 = 0x10
        const val Authentication2 = 0x12
        const val Read = 0x14
        const val Write = 0x16
        const val RequestServiceV2 = 0x32
        const val GetSystemStatus = 0x38
        const val RequestSpecificationVersion = 0x3C
        const val ResetMode = 0x3E
        const val Authentication1V2 = 0x40
        const val Authentication2V2 = 0x42
        const val ReadV2 = 0x44
        const val WriteV2 = 0x46
        const val UpdateRandomID = 0x4C
    }
}

class ResponseCode{
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
        private val timeSlot: TimeSlot): FelicaCmdData(){

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

    enum class RequestCode(val value: Byte)  {
        NoRequest(0x00),
        SystemCodeRequest(0x01),
        CommunicationPerformanceRequest(0x02),
    }
    enum class TimeSlot(val value: Byte)  {
        MaximumNumberOfSlot1(0x00),
        MaximumNumberOfSlot2(0x01),
        MaximumNumberOfSlot4(0x03),
        MaximumNumberOfSlot8(0x07),
        MaximumNumberOfSlot16(0x0F),
    }
}

class PollingRC(val data:ByteArray){
    val length: Int
    val responseCode: Int
    val idm: ByteArray
    val pmm:ByteArray
    val requestData: Int
    init {
        if(data.isEmpty()){
            this.length = 0
        }else{
            this.length = data[0].toInt()
        }
        if(data.size < 2){
            this.responseCode = 0
        }else{
            this.responseCode = data[1].toInt()
        }
        if(data.size < 10){
            this.idm = byteArrayOf()
        }else{
            this.idm = data.slice(2..9).toByteArray()
        }
        if(data.size < 18){
            this.pmm = byteArrayOf()
        }else{
            this.pmm = data.slice(10..17).toByteArray()
        }
        if(data.size < 20){
            this.requestData = 0
        }else{
            this.requestData = (data[18].toInt() and 0xFF shl 8) or (data[19].toInt() and 0xFF)

        }

    }

    override fun toString(): String {
        val str = """
            data: ${Util.getByte2HexString(this.data)}
            length: ${this.length}
            responseCode: ${this.responseCode}
            idm: ${Util.getByte2HexString(this.idm)}
            pmm: ${Util.getByte2HexString(this.pmm)}
            requestData: ${this.requestData.toString(16)}
        """
        return str
    }
}




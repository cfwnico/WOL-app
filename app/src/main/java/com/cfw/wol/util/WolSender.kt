package com.cfw.wol.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object WolSender {
    suspend fun sendMagicPacket(macStr: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val macBytes = getMacBytes(macStr) ?: return@withContext false
            val bytes = ByteArray(6 + 16 * macBytes.size)
            
            // First 6 bytes are 0xFF
            for (i in 0..5) {
                bytes[i] = 0xff.toByte()
            }
            // Followed by 16 repetitions of the MAC address
            for (i in 6 until bytes.size step macBytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
            }
            
            val address = InetAddress.getByName("255.255.255.255")
            val packet = DatagramPacket(bytes, bytes.size, address, port)
            
            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.send(packet)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getMacBytes(macStr: String): ByteArray? {
        val hexStr = macStr.replace(":", "").replace("-", "")
        if (hexStr.length != 12) return null
        return try {
            val bytes = ByteArray(6)
            for (i in 0..5) {
                bytes[i] = hexStr.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
            bytes
        } catch (e: Exception) {
            null
        }
    }
}

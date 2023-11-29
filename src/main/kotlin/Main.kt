import java.net.DatagramPacket
import java.net.DatagramSocket

fun main() {
    try {
        val serverSocket = DatagramSocket(2053)
        while (true) {
            val buf = ByteArray(512)
            val packet = DatagramPacket(buf, buf.size)
            serverSocket.receive(packet)
            println("Received data")

            val bufResponse = ByteArray(512)
            val packetResponse = DatagramPacket(bufResponse, bufResponse.size, packet.socketAddress)
            serverSocket.send(packetResponse)
        }
    } catch (e: Exception) {
        println("Exception: ${e.message}")
    }
}
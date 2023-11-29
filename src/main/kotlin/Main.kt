import java.net.DatagramPacket
import java.net.DatagramSocket

fun main() {
    try {
        val serverSocket = DatagramSocket(2053)
        while (true) {
            val buf = ByteArray(512)
            val packet = DatagramPacket(buf, buf.size)
            serverSocket.receive(packet)

            val parsed = packet.data.toDomain()
            println("Parsed $parsed")
            val response = handlePacket(parsed)
            println("Response $parsed")
            val responsePacket = response.toPacket()

            val packetResponse =
                DatagramPacket(responsePacket, responsePacket.size, packet.socketAddress)
            serverSocket.send(packetResponse)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Exception: $e")
    }
}

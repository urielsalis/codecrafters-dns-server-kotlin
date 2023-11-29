import DNSHeader.RCode
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun handlePacket(resolver: String?, parsed: DNSPacket): DNSPacket {
    val rcode = if (parsed.header.opcode == 0.toByte()) {
        RCode.NO_ERROR
    } else {
        RCode.NOT_IMPLEMENTED
    }
    val answers = parsed.questions.flatMap { handleQuestion(resolver, parsed.header, it) }
    return DNSPacket(
        header = parsed.header.copy(qr = true, rcode = rcode, ancount = answers.size.toShort()),
        questions = parsed.questions,
        answers = answers,
        authorities = listOf(),
        additionals = listOf()
    )
}

fun handleQuestion(resolver: String?, header: DNSHeader, question: DNSQuestion): List<DNSRecord> {
    if (resolver != null && header.rd) {
        val parts = resolver.split(":")
        val host = parts[0]
        val port = parts.getOrElse(1) { "53" }.toInt()
        val socket = DatagramSocket()
        val buf = DNSPacket(
            header.copy(qdcount = 1), listOf(question), listOf(), listOf(), listOf()
        ).toPacket()
        val resolverPacketDatagram =
            DatagramPacket(buf, buf.size, InetAddress.getByName(host), port)
        socket.send(resolverPacketDatagram)
        val responseBuf = ByteArray(512)
        val responsePacket = DatagramPacket(responseBuf, responseBuf.size)
        socket.receive(responsePacket)
        val parsed = responsePacket.data.toDomain()
        if (parsed.answers.isNotEmpty()) {
            return parsed.answers
        }
    }
    return listOf(
        DNSRecord(
            name = question.name,
            type = DNSType.A,
            klass = DNSClass.IN,
            ttl = 60,
            data = byteArrayOf(127, 0, 0, 1)
        )
    )
}

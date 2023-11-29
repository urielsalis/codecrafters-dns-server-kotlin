import DNSHeader.RCode

fun handlePacket(parsed: DNSPacket): DNSPacket {
    val rcode = if (parsed.header.opcode == 0.toByte()) {
        RCode.NO_ERROR
    } else {
        RCode.NOT_IMPLEMENTED
    }
    return DNSPacket(
        header = parsed.header.copy(qr = true, rcode = rcode, ancount = 1),
        questions = parsed.questions,
        answers = parsed.questions.flatMap { handleQuestion(parsed.header, it) },
        authorities = listOf(),
        additionals = listOf()
    )
}

fun handleQuestion(header: DNSHeader, question: DNSQuestion): List<DNSRecord> {
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

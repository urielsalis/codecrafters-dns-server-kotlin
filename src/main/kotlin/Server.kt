fun handlePacket(parsed: DNSPacket): DNSPacket {
    return DNSPacket(
        header = parsed.header.copy(qr = true, rcode = DNSHeader.RCode.NO_ERROR, ancount = 1),
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

fun handlePacket(parsed: DNSPacket): DNSPacket {
    return DNSPacket(
        header = parsed.header.copy(qr = true, rcode = DNSHeader.RCode.NO_ERROR),
        questions = parsed.questions,
        answers = listOf(),
        authorities = listOf(),
        additionals = listOf()
    )
}
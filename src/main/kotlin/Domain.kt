data class DNSPacket(
    val header: DNSHeader,
    val questions: List<DNSQuestion>,
    val answers: List<DNSRecord>,
    val authorities: List<DNSRecord>,
    val additionals: List<DNSRecord>,
)

typealias HalfByte = Byte

data class DNSHeader(
    val id: Short,  // Packet Identifier
    val qr: Boolean,  // Query/Response Indicator
    val opcode: HalfByte,  // Operation Code
    val aa: Boolean,  // Authoritative Answer
    val tc: Boolean,  // Truncation Flag
    val rd: Boolean,  // Recursion Desired
    val ra: Boolean,  // Recursion Available
    val z: HalfByte,  // Reserved for future use, currently used by DNSSEC, only 3 bits
    val rcode: RCode,  // Response Code, only 4 bits
    val qdcount: Short,  // Question Count
    val ancount: Short,  // Answer Record Count
    val nscount: Short,  // Authority Record Count
    val arcount: Short
) {
    fun forResolving(): DNSHeader = this.copy(
        qr = false, rd = true, ra = false, qdcount = 1, ancount = 0, nscount = 0, arcount = 0
    )

    enum class RCode(val value: HalfByte) {
        UNKNOWN(-1), NO_ERROR(0), FORMAT_ERROR(1), SERVER_FAILURE(2), NAME_ERROR(3), NOT_IMPLEMENTED(
            4
        ),
        REFUSED(5);
    }
}


data class DNSQuestion(val name: List<String>, val type: DNSType, val klass: DNSClass)

data class DNSRecord(
    val name: List<String>,
    val type: DNSType,
    val klass: DNSClass,
    val ttl: Int,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DNSRecord) return false

        if (name != other.name) return false
        if (type != other.type) return false
        if (klass != other.klass) return false
        if (ttl != other.ttl) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + klass.hashCode()
        result = 31 * result + ttl
        result = 31 * result + data.contentHashCode()
        return result
    }
}

enum class DNSType(val value: Short) {
    A(1), NS(2), MD(3), MF(4), CNAME(5), SOA(6), MB(7), MG(8), MR(9), NULL(10), WKS(11), PTR(12), HINFO(
        13
    ),
    MINFO(14), MX(15), TXT(16), OPT(41), RRSIG(46), ANY(255)
}


enum class DNSClass(val value: Short) {
    IN(1), CS(2), CH(3), HS(4), ANY(255)
}


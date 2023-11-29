import DNSHeader.RCode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

fun ByteArray.toDomain(): DNSPacket {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
    val header = parseHeader(buffer)
    return DNSPacket(header, listOf(), listOf(), listOf(), listOf())
}

fun DNSPacket.toPacket(): ByteArray {
    val buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN)
    writeHeader(this.header, buffer)
    return buffer.rewind().array()
}

fun parseHeader(buffer: ByteBuffer): DNSHeader {
    val id = buffer.short
    val flags1 = buffer.get().asBitSet()
    val flags2 = buffer.get().asBitSet()
    val qr = flags1[7]
    val opcode = flags1[3, 7].valueOrZero()
    val aa = flags1[2]
    val tc = flags1[1]
    val rd = flags1[0]
    val ra = flags2[7]
    val z = flags2[4, 7].valueOrZero()
    val rcode = flags2[0, 4].valueOrZero()
    val qdCount = buffer.short
    val anCount = buffer.short
    val nsCount = buffer.short
    val arCount = buffer.short

    return DNSHeader(
        id = id,
        qr = qr,
        opcode = opcode,
        aa = aa,
        tc = tc,
        rd = rd,
        ra = ra,
        z = z,
        rcode = RCode.entries.first { it.value == rcode },
        qdcount = qdCount,
        ancount = anCount,
        nscount = nsCount,
        arcount = arCount
    )
}

fun writeHeader(header: DNSHeader, buffer: ByteBuffer) {
    buffer.putShort(header.id)
    val flags1 = BitSet(8)
    val flags2 = BitSet(8)
    val opcode = header.opcode.asBitSet()
    val z = header.z.asBitSet()
    val rcode = header.rcode.value.asBitSet()
    flags1[7] = header.qr
    flags1[6] = opcode[3]
    flags1[5] = opcode[2]
    flags1[4] = opcode[1]
    flags1[3] = opcode[0]
    flags1[2] = header.aa
    flags1[1] = header.tc
    flags1[0] = header.rd
    flags2[7] = header.ra
    flags2[6] = z[2]
    flags2[5] = z[1]
    flags2[4] = z[10]
    flags2[3] = rcode[3]
    flags2[2] = rcode[2]
    flags2[1] = rcode[1]
    flags2[0] = rcode[0]
    buffer.put(flags1.valueOrZero())
    buffer.put(flags2.valueOrZero())
    buffer.putShort(header.qdcount)
    buffer.putShort(header.ancount)
    buffer.putShort(header.nscount)
    buffer.putShort(header.arcount)
}

private fun BitSet.valueOrZero(): HalfByte = this.toByteArray().firstOrNull() ?: 0
private fun Byte.asBitSet() = BitSet.valueOf(byteArrayOf(this))

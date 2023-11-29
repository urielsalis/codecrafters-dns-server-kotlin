import DNSHeader.RCode
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.and

fun ByteArray.toDomain(): DNSPacket {
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.BIG_ENDIAN)
    val header = parseHeader(buffer)
    val questions = (0 until header.qdcount).map { parseQuestion(buffer) }
    val answers = (0 until header.ancount).map { parseRecord(buffer) }
    return DNSPacket(header, questions, answers, listOf(), listOf())
}

fun parseQuestion(buffer: ByteBuffer): DNSQuestion {
    val name = parseName(buffer)
    val type = buffer.short
    val klass = buffer.short
    return DNSQuestion(name,
        DNSType.entries.first { it.value == type },
        DNSClass.entries.first { it.value == klass })
}

fun parseRecord(buffer: ByteBuffer): DNSRecord {
    val name = parseName(buffer)
    val type = buffer.short
    val klass = buffer.short
    val ttl = buffer.int
    val rdLength = buffer.short
    val data = ByteArray(rdLength.toInt())
    buffer.get(data)
    return DNSRecord(name,
        DNSType.entries.first { it.value == type },
        DNSClass.entries.first { it.value == klass },
        ttl,
        data
    )
}

fun parseName(buffer: ByteBuffer): List<String> {
    val labels = mutableListOf<String>()
    var length = buffer.get()
    while (length != 0x00.toByte()) {

        if ((length.toInt() and 0b11000000) > 0) {
            // Pointer
            val nextByte = buffer.get()
            val offset = ((length and 0b00111111.toByte()).toInt() shl 8) + nextByte
            labels.addAll(parseName(buffer.duplicate().rewind().position(offset)))
            return labels
        }
        val bytes = ByteArray(length.toInt())
        buffer.get(bytes)
        labels.add(String(bytes))
        length = buffer.get()
    }
    return labels
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

fun DNSPacket.toPacket(): ByteArray {
    val buffer = ByteBuffer.allocate(512).order(ByteOrder.BIG_ENDIAN)
    writeHeader(this.header, buffer)
    this.questions.forEach { writeQuestion(it, buffer) }
    this.answers.forEach { writeRecord(it, buffer) }
    return buffer.rewind().array()
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

fun writeQuestion(question: DNSQuestion, buffer: ByteBuffer) {
    writeName(question.name, buffer)
    buffer.putShort(question.type.value)
    buffer.putShort(question.klass.value)
}

fun writeRecord(record: DNSRecord, buffer: ByteBuffer) {
    writeName(record.name, buffer)
    buffer.putShort(record.type.value)
    buffer.putShort(record.klass.value)
    buffer.putInt(record.ttl)
    buffer.putShort(record.data.size.toShort())
    buffer.put(record.data)
}

private fun writeName(name: List<String>, buffer: ByteBuffer) {
    name.forEach { label ->
        buffer.put(label.length.toByte())
        buffer.put(label.toByteArray())
    }
    buffer.put(0x00.toByte())
}

private fun BitSet.valueOrZero(): HalfByte = this.toByteArray().firstOrNull() ?: 0
private fun Byte.asBitSet() = BitSet.valueOf(byteArrayOf(this))

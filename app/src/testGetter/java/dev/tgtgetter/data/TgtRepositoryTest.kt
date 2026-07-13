package dev.tgtgetter.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Base64

class TgtRepositoryTest {
    @Test
    fun parseTgt_extractsOnlyTgt() {
        val tgt = "TGT-example-token"
        val unrelatedValue = "abc15000001xyz"
        val serialized = byteArrayOf(0x74, 0x00, tgt.length.toByte()) + tgt.toByteArray() +
            byteArrayOf(0x74, 0x00, unrelatedValue.length.toByte()) + unrelatedValue.toByteArray()
        val encoded = Base64.getEncoder().encodeToString(serialized)
        val xml = "<map><string name=\"Key_EDUserNew\">$encoded</string></map>"

        assertEquals(tgt, TgtRepository.parseTgt(xml))
    }
}

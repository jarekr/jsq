import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jarekr.jsq.JsqHeader;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsqHeaderTest {

    @Test
    public void createJsqHeader() {

        Map<String, Byte> prefixToChar = new HashMap<>();
        prefixToChar.put("0", (byte)'a');
        prefixToChar.put("10", (byte)'b');
        prefixToChar.put("110", (byte)'c');
        prefixToChar.put("101", (byte)'d');
        prefixToChar.put("111", (byte)'e');
        JsqHeader header = new JsqHeader(prefixToChar, 1024, 0);

        assertNotNull(header);

        byte[] bytes = header.generate();

        assertNotNull(bytes);
        assertEquals(-52, bytes[0]);
        assertEquals(-35, bytes[1]);
        assertEquals(105, bytes[2]);
        assertEquals(66, bytes[3]);
        assertEquals(5, bytes[7]);
    }

    @Test
    public void parseJsqHeader() {

        byte[] headerBytes = {
                (byte)0xcc, (byte)0xdd, 0x69, 0x42, 0x00, 0x00, 0x00, 0x06,
                0x00, 0x00, 0x01, 0x2d, 0x03, 0x63, 0x00, 0x00, 0x00, 0x06,
                0x03, 0x0a, 0x00, 0x00, 0x00, 0x00, 0x02, 0x61, 0x00, 0x00,
                0x00, 0x01, 0x03, 0x64, 0x00, 0x00, 0x00, 0x07, 0x03, 0x65,
                0x00, 0x00, 0x00, 0x01, 0x02, 0x62, 0x00, 0x00, 0x00, 0x02
        };

        JsqHeader header = JsqHeader.parseHeader(headerBytes);
        assertNotNull(header);
        assertEquals(48, header.getHeaderByteCount());
        assertEquals((byte)99, header.prefixToChar().get("110"));
        assertEquals((byte)100, header.prefixToChar().get("111"));

    }
}

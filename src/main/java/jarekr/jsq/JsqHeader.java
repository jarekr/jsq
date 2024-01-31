package jarekr.jsq;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JsqHeader {

    private int bitCount = 0;

    private int headerByteCount = 0;

    private Map<String, Byte> prefixToChar;

    public JsqHeader(Map<String, Byte> prefixToChar, int bitCount, int position) {
        this.bitCount = bitCount;
        this.prefixToChar = prefixToChar;
        this.headerByteCount = position;
    }

    public static final byte[] JSQ_HEADER = new byte[]{ (byte)0xcc, (byte)0xdd, (byte)0x69, (byte)0x42 };

    public byte[] generate() {

        ByteBuffer buffer = ByteBuffer.allocate((JSQ_HEADER.length + 8) + (6 * prefixToChar.size()));
        buffer.put(JSQ_HEADER);  // jsq file type
        buffer.putInt(prefixToChar.size()); //count of transliterations / replacements
        buffer.putInt(bitCount);

        for (var e: prefixToChar.entrySet()) {
            Byte khar = e.getValue();
            String prefix = e.getKey();
            if (khar == null) {
                khar = 0;
            }

            var expectedBits = prefix.length();

            buffer.put((byte)expectedBits); // + 1 byte
            buffer.put((byte)khar);  // + 1 byte
            buffer.putInt(Integer.parseInt(prefix, 2));
        }
        return buffer.array();
    }

    public static JsqHeader parseHeader(byte[] bytes) {

        if (bytes.length < JSQ_HEADER.length + 8) {
            return null;
        }

        var result = Arrays.compare(JSQ_HEADER, 0, JSQ_HEADER.length-1,
                bytes, 0, JSQ_HEADER.length -1);

        if (result != 0) {
            return null;
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(JSQ_HEADER.length);
        var items = buffer.getInt();
        var bitCount = buffer.getInt();
        Map<String, Byte> prefixToChar = new HashMap<>();
        for (int ii = 0; ii < items; ++ii) {
            var bitlen = buffer.get();
            var key = buffer.get();

            int rawint = buffer.getInt();
            String prefix = Integer.toBinaryString(rawint);
            if (prefix.length() < bitlen) {
                for (int jk = 0; jk <= bitlen - prefix.length(); ++jk) {
                    prefix = "0" + prefix;
                }
            }
            prefixToChar.put(prefix, key);
        }

        return new JsqHeader(prefixToChar, bitCount, buffer.position());
    }

    public Map<String , Byte> prefixToChar() {
        return this.prefixToChar;
    }

    public int getHeaderByteCount() {
        return headerByteCount;
    }

    public int getBitCount() {
        return this.bitCount;
    }
}

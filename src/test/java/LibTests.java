import jarekr.jsq.BitVector;
import jarekr.jsq.HNode;
import jarekr.jsq.JsqHeader;
import org.junit.jupiter.api.Test;

import static jarekr.jsq.Lib.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LibTests {

    public static final String INPUT2 = "aabbabbbaaabcccaabbeeeeaabbeeeeaaaeeabbbeeaaaaeaabbcc";

    public static byte[] generateInput1() {
        return "abbccceeee".getBytes(StandardCharsets.UTF_8);
    }
    public static byte[] generateInput2() {
        return INPUT2.getBytes(StandardCharsets.UTF_8);
    }
    public static HNode generateTree1(byte[] input) {
        Map<Byte, Integer> result = countBytes(input);
        assertNotNull(result);
        return buildTree(result);
    }

    @Test
    public void countBytesTest1() {
        byte[] bytes = generateInput1();

        Map<Byte, Integer> result = countBytes(bytes);

        assertNotNull(result);

        assertFalse(result.isEmpty());
        assertEquals(1, result.get((byte)'a'));
        assertEquals(2, result.get((byte)'b'));
        assertEquals(3, result.get((byte)'c'));
        assertEquals(4, result.get((byte)'e'));
    }

    @Test
    public void treeTest1() {
        HNode tree = buildTree(countBytes(generateInput1()));
        assertNotNull(tree);
        assertEquals(10, tree.freq);
        assertNotNull(tree.left);
        assertEquals(4, tree.left.freq);

        Map<Byte, HNode> byByte = derivePrefixes(tree);
        assertNotNull(byByte);
        assertEquals(4, byByte.get((byte)'e').freq);
        assertEquals(1, byByte.get((byte)'a').freq);
        assertEquals(2, byByte.get((byte)'b').freq);
        assertEquals(3, byByte.get((byte)'c').freq);
        assertFalse(byByte.containsKey((byte)' '));
        assertFalse(byByte.containsKey((byte)'d'));
    }

    @Test
    public void generateHeaderTest() {
        HNode root = generateTree1(generateInput1());
        Map<Byte, HNode> byChar = derivePrefixes(root);
        JsqHeader header = new JsqHeader(toByPrefix(byChar), 0, 0);
        assertNotNull(header);
        byte[] byteHeader = header.generate();
        assertEquals(36, byteHeader.length);
    }

    @Test
    public void parseHeaderTest() {
        HNode root = generateTree1(generateInput1());
        Map<Byte, HNode> byChar = derivePrefixes(root);
        JsqHeader jheader = new JsqHeader(toByPrefix(byChar), 0, 0);
        assertNotNull(jheader);
        byte[] header = jheader.generate();
        var foo = JsqHeader.parseHeader(header);
        assertNotNull(foo);
    }

    @Test
    public void compressionTest1() {
        byte[] uncompressed = generateInput1();
        HNode root = generateTree1(generateInput1());
        Map<Byte, HNode> byChar = derivePrefixes(root);
        BitVector compressed = compress(uncompressed, byChar);
        assertNotNull(compressed);
    }

    @Test
    public void deCompressionTest1() {
        byte[] uncompressed = generateInput1();
        HNode root = generateTree1(generateInput1());
        Map<Byte, HNode> byChar = derivePrefixes(root);

        BitVector compressed = compress(uncompressed, byChar);
        assertNotNull(compressed);
        List<Byte> decompressed = decompress(compressed, toByPrefix(byChar));
        assertNotNull(decompressed);
        assertFalse(decompressed.isEmpty());
    }

    @Test
    public void deCompressionTest2() {
        byte[] uncompressed = generateInput2();
        HNode root = generateTree1(generateInput1());
        Map<Byte, HNode> byChar = derivePrefixes(root);
        BitVector compressed = compress(uncompressed, byChar);
        assertNotNull(compressed);
        List<Byte> decompressed = decompress(compressed, toByPrefix(byChar));
        assertNotNull(decompressed);
        assertFalse(decompressed.isEmpty());

        byte[] bytes = new byte[decompressed.size()];
        int i = 0;
        for (Byte b: decompressed) {
            bytes[i] = b;
            ++i;
        }
        assertEquals(INPUT2, StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)).toString());
    }
}

package jarekr.jsq;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Lib {

    public static String bitStringFromInt(int value, int bitlen) {
        StringBuilder sb = new StringBuilder();
        return "";
    }

    public static Map<Byte, HNode> derivePrefixes(HNode root) {
        List<HNode> nodes = new ArrayList<>();
        root.prefix = "";
        nodes.add(root);

        Map<Byte, HNode> byChar = new HashMap<>();
        while (!nodes.isEmpty()) {
            HNode n = nodes.remove(0);
            if (n.khar != null) {
                byChar.put(n.khar, n);
            }
            if (n.left != null) {
                n.left.prefix = n.prefix + "0";
                nodes.add(n.left);
            }
            if (n.right != null) {
                n.right.prefix = n.prefix + "1";
                nodes.add(n.right);
            }
        }

        return byChar;
    }

    public static Map<String, Byte> toByPrefix(Map<Byte, HNode> byChar) {
        var foo = byChar.values().stream().map( v -> { return v.prefix + "::" + String.valueOf(Integer.parseInt(v.prefix, 2)); }).sorted().toList();
        return byChar.entrySet().stream().collect(Collectors
                .toMap( k -> k.getValue().prefix, Map.Entry::getKey));
    }

    public static Map<Byte, Integer> countBytes(byte[] bytes) {
        Map<Byte, Integer> map = new HashMap<>();
        for (byte bb: bytes) {
            map.put(bb, map.getOrDefault(bb, 0) + 1);
        }
        return map;
    }

    public static HNode buildTree(Map<Byte, Integer> freqmap) {
        PriorityQueue<HNode> queue = new PriorityQueue<>();
        for (var e : freqmap.entrySet()) {
            queue.add(new HNode(e.getKey(), e.getValue()));
        }

        TreeMap<Integer, List<HNode>> htreemap = new TreeMap<Integer, List<HNode>>();

        HNode lasthead = null;
        while (queue.size() > 1) {
            HNode hn1 = queue.poll();
            HNode hn2 = queue.poll();
            HNode nn = new HNode(hn1.freq + hn2.freq);
            if (hn1.freq < hn2.freq) {
                nn.left = hn1;
                nn.right = hn2;
            } else {
                nn.left = hn2;
                nn.right = hn1;
            }
            lasthead = nn;
            queue.add(nn);
        }
        HNode head = queue.poll();
        if (head == null) {
            head = lasthead;
        }
        return head;
    }

    public static BitVector compress(byte[] uncompressed, Map<Byte, HNode> byChar) {

        BitVector squished = new BitVector();
        //for (int i = uncompressed.length - 1; i >= 0; --i) {
        //    byte b = uncompressed[i];
        for (byte b: uncompressed) {
            HNode replacement = byChar.get(b);
            for (char c: replacement.prefix.toCharArray()) {
                squished.add(c == '1');
            }
        }
        return squished;
    }

    public static List<Byte> decompress(BitVector compressed, Map<String, Byte> byPrefix) {
        ArrayList<Byte> buffer = new ArrayList<>();

        StringBuilder currentKey = new StringBuilder();

        var count = compressed.getBitCount();
        for (int i = 0; i < count; ++i) {

            if (compressed.get(i)) {
                currentKey.append('1');
            } else {
                currentKey.append('0');
            }
            Byte replacement = byPrefix.get(currentKey.toString());
            if (replacement != null) {
                currentKey.setLength(0);
                buffer.add(replacement);
            }
        }
        return buffer;
    }

    public static byte[] writeOutToByteArray(byte[] header, BitVector compressed) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(header);
        long bitsWritten = 0;
        long bitsToWrite = compressed.getBitCount();
        for (int k: compressed.toIntList()) {
            long bitsLeft = bitsToWrite - bitsWritten;
            if (bitsLeft <= 0) {
                break;
            }
            dos.writeInt(k);
            bitsWritten += 32;
        }
        dos.close();
        return baos.toByteArray();
    }

    public static void writeOutToFile(String newFileName, byte[] header, BitVector compressed) throws IOException {
        FileOutputStream output = new FileOutputStream(newFileName);
        output.write(writeOutToByteArray(header,compressed));
        output.close();
    }

    public static BitVector readFromByteArray(byte[] bytes, JsqHeader header) throws IOException {
        int headerByteCount = header.getHeaderByteCount();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes, headerByteCount, bytes.length - headerByteCount);
        DataInputStream dis = new DataInputStream(bais);

        List<Integer> contents = new ArrayList<>();
        while (dis.available() > 3) {
            contents.add(dis.readInt());
        }

        if (dis.available() > 0) {
            Integer lastInt = Integer.valueOf(0);
            while (dis.available() > 0) {
                byte b = dis.readByte();
                lastInt += b << 4;
            }
            contents.add(lastInt);
        }

        return BitVector.wrap(contents, header.getBitCount());
    }
}

package jarekr.jsq;

public class HNode implements Comparable<HNode> {

    public HNode(byte khar, int freq) {
        this.khar = khar;
        this.freq = freq;
    }

    public HNode(int freq){
        this.khar = null;
        this.freq = freq;
    }

    public int freq;
    public HNode left = null;
    public HNode right = null;
    public Byte khar = null;
    public String prefix = null;

    public String kharToString() {
        if (khar == null) {
            return null;
        }
        char[] buf = new char[1];
        buf[0] = (char) khar.byteValue();
        var ascii = (khar < 0 ) ? "?" : String.valueOf(buf);
        if (khar <= 15 && khar >= 9) {
            ascii = String.format("%x", khar);
        }
        return ascii;
    }

    @Override
    public int compareTo(HNode hNode) {
        return Integer.compare(this.freq, hNode.freq);
    }
}

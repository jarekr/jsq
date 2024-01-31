package jarekr.jsq;

import java.util.ArrayList;
import java.util.List;

public class BitVector {

    private List<Integer> backing;
    private long bitCount = 0;

    public BitVector() {
        this.backing = new ArrayList<>();
        this.bitCount = 0;
    }

    public long getBitCount() { return bitCount; }

    public BitVector add(boolean on) {
        this.set(this.bitCount, on);
        return this;
    }

    public BitVector set(long idx, boolean on) {
        int intOffset = (int) (idx / 32);
        int bitOffset = (int) (idx % 32);
        if (this.backing.size() < intOffset + 1){
            for (int i = 0; i <= intOffset; ++i) {
                backing.add(0);
            }
        }

        if (this.bitCount <= idx) {
            this.bitCount = idx + 1;
        }

        if (on) {
            backing.set(intOffset, backing.get(intOffset) + (1 << bitOffset));
        }
        return this;
    }

    public boolean get(long idx) {
        int intidx = (int) (idx / 32);
        int bitoffset = (int) (idx % 32);
        int value = backing.get(intidx);

        return (value >>> bitoffset & 1) == 1;
    }

    public static BitVector wrap(Integer item, int bitCount) {
        BitVector bv = new BitVector();
        bv.backing.add(item);
        bv.bitCount = bitCount;
        return bv;
    }

    public static BitVector wrap(List<Integer> input, int bitCount) {
        BitVector bv = new BitVector();
        bv.backing = input;
        bv.bitCount = bitCount;
        return bv;
    }
    public List<Integer> toIntList() {
        return backing.stream().toList();
    }

    public List<Integer> reversed() {
        List<Integer> tmp = this.toIntList().reversed();
        return tmp.stream().map(Integer::reverse).toList();
    }
}

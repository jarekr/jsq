import jarekr.jsq.BitVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BitVectorTests {

    @Test
    public void testSetArbitrary() {

        BitVector v = new BitVector();
        v.set(0, true);
        v.set(2, true);
        v.set(3, true);
        assertEquals(4, v.getBitCount());
    }

    @Test
    public void testGetByteArray() {
        BitVector bv = new BitVector();
        //bv.add(true).add(false).add(true).add(false);
        for (int i = 0; i < 16; ++i){
            bv.add(true);
        }
        List<Integer> arr = bv.toIntList();
        assertNotNull(arr);
        assertFalse(arr.isEmpty());
    }
}

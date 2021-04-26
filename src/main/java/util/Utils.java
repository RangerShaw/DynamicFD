package util;

import algorithm.hittingSet.fdConnectors.FdConnector;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

public class Utils {

    /**
     * Compare two BitSets from the lowest bit
     */
    public static Comparator<BitSet> BitsetComparator() {
        return (a, b) -> {
            if (a.equals(b)) return 0;

            BitSet xor = (BitSet) a.clone();
            xor.xor(b);
            int lowestDiff = xor.nextSetBit(0);

            if (lowestDiff == -1) return 0;
            return b.get(lowestDiff) ? 1 : -1;
        };
    }

    public static int boolArrayToInt(boolean[] bools) {
        int x = 0;
        for (boolean b : bools)
            x = (x << 1) | (b ? 1 : 0);
        return x;
    }

    public static BitSet boolArrayToBitSet(boolean[] bools) {
        BitSet bs = new BitSet(bools.length);
        for (int i = 0; i < bools.length; i++)
            if (bools[i]) bs.set(i);
        return bs;
    }

    public static BitSet boolArrayToInverseBitSet(boolean[] bools) {
        BitSet bs = new BitSet(bools.length);
        for (int i = 0; i < bools.length; i++)
            if (!bools[i]) bs.set(i);
        return bs;
    }

    public static int bitsetToInt(int nAttributes, BitSet bs) {
        int x = 0;
        for (int i = 0; i < nAttributes; i++)
            x = (x << 1) | (bs.get(i) ? 1 : 0);
        return x;
    }

    public static int bitsetToInverseInt(int nAttributes, BitSet bs) {
        int x = 0;
        for (int i = 0; i < nAttributes; i++)
            x = (x << 1) | (bs.get(i) ? 0 : 1);
        return x;
    }


}

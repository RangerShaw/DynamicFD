package util;

import java.util.ArrayList;
import java.util.List;

public class IntSet {

    public static List<Integer> setBits(int n) {
        List<Integer> res = new ArrayList<>();
        int pos = 0;
        while (n > 0) {
            if ((n & 1) != 0) res.add(pos);
            pos++;
            n >>= 1;
        }
        return res;
    }
}

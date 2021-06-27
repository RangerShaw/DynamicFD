package algorithm.hittingSet.AMMCS;

import algorithm.hittingSet.NumSet;
import util.Utils;

import javax.swing.text.Utilities;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class Subset {

    public long set;

    public long count;

    public Subset(long set) {
        this.set = set;
        this.count = 1;
    }

    public Subset(long set, long count) {
        this.set = set;
        this.count = count;
    }

    static public List<Subset> bitSetMapToSubsets(int nEle, Map<BitSet, Long> diffMap) {
        List<Subset> res = new ArrayList<>();
        for (Map.Entry<BitSet, Long> e : diffMap.entrySet())
            res.add(new Subset(Utils.bitsetToLong(nEle, e.getKey()), e.getValue()));
        return res;
    }

}

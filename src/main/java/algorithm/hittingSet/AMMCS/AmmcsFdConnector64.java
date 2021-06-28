package algorithm.hittingSet.AMMCS;

import algorithm.hittingSet.NumSet;
import util.Utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AmmcsFdConnector64 {

    double threshold;

    long maxError;

    int nElements;

    List<List<BitSet>> minFDs = new ArrayList<>();

    List<Ammcs64> ammcsList = new ArrayList<>();


    public AmmcsFdConnector64() {
    }

    public void initiate(double threshold, int nElements, List<Subset> toCover) {
        this.threshold = threshold;
        this.nElements = nElements;
        maxError = (long) (threshold * toCover.stream().map(sb -> sb.count).reduce(0L, Long::sum));

        List<List<Subset>> subsetParts = genSubsetRhss(toCover);

        for (int rhs = 0; rhs < nElements; rhs++) {
            ammcsList.add(new Ammcs64(nElements));
            ammcsList.get(rhs).initiate(subsetParts.get(rhs),threshold);
            minFDs.add(ammcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.longToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
    }

    List<List<Subset>> genSubsetRhss(List<Subset> subsets) {
        List<List<Subset>> subsetParts = new ArrayList<>(nElements);

        for (int i = 0; i < nElements; i++)
            subsetParts.add(new ArrayList<>(subsets.size() / nElements));

        for (Subset set : subsets) {
            long tmp = set.set;
            int pos = 0;
            while (tmp > 0) {
                if ((tmp & 1) != 0) subsetParts.get(pos).add(new Subset(set.set & ~(1L << pos), set.count));
                pos++;
                tmp >>>= 1;
            }
        }
        return subsetParts;
    }

    public List<List<BitSet>> getMinFDs() {
        return new ArrayList<>(minFDs);
    }

}

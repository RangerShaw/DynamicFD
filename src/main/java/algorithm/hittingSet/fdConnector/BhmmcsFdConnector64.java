package algorithm.hittingSet.fdConnector;

import algorithm.hittingSet.BHMMCS.Bhmmcs64;
import algorithm.hittingSet.NumSet;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class BhmmcsFdConnector64 implements FdConnector {

    /**
     * number of elements or attributes
     */
    int nElements;

    /**
     * minimal FD on each rhs
     */
    List<List<BitSet>> minFDs = new ArrayList<>();

    /**
     * Bhmmcs algorithms on different rhs
     */
    List<Bhmmcs64> bhmmcsList = new ArrayList<>();


    public BhmmcsFdConnector64() {
    }

    /**
     * @param _toCover all subsets (different sets) to be covered
     */
    public void initiate(int nElements, List<? extends Number> _toCover) {
        this.nElements = nElements;

        List<Long> toCover = (List<Long>) _toCover;
        NumSet.sortLongSets(nElements, toCover);

        List<List<Long>> subsetParts = genSubsetRhss(toCover);

        for (int rhs = 0; rhs < nElements; rhs++) {
            bhmmcsList.add(new Bhmmcs64(nElements));
            bhmmcsList.get(rhs).initiate(subsetParts.get(rhs));
            minFDs.add(bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.longToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
    }

    public List<List<BitSet>> insertSubsets(List<? extends Number> _addedSets) {
        List<Long> addedSets = (List<Long>) _addedSets;

        NumSet.sortLongSets(nElements, addedSets);
        List<List<Long>> subsetParts = genSubsetRhss(addedSets);

        for (int rhs = 0; rhs < nElements; rhs++) {
            bhmmcsList.get(rhs).insertSubsets(subsetParts.get(rhs));
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.longToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
        return new ArrayList<>(minFDs);
    }

    public List<List<BitSet>> removeSubsets(List<? extends Number> _leftDiffs, Set<? extends Number> _removed) {
        List<List<Long>> leftSubsetRhss = genSubsetRhss((List<Long>) _leftDiffs);

        List<Long> rmvdDiffs = new ArrayList<>((Set<Long>) _removed);
        NumSet.sortLongSets(nElements, rmvdDiffs);
        List<List<Long>> rmvdSubsetRhss = genSubsetRhss(rmvdDiffs);

        for (int rhs = 0; rhs < nElements; rhs++) {
            bhmmcsList.get(rhs).removeSubsets(leftSubsetRhss.get(rhs), rmvdSubsetRhss.get(rhs));
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.longToBitSet(nElements, sb)).collect(Collectors.toList()));
        }

        return new ArrayList<>(minFDs);
    }

    List<List<Long>> genSubsetRhss(List<Long> subsets) {
        List<List<Long>> subsetParts = new ArrayList<>(nElements);

        for (int i = 0; i < nElements; i++)
            subsetParts.add(new ArrayList<>(subsets.size() / nElements));

        for (long set : subsets) {
            long tmp = set;
            int pos = 0;
            while (tmp > 0) {
                if ((tmp & 1) != 0) subsetParts.get(pos).add(set & ~(1L << pos));
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

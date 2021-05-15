package algorithm.hittingSet.fdConnector;

import algorithm.hittingSet.BHMMCS.Bhmmcs64;
import algorithm.hittingSet.IntSet;
import util.Utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
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
        List<Long> toCover = (List<Long>) _toCover;

        this.nElements = nElements;

        IntSet.sortLongSets(nElements, toCover);
        List<List<Long>> subsetParts = genSubsetParts(toCover);

        for (int rhs = 0; rhs < nElements; rhs++) {
            List<Long> diffSets = genDiffsOnRhs(subsetParts.get(rhs), rhs);
            bhmmcsList.add(new Bhmmcs64(nElements));
            bhmmcsList.get(rhs).initiate(diffSets);
            minFDs.add(bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.longToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
    }

    public List<List<BitSet>> insertSubsets(List<? extends Number> _addedSets) {
        List<Long> addedSets = (List<Long>) _addedSets;

        IntSet.sortLongSets(nElements, addedSets);
        List<List<Long>> subsetParts = genSubsetParts(addedSets);

        for (int rhs = 0; rhs < nElements; rhs++) {
            List<Long> newDiffSets = genDiffsOnRhs(subsetParts.get(rhs), rhs);
            bhmmcsList.get(rhs).insertSubsets(newDiffSets);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.longToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
        return new ArrayList<>(minFDs);
    }

    public List<List<BitSet>> removeSubsets(List<? extends Number> _leftDiffs, Set<? extends Number> _removed) {
        List<Long> leftDiffs = (List<Long>) _leftDiffs;
        Set<Long> removed = (Set<Long>) _removed;

        // IntSet.sortIntSets(nElements, leftDiffs);    /* leftDiffs are already sorted if we manage diffSets carefully */
        List<List<Long>> leftSubsetParts = genSubsetParts(leftDiffs);

        List<Long> rmvdDiffs = new ArrayList<>(removed);
        IntSet.sortLongSets(nElements, rmvdDiffs);

        List<List<Long>> rmvdSubsetParts = genSubsetParts(rmvdDiffs);

        for (int rhs = 0; rhs < nElements; rhs++) {
            List<Long> leftDiffSet = genDiffsOnRhs(leftSubsetParts.get(rhs), rhs);
            List<Long> removedDiffSet = genDiffsOnRhs(rmvdSubsetParts.get(rhs), rhs);
            bhmmcsList.get(rhs).removeSubsets(leftDiffSet, removedDiffSet);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.longToBitSet(nElements, sb)).collect(Collectors.toList()));
        }

        return new ArrayList<>(minFDs);
    }

    List<List<Long>> genSubsetParts(List<Long> subsets) {
        List<List<Long>> subsetParts = new ArrayList<>(nElements);

        for (int i = 0; i < nElements; i++)
            subsetParts.add(new ArrayList<>());

        for (long set : subsets)
            for (int e : IntSet.indicesOfOnes(set))
                subsetParts.get(e).add(set);

        return subsetParts;
    }

    List<Long> genDiffsOnRhs(List<Long> sets, int rhs) {
        long mask = ~(1L << rhs);
        List<Long> diffSetsOnRhs = new ArrayList<>(sets.size());

        for (long set : sets)
            diffSetsOnRhs.add(set & mask);

        return diffSetsOnRhs;
    }

    public List<List<BitSet>> getMinFDs() {
        return new ArrayList<>(minFDs);
    }

}

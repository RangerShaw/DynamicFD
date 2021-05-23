package algorithm.hittingSet.fdConnector;

import algorithm.hittingSet.BHMMCS.Bhmmcs;
import algorithm.hittingSet.NumSet;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class BhmmcsFdConnector implements FdConnector {

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
    List<Bhmmcs> bhmmcsList = new ArrayList<>();


    public BhmmcsFdConnector() {
    }

    /**
     * @param _toCover all subsets (different sets) to be covered
     */
    public void initiate(int nElements, List<? extends Number> _toCover) {
        this.nElements = nElements;

        List<Integer> toCover = (List<Integer>) _toCover;
        NumSet.sortIntSets(nElements, toCover);

        List<List<Integer>> subsetParts = genSubsetRhss(toCover);

        for (int rhs = 0; rhs < nElements; rhs++) {
            bhmmcsList.add(new Bhmmcs(nElements));
            bhmmcsList.get(rhs).initiate(subsetParts.get(rhs));
            minFDs.add(bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.intToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
    }

    public List<List<BitSet>> insertSubsets(List<? extends Number> _addedSets) {
        List<Integer> addedSets = (List<Integer>) _addedSets;

        NumSet.sortIntSets(nElements, addedSets);
        List<List<Integer>> subsetParts = genSubsetRhss(addedSets);

        for (int rhs = 0; rhs < nElements; rhs++) {
            bhmmcsList.get(rhs).insertSubsets(subsetParts.get(rhs));
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.intToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
        return new ArrayList<>(minFDs);
    }

    public List<List<BitSet>> removeSubsets(List<? extends Number> _leftDiffs, Set<? extends Number> _removed) {
        // IntSet.sortIntSets(nElements, leftDiffs);    /* leftDiffs are already sorted if we manage diffSets carefully */
        List<List<Integer>> leftSubsetRhss = genSubsetRhss((List<Integer>) _leftDiffs);

        List<Integer> rmvdDiffs = new ArrayList<>((Set<Integer>) _removed);
        NumSet.sortIntSets(nElements, rmvdDiffs);
        List<List<Integer>> rmvdSubsetRhss = genSubsetRhss(rmvdDiffs);

        for (int rhs = 0; rhs < nElements; rhs++) {
            bhmmcsList.get(rhs).removeSubsets(leftSubsetRhss.get(rhs), rmvdSubsetRhss.get(rhs));
            //bhmmcsList.get(rhs).removeSubsetsRestart(leftSubsetRhss.get(rhs));
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.intToBitSet(nElements, sb)).collect(Collectors.toList()));
        }

        return new ArrayList<>(minFDs);
    }

    List<List<Integer>> genSubsetRhss(List<Integer> subsets) {
        List<List<Integer>> subsetParts = new ArrayList<>(nElements);

        for (int i = 0; i < nElements; i++)
            subsetParts.add(new ArrayList<>());

        for (int set : subsets)
            for (int e : NumSet.indicesOfOnes(set))
                subsetParts.get(e).add(set & ~(1 << e));

        return subsetParts;
    }

    public List<List<BitSet>> getMinFDs() {
        return new ArrayList<>(minFDs);
    }

}

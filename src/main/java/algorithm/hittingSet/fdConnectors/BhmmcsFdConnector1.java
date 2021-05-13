package algorithm.hittingSet.fdConnectors;

import algorithm.hittingSet.BHMMCS.Bhmmcs1;
import util.Utils;
import util.IntSet;

import java.util.*;
import java.util.stream.Collectors;

public class BhmmcsFdConnector1 extends FdConnector {

    /**
     * Bhmmcs algorithms on different rhs
     */
    List<Bhmmcs1> bhmmcsList = new ArrayList<>();

    List<List<Integer>> subsetBuckets = new ArrayList<>();

    public BhmmcsFdConnector1() {
    }

    /**
     * @param toCover all subsets (different sets) to be covered
     */
    public void initiate(int nElements, List<Integer> toCover) {
        super.initiate(nElements);

        //generateSubsetBuckets(toCover);
        toCover.sort(Comparator.comparing(Integer::bitCount));
        List<List<Integer>> subsetParts = genSubsetParts(toCover);

        for (int rhs = 0; rhs < nElements; rhs++) {
            List<Integer> diffSets = genDiffsOnRhs1(subsetParts.get(rhs), rhs);
            bhmmcsList.add(new Bhmmcs1(nElements));
            bhmmcsList.get(rhs).initiate(diffSets);
            minFDs.add(bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.intToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
    }

    public List<List<BitSet>> insertSubsets(List<Integer> addedSets) {
        addedSets.sort(Comparator.comparing(Integer::bitCount));
        List<List<Integer>> subsetParts = genSubsetParts(addedSets);

        for (int rhs = 0; rhs < nElements; rhs++) {
            List<Integer> newDiffSets = genDiffsOnRhs1(subsetParts.get(rhs), rhs);
            bhmmcsList.get(rhs).insertSubsets(newDiffSets);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.intToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
        return new ArrayList<>(minFDs);
    }

    public List<List<BitSet>> removeSubsets(List<Integer> leftDiffs, Set<Integer> removed) {
        List<Integer> rmvdDiffs = new ArrayList<>(removed);

        leftDiffs.sort(Comparator.comparing(Integer::bitCount));
        List<List<Integer>> leftSubsetParts = genSubsetParts(leftDiffs);
        rmvdDiffs.sort(Comparator.comparing(Integer::bitCount));
        List<List<Integer>> rmvdSubsetParts = genSubsetParts(rmvdDiffs);

        for (int rhs = 0; rhs < nElements; rhs++) {
            List<Integer> leftDiffSet = genDiffsOnRhs1(leftSubsetParts.get(rhs), rhs);
            List<Integer> removedDiffSet = genDiffsOnRhs1(rmvdSubsetParts.get(rhs), rhs);
            bhmmcsList.get(rhs).removeSubsets(leftDiffSet, removedDiffSet);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets().stream().map(sb -> Utils.intToBitSet(nElements, sb)).collect(Collectors.toList()));
        }
        return new ArrayList<>(minFDs);
    }

    void genSubsetBuckets(List<Integer> subsets) {
        for (int i = 0; i < nElements + 1; i++)
            subsetBuckets.add(new ArrayList<>());

        for (int sb : subsets)
            subsetBuckets.get(Integer.bitCount(sb)).add(sb);
    }

    List<List<Integer>> genSubsetParts(List<Integer> subsets) {
        List<List<Integer>> subsetParts = new ArrayList<>(nElements);

        for (int i = 0; i < nElements; i++)
            subsetParts.add(new ArrayList<>());

        for (int set : subsets)
            for (int e : IntSet.indicesOfOnes(set))
                subsetParts.get(e).add(set);

        return subsetParts;
    }

    List<List<List<Integer>>> genSubsetParts1(List<Integer> subsets) {
        List<List<List<Integer>>> subsetParts = new ArrayList<>(nElements);

        for (int i = 0; i < nElements; i++) {
            subsetParts.add(new ArrayList<>());
            for (int j = 0; j < nElements; j++)
                subsetParts.get(i).add(new ArrayList<>());
        }

        for (int set : subsets) {
            List<Integer> ones = IntSet.indicesOfOnes(set);
            for (int i = 0; i < ones.size() - 1; i++) {
                for (int j = i + 1; j < ones.size(); j++) {
                    subsetParts.get(ones.get(i)).get(ones.get(j)).add(set);
                    subsetParts.get(ones.get(j)).get(ones.get(i)).add(set);
                }
            }
        }
        return subsetParts;
    }

    List<Integer> genDiffsOnRhs1(List<Integer> sets, int rhs) {
        int mask = ~(1 << rhs);
        List<Integer> diffSetsOnRhs = new ArrayList<>(sets.size());

        for (int set : sets)
            diffSetsOnRhs.add(set & mask);

        return diffSetsOnRhs;
    }


}

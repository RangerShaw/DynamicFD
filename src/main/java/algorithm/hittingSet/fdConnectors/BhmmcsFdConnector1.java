//package algorithm.hittingSet.fdConnectors;
//
//import algorithm.hittingSet.BHMMCS.Bhmmcs1;
//
//import java.util.ArrayList;
//import java.util.BitSet;
//import java.util.List;
//
//public class BhmmcsFdConnector1 extends FdConnector {
//
//    /**
//     * Bhmmcs algorithms on different rhs
//     */
//    List<Bhmmcs1> bhmmcsList = new ArrayList<>();
//
//
//    public BhmmcsFdConnector1() {
//    }
//
//    /**
//     * @param toCover all subsets (different sets) to be covered
//     */
//    public void initiate(int nElements, List<Integer> toCover) {
//        super.initiate(nElements);
//
//        List<List<Integer>> subsetParts = new ArrayList<>();
//        for (int i = 0; i < nElements; i++)
//            subsetParts.add(new ArrayList<>());
//
//        for (int set : toCover) {
//            for (int i = 0; i < nElements; i++)
//                if ((set & (1 << i)) != 0) subsetParts.get(i).add(set);
//        }
//
//        for (int rhs = 0; rhs < nElements; rhs++) {
//            List<Integer> diffSets = generateDiffsOnRhs1(subsetParts.get(rhs), rhs);
//            bhmmcsList.add(new Bhmmcs1(nElements));
//            bhmmcsList.get(rhs).initiate(diffSets);
//            minFDs.add(bhmmcsList.get(rhs).getMinCoverSets());
//        }
//    }
//
//    public List<List<BitSet>> insertSubsets(List<Integer> addedSets) {
//        List<List<Integer>> subsetParts = new ArrayList<>();
//        for (int i = 0; i < nElements; i++)
//            subsetParts.add(new ArrayList<>());
//        for (int set : addedSets) {
//            for (int i = 0; i < nElements; i++)
//                if ((set & (1 << i)) != 0) subsetParts.get(i).add(set);
//        }
//
//        for (int rhs = 0; rhs < nElements; rhs++) {
//            List<Integer> newDiffSets = generateDiffsOnRhs1(subsetParts.get(rhs), rhs);
//            bhmmcsList.get(rhs).insertSubsets(newDiffSets);
//            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
//        }
//        return new ArrayList<>(minFDs);
//    }
//
//    public List<List<BitSet>> removeSubsets(List<BitSet> remainedSets) {
//        for (int rhs = 0; rhs < nElements; rhs++) {
//            List<BitSet> newDiffSets = generateDiffsOnRhs(remainedSets, rhs);
//            bhmmcsList.get(rhs).removeSubsets(newDiffSets);
//            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
//        }
//        return new ArrayList<>(minFDs);
//    }
//
//    public List<List<BitSet>> removeSubsets(List<Integer> leftDiffSets, List<Integer> removedDiffSets) {
//        for (int rhs = 0; rhs < nElements; rhs++) {
//            List<BitSet> leftDiffSet = generateDiffsOnRhs(leftDiffSets, rhs);
//            List<BitSet> removedDiffSet = generateDiffsOnRhs(removedDiffSets, rhs);
//            bhmmcsList.get(rhs).removeSubsets(leftDiffSet, removedDiffSet);
//            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
//        }
//        return new ArrayList<>(minFDs);
//    }
//
//    List<Integer> generateDiffsOnRhs1(List<Integer> sets, int rhs) {
//        List<Integer> diffSetsOnRhs = new ArrayList<>();
//
//        for (int set : sets) {
//            diffSetsOnRhs.add(set & ~(1 << rhs));
//        }
//
//        return diffSetsOnRhs;
//    }
//
//
//}

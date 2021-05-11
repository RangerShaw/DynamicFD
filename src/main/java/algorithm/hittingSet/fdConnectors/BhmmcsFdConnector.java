package algorithm.hittingSet.fdConnectors;

//import algorithm.hittingSet.BHMMCS.Bhmmcs1;
import algorithm.hittingSet.BHMMCS.Bhmmcs;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BhmmcsFdConnector extends FdConnector {

    /**
     * Bhmmcs algorithms on different rhs
     */
    List<Bhmmcs> bhmmcsList = new ArrayList<>();

    List<List<Integer>> subsetParts = new ArrayList<>();

    public BhmmcsFdConnector() {
    }

    /**
     * @param toCover all subsets (different sets) to be covered
     */
    public void initiate(int nElements, List<BitSet> toCover) {
        super.initiate(nElements);

        for (int rhs = 0; rhs < nElements; rhs++) {
            List<BitSet> diffSets = generateDiffsOnRhs(toCover, rhs);
            bhmmcsList.add(new Bhmmcs(nElements));
            bhmmcsList.get(rhs).initiate(diffSets);
            minFDs.add(bhmmcsList.get(rhs).getMinCoverSets());
        }
    }

    public List<List<BitSet>> insertSubsets(List<BitSet> addedSets) {
        for (int rhs = 0; rhs < nElements; rhs++) {
            List<BitSet> newDiffSets = generateDiffsOnRhs(addedSets, rhs);
            bhmmcsList.get(rhs).insertSubsets(newDiffSets);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
        }
        return new ArrayList<>(minFDs);
    }

    public List<List<BitSet>> removeSubsets(List<BitSet> remainedSets) {
        for (int rhs = 0; rhs < nElements; rhs++) {
            List<BitSet> newDiffSets = generateDiffsOnRhs(remainedSets, rhs);
            bhmmcsList.get(rhs).removeSubsets(newDiffSets);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
        }
        return new ArrayList<>(minFDs);
    }

    public List<List<BitSet>> removeSubsets(List<BitSet> leftDiffSets, List<BitSet> removedDiffSets) {
        for (int rhs = 0; rhs < nElements; rhs++) {
            List<BitSet> leftDiffSet = generateDiffsOnRhs(leftDiffSets, rhs);
            List<BitSet> removedDiffSet = generateDiffsOnRhs(removedDiffSets, rhs);
            //bhmmcsList.get(rhs).removeSubsets(leftDiffSet, removedDiffSet);
            bhmmcsList.get(rhs).removeSubsets(leftDiffSet);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
        }
        return new ArrayList<>(minFDs);
    }

}

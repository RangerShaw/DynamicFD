package algorithm.hittingSet.fdConnectors;

import algorithm.hittingSet.BHMMCS.Bhmmcs;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class BhmmcsFdConnector extends FdConnector {

    /**
     * Bhmmcs algorithms on different rhs
     */
    List<Bhmmcs> bhmmcsList = new ArrayList<>();


    public BhmmcsFdConnector() {
    }

    /**
     * @param toCover all subsets (different sets) to be covered
     */
    public void initiate(int nElements, List<BitSet> toCover) {
        super.initiate(nElements);

        for (int rhs = 0; rhs < nElements; rhs++) {
            //System.out.println("  [FdConnector] initiating on rhs " + rhs + "...");
            List<BitSet> diffSets = generateDiffSetsOnRhs(toCover, rhs);
            bhmmcsList.add(new Bhmmcs(nElements));
            bhmmcsList.get(rhs).initiate(diffSets);
            minFDs.add(bhmmcsList.get(rhs).getMinCoverSets());
        }
    }

    public void insertSubsets(List<BitSet> addedSets) {
        for (int rhs = 0; rhs < nElements; rhs++) {
            List<BitSet> newDiffSets = generateDiffSetsOnRhs(addedSets, rhs);
            bhmmcsList.get(rhs).insertSubsets(newDiffSets);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
        }
    }

    public void removeSubsets(List<BitSet> remainedSets) {
        for (int rhs = 0; rhs < nElements; rhs++) {
            List<BitSet> newDiffSets = generateDiffSetsOnRhs(remainedSets, rhs);
            bhmmcsList.get(rhs).removeSubsets(newDiffSets);
            minFDs.set(rhs, bhmmcsList.get(rhs).getMinCoverSets());
        }
    }

}

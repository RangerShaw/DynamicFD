package algorithm.hittingSet.fdConnectors;

import java.util.*;

public abstract class FdConnector {

    /**
     * number of elements or attributes
     */
    protected int nElements;

    /**
     * minimal FD on each rhs
     */
    protected List<List<BitSet>> minFDs = new ArrayList<>();


    protected FdConnector() {
    }

    public void initiate(int nElements) {
        this.nElements = nElements;
    }

    protected List<BitSet> generateDiffsOnRhs(List<BitSet> diffSets, int rhs) {
        List<BitSet> diffSetsOnRhs = new ArrayList<>();

        for (BitSet diffSet : diffSets) {
            if (diffSet.get(rhs)) {
                BitSet diffSetRhs = (BitSet) diffSet.clone();
                diffSetRhs.clear(rhs);
                diffSetsOnRhs.add(diffSetRhs);
            }
        }

        return diffSetsOnRhs;
    }

    protected Set<BitSet> generateDiffsOnRhs(Set<BitSet> diffSets, int rhs) {
        Set<BitSet> diffSetsOnRhs = new HashSet<>();

        for (BitSet diffSet : diffSets) {
            if (diffSet.get(rhs)) {
                BitSet diffSetRhs = (BitSet) diffSet.clone();
                diffSetRhs.clear(rhs);
                diffSetsOnRhs.add(diffSetRhs);
            }
        }

        return diffSetsOnRhs;
    }

    public List<List<BitSet>> getMinFDs() {
        return new ArrayList<>(minFDs);
    }

}

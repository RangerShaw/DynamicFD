package algorithm.hittingSet.fdConnector;

import java.util.BitSet;
import java.util.List;
import java.util.Set;

public interface FdConnector {

    void initiate(int nElements, List<? extends Number> toCover);

    List<List<BitSet>> insertSubsets(List<? extends Number> addedSets);

    List<List<BitSet>> removeSubsets(List<? extends Number> leftDiffs, Set<? extends Number> removed);

    List<List<BitSet>> getMinFDs();
}

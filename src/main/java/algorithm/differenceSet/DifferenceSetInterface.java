package algorithm.differenceSet;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DifferenceSetInterface {

    Map<BitSet, Integer> generateDiffSet(List<List<Integer>> inversePli);

    List<Integer> generateDiffSet(List<List<Integer>> inversePli, String diffFp);

    List<Integer> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli);

    List<Integer> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedData, boolean[] removed, Set<Integer> removedDiffs);

    List<Integer> getDiffSet();

}

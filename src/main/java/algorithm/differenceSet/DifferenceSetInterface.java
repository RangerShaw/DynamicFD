package algorithm.differenceSet;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DifferenceSetInterface {

    Map<BitSet, Integer> generateDiffSet(List<List<Integer>> inversePli);

    List<? extends Number> generateDiffSet(List<List<Integer>> inversePli, String diffFp);

    List<? extends Number> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli);

    Set<? extends Number> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedData, boolean[] removed);

    List<? extends Number> getDiffSet();

}

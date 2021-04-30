package algorithm.differenceSet;

import java.util.BitSet;
import java.util.List;

public interface DifferenceSetInterface {

    List<BitSet> generateDiffSet(List<List<Integer>> inversePli);

    List<BitSet> generateDiffSet(List<List<Integer>> inversePli, String diffFp);

    List<BitSet> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli);

    List<BitSet> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedData, boolean[] removed);

    List<BitSet> getDiffSet();

}

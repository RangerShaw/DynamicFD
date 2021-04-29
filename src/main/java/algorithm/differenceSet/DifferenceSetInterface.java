package algorithm.differenceSet;

import java.util.BitSet;
import java.util.List;

public interface DifferenceSetInterface {

    public List<BitSet> generateDiffSet(List<List<Integer>> inversePli);

    public List<BitSet> generateDiffSet(List<List<Integer>> inversePli, String diffFp);

    public List<BitSet> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli);

    public List<BitSet> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli,
                                   List<Integer> removedData, boolean[] removed);

    public List<BitSet> getDiffSet();

}

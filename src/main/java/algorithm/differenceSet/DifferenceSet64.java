package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashLongIntMap;
import com.koloboke.collect.map.hash.HashLongIntMaps;
import util.DataIO;
import util.Utils;

import java.util.*;

public class DifferenceSet64 implements DifferenceSetInterface {

    int nAttributes;

    int nTuples;

    List<BitSet> diffSet = new ArrayList<>();

    /**
     * hashcode of a Diff -> count of its occurrences
     */
    HashLongIntMap dfFreq = HashLongIntMaps.newMutableMap();


    public DifferenceSet64() {
    }

    void initiateDataStructure(List<List<Integer>> inversePli) {
        nTuples = inversePli.size();
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();
    }

    public List<BitSet> generateDiffSet(List<List<Integer>> inversePli) {
        initiateDataStructure(inversePli);

        Map<BitSet, Integer> diffSetMap = new HashMap<>();
        for (int t1 = 0; t1 < nTuples - 1; t1++) {
            for (int t2 = t1 + 1; t2 < nTuples; t2++) {
                BitSet diffSet = new BitSet(nAttributes);
                for (int e = 0; e < nAttributes; e++)
                    if (!inversePli.get(t1).get(e).equals(inversePli.get(t2).get(e)))
                        diffSet.set(e);
                diffSetMap.put(diffSet, diffSetMap.getOrDefault(diffSet, 0) + 1);
            }
        }

        diffSet.addAll(diffSetMap.keySet());

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            dfFreq.addValue(Utils.bitsetToLong(nAttributes, df.getKey()), df.getValue());

        return new ArrayList<>(diffSet);
    }

    public List<BitSet> generateDiffSet(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        Map<BitSet, Integer> diffSetMap = DataIO.readDiffSetsMap(diffFp);

        diffSet.addAll(diffSetMap.keySet());

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            dfFreq.addValue(Utils.bitsetToLong(nAttributes, df.getKey()), df.getValue());

        return new ArrayList<>(diffSet);
    }

    /**
     * @return new Diffs
     */
    public List<BitSet> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        long[] diffHash = new long[inversePli.size()];

        long initHash = 0L;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1L << i);

        List<BitSet> newDiffs = new ArrayList<>();

        for (int t = nTuples; t < inversePli.size(); t++) {
            // reset structures
            Arrays.fill(diffHash, initHash);

            // update pli, generate diffBools and diffHash
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {
                    long mask = ~(1L << (nAttributes - 1 - e)); // existing cluster
                    for (int neighbor : pliE.get(clstId)) {
                        diffHash[neighbor] &= mask;
                    }
                }

                pliE.get(clstId).add(t);
            }

            // generate new diff
            for (int i = 0; i < t; i++) {
                if (dfFreq.addValue(diffHash[i], 1, 0) == 1)
                    newDiffs.add(Utils.longToBitSet(nAttributes, diffHash[i]));
            }
        }

        diffSet.addAll(newDiffs);
        nTuples = inversePli.size();

        return newDiffs;
    }

    /**
     * @return remaining Diffs
     */
    public List<BitSet> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedData, boolean[] removed) {
        long[] diffHash = new long[inversePli.size()];

        long initHash = 0;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1L << i);

        Set<BitSet> removedDiffs = new HashSet<>();

        for (int t : removedData) {
            // reset structures
            Arrays.fill(diffHash, initHash);

            // generate diffBools and diffHash
            for (int e = 0; e < nAttributes; e++) {
                long mask = ~(1L << (nAttributes - 1 - e));   // existing cluster
                for (int neighbor : pli.get(e).get(inversePli.get(t).get(e))) {
                    diffHash[neighbor] &= mask;
                }
            }

            // generate removed diff
            for (int i = 0; i < diffHash.length; i++) {
                if (!removed[i] && dfFreq.addValue(diffHash[i], -1) == 0)
                    removedDiffs.add(Utils.longToBitSet(nAttributes, diffHash[i]));
            }
        }

        diffSet.removeIf(removedDiffs::contains);
        nTuples -= removed.length;

        return new ArrayList<>(diffSet);
    }

    public List<BitSet> getDiffSet() {
        return new ArrayList<>(diffSet);
    }


}
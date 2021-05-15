package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashLongIntMap;
import com.koloboke.collect.map.hash.HashLongIntMaps;
import util.DataIO;
import algorithm.hittingSet.IntSet;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class DifferenceSet64 implements DifferenceSetInterface {

    int nAttributes;

    int nTuples;

    List<Long> diffSet = new ArrayList<>();

    /**
     * hashcode of a Diff -> count of its occurrences
     */
    HashLongIntMap dfFreq = HashLongIntMaps.newMutableMap();

    long initHash = 0L;


    public DifferenceSet64() {
    }

    void initiateDataStructure(List<List<Integer>> inversePli) {
        nTuples = inversePli.size();
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();

        for (int i = 0; i < nAttributes; i++)
            initHash |= 1L << i;
    }

    public Map<BitSet, Integer> generateDiffSet(List<List<Integer>> inversePli) {
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

        diffSet.addAll(diffSetMap.keySet().stream().map(bs -> Utils.bitsetToLong(nAttributes, bs)).collect(Collectors.toList()));

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            dfFreq.addValue(Utils.bitsetToLong(nAttributes, df.getKey()), df.getValue());

        return diffSetMap;
    }

    public List<Long> generateDiffSet(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        Map<BitSet, Integer> diffSetMap = DataIO.readDiffSetsMap(diffFp);

        diffSet.addAll(diffSetMap.keySet().stream().map(bs -> Utils.bitsetToLong(nAttributes, bs)).collect(Collectors.toList()));

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            dfFreq.put(Utils.bitsetToLong(nAttributes, df.getKey()), (int) df.getValue());

        return new ArrayList<>(diffSet);
    }

    /**
     * @return new Diffs
     */
    public List<Long> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        long[] diffHash = new long[inversePli.size()];

        long initHash = 0L;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1L << i);

        List<Long> newDiffs = new ArrayList<>();

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
                    long mask = ~(1L << e); // existing cluster
                    for (int neighbor : pliE.get(clstId))
                        diffHash[neighbor] &= mask;
                }

                pliE.get(clstId).add(t);
            }

            // generate new diff
            for (int i = 0; i < t; i++) {
                if (dfFreq.addValue(diffHash[i], 1, 0) == 1)
                    newDiffs.add(diffHash[i]);
            }
        }

        diffSet.addAll(newDiffs);
        IntSet.sortLongSets(nAttributes, diffSet);
        nTuples = inversePli.size();

        return newDiffs;
    }

    /**
     * @return remaining Diffs
     */
    public Set<Long> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedData, boolean[] removed) {
        Set<Long> removedDiffs = new HashSet<>();
        long[] diffHash = new long[inversePli.size()];

        for (int t : removedData) {
            // reset structures
            Arrays.fill(diffHash, initHash);

            // generate diffBools and diffHash
            for (int e = 0; e < nAttributes; e++) {
                long mask = ~(1L << e);   // existing cluster
                for (int neighbor : pli.get(e).get(inversePli.get(t).get(e))) {
                    diffHash[neighbor] &= mask;
                }
            }

            // generate removed diff
            for (int i = 0; i < diffHash.length; i++) {
                if (!removed[i] && dfFreq.addValue(diffHash[i], -1) == 0)
                    removedDiffs.add(diffHash[i]);
            }
        }

        diffSet.removeIf(removedDiffs::contains);
        nTuples -= removed.length;

        return removedDiffs;
    }

    public List<Long> getDiffSet() {
        return new ArrayList<>(diffSet);
    }

}
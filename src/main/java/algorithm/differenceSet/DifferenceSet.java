package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashIntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import util.DataIO;
import algorithm.hittingSet.IntSet;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class DifferenceSet implements DifferenceSetInterface {

    int nAttributes;

    int nTuples;

    List<Integer> diffSet = new ArrayList<>();

    /**
     * hashcode of a Diff -> count of its occurrences
     */
    HashIntIntMap diffFreq = HashIntIntMaps.newMutableMap();

    int initHash = 0;


    public DifferenceSet() {
    }

    void initiateDataStructure(List<List<Integer>> inversePli) {
        nTuples = inversePli.size();
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();

        for (int i = 0; i < nAttributes; i++)
            initHash |= 1 << i;
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

        diffSet.addAll(diffSetMap.keySet().stream().map(bs -> Utils.bitsetToInt(nAttributes, bs)).collect(Collectors.toList()));
        IntSet.sortIntSets(nAttributes, diffSet);

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            diffFreq.addValue(Utils.bitsetToInt(nAttributes, df.getKey()), df.getValue());

        return diffSetMap;
    }

    public List<Integer> generateDiffSet(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        Map<BitSet, Integer> diffSetMap = DataIO.readDiffSetsMap(diffFp);

        diffSet.addAll(diffSetMap.keySet().stream().map(bs -> Utils.bitsetToInt(nAttributes, bs)).collect(Collectors.toList()));
        IntSet.sortIntSets(nAttributes, diffSet);

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            diffFreq.put(Utils.bitsetToInt(nAttributes, df.getKey()), (int) df.getValue());

        return new ArrayList<>(diffSet);
    }

    /**
     * @return new Diffs
     */
    public List<Integer> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        int[] diffHash = new int[inversePli.size()];

        int initHash = 0;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1 << i);

        List<Integer> newDiffs = new ArrayList<>();

        // for each newly inserted tuple, generate its diffs with all front tuples
        for (int t = nTuples; t < inversePli.size(); t++) {
            // reset diffHash
            Arrays.fill(diffHash, initHash);

            // update pli, generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {                                          // existing cluster
                    int mask = ~(1 << e);
                    for (int neighbor : pliE.get(clstId))
                        diffHash[neighbor] &= mask;
                }

                pliE.get(clstId).add(t);
            }

            // generate new diff
            for (int i = 0; i < t; i++) {
                if (diffFreq.addValue(diffHash[i], 1, 0) == 1)
                    newDiffs.add(diffHash[i]);
            }
        }

        diffSet.addAll(newDiffs);
        IntSet.sortIntSets(nAttributes, diffSet);
        nTuples = inversePli.size();

        return newDiffs;
    }

    /**
     * @return remaining Diffs
     */
    public Set<Integer> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedData, boolean[] removed) {
        Set<Integer> removedDiffs = new HashSet<>();
        int[] diffHash = new int[inversePli.size()];

        for (int t : removedData) {
            // reset diffHash
            Arrays.fill(diffHash, initHash);

            // generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                int mask = ~(1 << e);
                for (int neighbor : pli.get(e).get(inversePli.get(t).get(e)))
                    diffHash[neighbor] &= mask;
            }

            // generate removed diff
            for (int i = 0; i < diffHash.length; i++) {
                if ((!removed[i] || i < t) && diffFreq.addValue(diffHash[i], -1) == 0)
                    removedDiffs.add(diffHash[i]);
            }
        }

        diffSet.removeAll(removedDiffs);
        nTuples -= removed.length;

        return removedDiffs;
    }

    public List<Integer> getDiffSet() {
        return new ArrayList<>(diffSet);
    }


}
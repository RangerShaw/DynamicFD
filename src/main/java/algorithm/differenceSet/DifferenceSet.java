package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashIntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import util.DataIO;
import util.Utils;

import java.util.*;

public class DifferenceSet implements DifferenceSetInterface{

    int nAttributes;

    int nTuples;

    List<BitSet> diffSet = new ArrayList<>();

    /**
     * hashcode of a Diff -> count of its occurrences
     */
    HashIntIntMap diffFreq = HashIntIntMaps.newMutableMap();


    public DifferenceSet() {
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
            diffFreq.addValue(Utils.bitsetToInt(nAttributes, df.getKey()), df.getValue());

        return new ArrayList<>(diffSet);
    }

    public List<BitSet> generateDiffSet(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        Map<BitSet, Integer> diffSetMap = DataIO.readDiffSetsMap(diffFp);

        diffSet.addAll(diffSetMap.keySet());

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            diffFreq.addValue(Utils.bitsetToInt(nAttributes, df.getKey()), df.getValue());

        return new ArrayList<>(diffSet);
    }

    /**
     * @return new Diffs
     */
    public List<BitSet> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        int[] diffHash = new int[inversePli.size()];
        boolean[][] diffBools = new boolean[inversePli.size()][nAttributes];

        int initHash = 0;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1 << i);

        List<BitSet> newDiffs = new ArrayList<>();

        for (int t = nTuples; t < inversePli.size(); t++) {
            // reset structures
            Arrays.fill(diffHash, initHash);
            for (int i = 0; i < t; i++)
                Arrays.fill(diffBools[i], true);

            // update pli, generate diffBools and diffHash
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {
                    int mask = ~(1 << e);   // existing cluster
                    for (int neighbor : pliE.get(clstId)) {
                        diffBools[neighbor][e] = false;
                        diffHash[neighbor] &= mask;
                    }
                }

                pliE.get(clstId).add(t);
            }

            // generate new diff
            for (int i = 0; i < t; i++) {
                if (diffFreq.addValue(diffHash[i], 1, 0) == 1)
                    newDiffs.add(Utils.boolArrayToBitSet(diffBools[i]));
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
        int[] diffHash = new int[inversePli.size()];
        boolean[][] diffBools = new boolean[inversePli.size()][nAttributes];

        int initHash = 0;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1 << i);

        Set<BitSet> removedDiffs = new HashSet<>();

        for (int t : removedData) {
            // reset structures
            Arrays.fill(diffHash, initHash);
            for (boolean[] diffBool : diffBools)
                Arrays.fill(diffBool, true);

            // generate diffBools and diffHash
            for (int e = 0; e < nAttributes; e++) {
                int mask = ~(1 << e);   // existing cluster
                for (int neighbor : pli.get(e).get(inversePli.get(t).get(e))) {
                    diffBools[neighbor][e] = false;
                    diffHash[neighbor] &= mask;
                }
            }

            // generate removed diff
            for (int i = 0; i < diffHash.length; i++) {
                if (!removed[i] && diffFreq.addValue(diffHash[i], -1) == 0)
                    removedDiffs.add(Utils.boolArrayToBitSet(diffBools[i]));
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
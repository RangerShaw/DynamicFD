package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashIntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import com.koloboke.collect.map.hash.HashIntLongMap;
import com.koloboke.collect.map.hash.HashIntLongMaps;
import me.tongfei.progressbar.ProgressBar;
import util.DataIO;
import algorithm.hittingSet.NumSet;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DifferenceSet implements DifferenceSetInterface {

    int nAttributes;

    int nTuples;

    List<Integer> diffSet = new ArrayList<>();

    /**
     * hashcode of a Diff -> count of its occurrences
     */
    HashIntLongMap diffFreq = HashIntLongMaps.newMutableMap();

    int fullDiff = 0;


    public DifferenceSet() {
    }

    void initiateDataStructure(List<List<Integer>> inversePli) {
        nTuples = inversePli.size();
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();

        for (int i = 0; i < nAttributes; i++)
            fullDiff |= 1 << i;
    }

    public Map<BitSet, Long> generateDiffSet(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();

        for (int i = 0; i < nAttributes; i++)
            fullDiff |= 1 << i;

        initInsertData(pli, inversePli);

        Map<BitSet, Long> diffSetMap = new HashMap<>();
        for (Map.Entry<Integer, Long> df : diffFreq.entrySet())
            diffSetMap.put(Utils.intToBitSet(nAttributes, df.getKey()), df.getValue());

        return diffSetMap;
    }

    public List<Integer> generateDiffSet(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        Map<BitSet, Long> diffSetMap = DataIO.readDiffSetsMap(diffFp);

        diffSet.addAll(diffSetMap.keySet().stream().map(bs -> Utils.bitsetToInt(nAttributes, bs)).collect(Collectors.toList()));
        NumSet.sortIntSets(nAttributes, diffSet);

        for (Map.Entry<BitSet, Long> df : diffSetMap.entrySet())
            diffFreq.put(Utils.bitsetToInt(nAttributes, df.getKey()), (long) df.getValue());

        return new ArrayList<>(diffSet);
    }

    /**
     * @return new Diffs
     */
    public List<Integer> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        int[] diffHash = new int[inversePli.size()];

        List<Integer> newDiffs = new ArrayList<>();

        // for each newly inserted tuple, generate its diffs with all front tuples
        for (int t = nTuples; t < inversePli.size(); t++) {
            // reset diffHash
            for (int i = 0; i < t; i++)
                diffHash[i] = fullDiff;

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
                if (diffFreq.addValue(diffHash[i], 1L, 0L) == 1L)
                    newDiffs.add(diffHash[i]);
            }
        }

        diffSet.addAll(newDiffs);
        NumSet.sortIntSets(nAttributes, diffSet);

        nTuples = inversePli.size();

        return newDiffs;
    }

    public List<Integer> insertData1(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        HashIntIntMap diffMap = HashIntIntMaps.newMutableMap();   // neighbor -> diff with it

        List<Integer> newDiffs = new ArrayList<>();
        long nFullDiff = 0;

        // for each newly inserted tuple, generate its diffs with all front tuples
        for (int t = nTuples; t < inversePli.size(); t++) {
            diffMap.clear();

            // update pli, generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {                                          // existing cluster
                    int mask = -(1 << e);
                    for (int neighbor : pliE.get(clstId))
                        diffMap.addValue(neighbor, mask, fullDiff);
                }

                pliE.get(clstId).add(t);
            }

            for (int diff : diffMap.values()) {
                if (diffFreq.addValue(diff, 1L, 0L) == 1L)
                    newDiffs.add(diff);
            }

            nFullDiff += t - diffMap.size();
        }

        if (nFullDiff > 0 && nFullDiff == diffFreq.addValue(fullDiff, nFullDiff, 0L))
            newDiffs.add(fullDiff);

        diffSet.addAll(newDiffs);
        NumSet.sortIntSets(nAttributes, diffSet);

        nTuples = inversePli.size();

        return newDiffs;
    }

    public List<Integer> initInsertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        HashIntIntMap diffMap = HashIntIntMaps.newMutableMap();

        List<Integer> newDiffs = new ArrayList<>();
        long[] nFullDiff = new long[]{0};

        // for each newly inserted tuple, generate its diffs with all front tuples
        ProgressBar.wrap(IntStream.range(nTuples, inversePli.size()), "Task").forEach(t -> {
            // reset diffHash
            diffMap.clear();

            // update pli, generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {                                          // existing cluster
                    int mask = -(1 << e);
                    for (int neighbor : pliE.get(clstId))
                        diffMap.addValue(neighbor, mask, fullDiff);
                }

                pliE.get(clstId).add(t);
            }

            for (int diff : diffMap.values()) {
                if (diffFreq.addValue(diff, 1L, 0L) == 1L)
                    newDiffs.add(diff);
            }

            nFullDiff[0] += t - diffMap.size();
        });

        if (nFullDiff[0] > 0) {
            diffFreq.addValue(fullDiff, nFullDiff[0], 0L);
            newDiffs.add(fullDiff);
        }

        diffSet.addAll(newDiffs);
        NumSet.sortIntSets(nAttributes, diffSet);

        nTuples = inversePli.size();

        return newDiffs;
    }

    /**
     * @return remaining Diffs
     */
    public Set<Integer> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli,
                                    List<Integer> removedData, boolean[] removed) {
        Set<Integer> removedDiffs = new HashSet<>();
        int[] diffHash = new int[inversePli.size()];

        for (int t : removedData) {
            // reset diffHash
            for (int i = 0; i < t; i++)
                diffHash[i] = fullDiff;

            // generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                int mask = ~(1 << e);
                for (int neighbor : pli.get(e).get(inversePli.get(t).get(e)))
                    diffHash[neighbor] &= mask;
            }

            // generate removed diff
            for (int i = 0; i < diffHash.length; i++) {
                if ((!removed[i] || i < t) && diffFreq.addValue(diffHash[i], -1L) == 0L)
                    removedDiffs.add(diffHash[i]);
            }
        }

        diffSet.removeAll(removedDiffs);
        nTuples -= removed.length;

        return removedDiffs;
    }

    public Set<Integer> removeData1(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedTuples, boolean[] removed) {
        Set<Integer> removedDiffs = new HashSet<>();
        HashIntIntMap diffMap = HashIntIntMaps.newMutableMap();
        long nFullDiff = 0;

        for (int i = 0; i < removedTuples.size(); i++) {
            int t = removedTuples.get(i);

            // reset diffHash
            diffMap.clear();

            // generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                int mask = -(1 << e);
                for (int neighbor : pli.get(e).get(inversePli.get(t).get(e)))
                    if (!removed[neighbor] || neighbor < t) diffMap.addValue(neighbor, mask, fullDiff);
            }

            for (int diff : diffMap.values()) {
                if (diffFreq.addValue(diff, -1L) == 0L)
                    removedDiffs.add(diff);
            }

            nFullDiff += nTuples - removedTuples.size() + i - diffMap.size();
        }

        if (nFullDiff > 0 && 0L == diffFreq.addValue(fullDiff, -nFullDiff))
            removedDiffs.add(fullDiff);

        diffSet.removeAll(removedDiffs);
        nTuples -= removed.length;

        return removedDiffs;
    }

    public List<Integer> getDiffSet() {
        return new ArrayList<>(diffSet);
    }

    public HashIntLongMap getDiffFreq() {
        return diffFreq;
    }
}
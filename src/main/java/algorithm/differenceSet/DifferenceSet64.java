package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashIntLongMap;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import me.tongfei.progressbar.ProgressBar;
import util.DataIO;
import algorithm.hittingSet.NumSet;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DifferenceSet64 implements DifferenceSetInterface {

    int nAttributes;

    int nTuples;

    List<Long> diffSet = new ArrayList<>();

    /**
     * hashcode of a Diff -> count of its occurrences
     */
    HashLongLongMap diffFreq = HashLongLongMaps.newMutableMap();

    long initHash = 0;


    public DifferenceSet64() {
    }

    void initiateDataStructure(List<List<Integer>> inversePli) {
        nTuples = inversePli.size();
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();

        for (int i = 0; i < nAttributes; i++)
            initHash |= 1L << i;
    }

    public Map<BitSet, Long> generateDiffSet(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();

        for (int i = 0; i < nAttributes; i++)
            initHash |= 1L << i;

        initInsertData(pli, inversePli);

        Map<BitSet, Long> diffSetMap = new HashMap<>();
        for (Map.Entry<Long, Long> df : diffFreq.entrySet())
            diffSetMap.put(Utils.longToBitSet(nAttributes, df.getKey()), df.getValue());

        return diffSetMap;
    }

    public List<Long> generateDiffSet(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        Map<BitSet, Long> diffSetMap = DataIO.readDiffSetsMap(diffFp);

        diffSet.addAll(diffSetMap.keySet().stream().map(bs -> Utils.bitsetToLong(nAttributes, bs)).collect(Collectors.toList()));
        NumSet.sortLongSets(nAttributes, diffSet);

        for (Map.Entry<BitSet, Long> df : diffSetMap.entrySet())
            diffFreq.put(Utils.bitsetToInt(nAttributes, df.getKey()), (long) df.getValue());

        return new ArrayList<>(diffSet);
    }

    /**
     * @return new Diffs
     */
    public List<Long> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        long[] diffHash = new long[inversePli.size()];

        List<Long> newDiffs = new ArrayList<>();

        // for each newly inserted tuple, generate its diffs with all front tuples
        for (int t = nTuples; t < inversePli.size(); t++) {
            // reset diffHash
            for (int i = 0; i < t; i++)
                diffHash[i] = initHash;

            // update pli, generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {                                          // existing cluster
                    long mask = ~(1L << e);
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
        NumSet.sortLongSets(nAttributes, diffSet);

        nTuples = inversePli.size();

        return newDiffs;
    }

    public List<Long> initInsertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        long[] diffHash = new long[inversePli.size()];

        List<Long> newDiffs = new ArrayList<>();

        ProgressBar.wrap(IntStream.range(nTuples, inversePli.size()), "Task").forEach(t -> {
        //for (int t = nTuples; t < inversePli.size(); t++) {
            // reset diffHash
            for (int i = 0; i < t; i++)
                diffHash[i] = initHash;

            // update pli, generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {                                          // existing cluster
                    long mask = ~(1L << e);
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
        });

        diffSet.addAll(newDiffs);
        NumSet.sortLongSets(nAttributes, diffSet);

        nTuples = inversePli.size();

        return newDiffs;
    }

    /**
     * @return remaining Diffs
     */
    public Set<Long> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli,
                                   List<Integer> removedData, boolean[] removed) {
        Set<Long> removedDiffs = new HashSet<>();
        long[] diffHash = new long[inversePli.size()];

        for (int t : removedData) {
            // reset diffHash
            Arrays.fill(diffHash, initHash);

            // generate diffHash
            for (int e = 0; e < nAttributes; e++) {
                long mask = ~(1L << e);
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

    public List<Long> getDiffSet() {
        return new ArrayList<>(diffSet);
    }

    public HashIntLongMap getDiffFreq() {
        return null;
    }
}
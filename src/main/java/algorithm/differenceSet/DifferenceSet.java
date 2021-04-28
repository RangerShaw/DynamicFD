package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashIntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import com.koloboke.collect.set.hash.HashIntSet;
import com.koloboke.collect.set.hash.HashIntSets;
import util.DataIO;
import util.Utils;

import java.util.*;

public class DifferenceSet {

    // TODO: int nNextTuple, avoid using the same tuple ID

    int nAttributes;

    int nTuples;

    List<BitSet> diffSet = new ArrayList<>();

    HashIntIntMap dfFreq = HashIntIntMaps.newMutableMap();


    public DifferenceSet() {
    }

    void initiateDataStructure(List<List<Integer>> inversePli) {
        nTuples = inversePli.size();
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();
    }

    public List<BitSet> generateDiffSets(List<List<Integer>> inversePli) {
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
            dfFreq.addValue(Utils.bitsetToInt(nAttributes, df.getKey()), df.getValue());

        return new ArrayList<>(diffSet);
    }

    public List<BitSet> generateDiffSets(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        Map<BitSet, Integer> diffSetMap = DataIO.readDiffSetsMap(diffFp);

        diffSet.addAll(diffSetMap.keySet());

        for (Map.Entry<BitSet, Integer> df : diffSetMap.entrySet())
            dfFreq.addValue(Utils.bitsetToInt(nAttributes, df.getKey()), df.getValue());

        return new ArrayList<>(diffSet);
    }

    public List<BitSet> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        int[] dfHashCodes = new int[inversePli.size()];
        boolean[][] diffBools = new boolean[inversePli.size()][nAttributes];

        int initHash = 0;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1 << i);

        List<BitSet> newDiffs = new ArrayList<>();

        for (int t = nTuples; t < inversePli.size(); t++) {
            // reset structures
            Arrays.fill(dfHashCodes, initHash);
            for (int i = 0; i < t; i++)
                Arrays.fill(diffBools[i], true);

            // update pli
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {
                    int mask = ~(1 << (nAttributes - 1 - e));   // existing cluster
                    for (int neighbor : pliE.get(clstId)) {
                        diffBools[neighbor][e] = false;
                        dfHashCodes[neighbor] &= mask;
                    }
                }

                pliE.get(clstId).add(t);
            }

            // generate diff
            for (int i = 0; i < t; i++) {
                if (dfFreq.addValue(dfHashCodes[i], 1, 0) == 1)
                    newDiffs.add(Utils.boolArrayToBitSet(diffBools[i]));
            }
        }

        diffSet.addAll(newDiffs);
        nTuples = inversePli.size();

        return newDiffs;
    }

    public List<BitSet> removeData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli, List<Integer> removedData) {
        int[] dfHashCodes = new int[inversePli.size()];
        boolean[][] diffBools = new boolean[inversePli.size()][nAttributes];
        HashIntSet removedTupleSet = HashIntSets.newMutableSet(removedData);

        int initHash = 0;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1 << i);

        Set<BitSet> removedDiffs = new HashSet<>();

        for (int t : removedData) {
            // reset structures
            Arrays.fill(dfHashCodes, initHash);
            for (int i = 0; i < diffBools.length; i++)
                Arrays.fill(diffBools[i], true);

            // update pli
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);

                if (clstId >= pliE.size())                      // new cluster
                    pliE.add(new ArrayList<>());
                else {
                    int mask = ~(1 << (nAttributes - 1 - e));   // existing cluster
                    for (int neighbor : pliE.get(clstId)) {
                        diffBools[neighbor][e] = false;
                        dfHashCodes[neighbor] &= mask;
                    }
                }

                pliE.get(clstId).add(t);
            }

            // update inversePli
            inversePli.set(t, null);

            // generate diff
            for (int i = 0; i < dfHashCodes.length; i++) {
                if (inversePli.get(i) != null)
                    if (dfFreq.addValue(dfHashCodes[i], -1) == 0)
                        removedDiffs.add(Utils.boolArrayToBitSet(diffBools[i]));
            }
        }

        return new ArrayList<>();
    }

    public List<BitSet> getDiffSet() {
        return new ArrayList<>(diffSet);
    }

//    void updateDiffSets3(List<List<List<Integer>>> pli, int nInsertedTuples,
//                         List<Set<Integer>> updatedClusters, List<Integer> insertedClusters, List<BitSet> newDiffSets) {
//
//        long startTime1 = System.nanoTime();
//        boolean[][][] agreeSetsMap = new boolean[nInsertedTuples][nTuples + nInsertedTuples][nAttributes];
//
//        for (int e = 0; e < nAttributes; e++) {
//            for (int c : updatedClusters.get(e)) {
//                List<Integer> clst = pli.get(e).get(c);
//                for (int i = clst.size() - 1; clst.get(i) >= nTuples; i--)
//                    for (int j = 0; clst.get(j) < nTuples; j++)
//                        agreeSetsMap[clst.get(i) - nTuples][clst.get(j)][e] = true;
//            }
//            for (int c = insertedClusters.get(e); c < pli.get(e).size(); c++) {
//                List<Integer> clst = pli.get(e).get(c);
//                for (int i = 1; i < clst.size() - 1; i++)
//                    for (int j = 0; j < i; j++)
//                        agreeSetsMap[clst.get(i) - nTuples][clst.get(j) - nTuples][e] = true;
//            }
//        }
//        long endTime1 = System.nanoTime();
//        System.out.println("updateDiffSets3 runtime 1: " + (endTime1 - startTime1) / 1000000 + "ms");
//
//        long startTime2 = System.nanoTime();
//        for (int i = 0; i < nInsertedTuples; i++) {
//            for (int j = 0; j < nTuples + i; j++) {
//                if (dfFreq.addValue(Utils.boolArrayToInt(agreeSetsMap[i][j]), 1, 0) == 1)
//                    newDiffSets.add(Utils.boolArrayToInverseBitSet(agreeSetsMap[i][j]));
//            }
//        }
//        long endTime2 = System.nanoTime();
//        System.out.println("updateDiffSets3 runtime 2: " + (endTime2 - startTime2) / 1000000 + "ms");
//    }


}
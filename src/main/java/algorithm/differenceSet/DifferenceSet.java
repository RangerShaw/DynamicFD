package algorithm.differenceSet;

import com.koloboke.collect.map.hash.HashIntIntMap;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import util.DataIO;
import util.Utils;

import java.util.*;

public class DifferenceSet {

    // TODO: int nNextTuple, avoid using the same tuple ID

    int nAttributes;

    int nTuples;

    Map<BitSet, Integer> diffSets = new HashMap<>();

    HashIntIntMap dfFreq = HashIntIntMaps.newMutableMap();


    public DifferenceSet() {
    }

    void initiateDataStructure(List<List<Integer>> inversePli) {
        nTuples = inversePli.size();
        nAttributes = inversePli.isEmpty() ? 0 : inversePli.get(0).size();
    }

    public Set<BitSet> generateDiffSets(List<List<Integer>> inversePli) {
        initiateDataStructure(inversePli);

        for (int t1 = 0; t1 < nTuples - 1; t1++) {
            for (int t2 = t1 + 1; t2 < nTuples; t2++) {
                BitSet diffSet = new BitSet(nAttributes);
                for (int e = 0; e < nAttributes; e++)
                    if (!inversePli.get(t1).get(e).equals(inversePli.get(t2).get(e)))
                        diffSet.set(e);
                diffSets.put(diffSet, diffSets.getOrDefault(diffSet, 0) + 1);
            }
        }

        for (Map.Entry<BitSet, Integer> df : diffSets.entrySet())
            dfFreq.addValue(Utils.bitsetToInt(nAttributes, df.getKey()), df.getValue());

        return diffSets.keySet();
    }

    public Set<BitSet> generateDiffSets(List<List<Integer>> inversePli, String diffFp) {
        initiateDataStructure(inversePli);

        diffSets = DataIO.readDiffSetsMap(diffFp);

        for (Map.Entry<BitSet, Integer> df : diffSets.entrySet())
            dfFreq.addValue(Utils.bitsetToInt(nAttributes, df.getKey()), df.getValue());

        return diffSets.keySet();
    }

    public List<BitSet> insertData(List<List<List<Integer>>> pli, List<List<Integer>> inversePli) {
        int[] dfHashCodes = new int[inversePli.size()];
        boolean[][] diffSetsBools = new boolean[inversePli.size()][nAttributes];

        int initHash = 0;
        for (int i = 0; i < nAttributes; i++)
            initHash |= (1 << i);

        List<BitSet> newDiffSets = new ArrayList<>();
        for (int t = nTuples; t < inversePli.size(); t++) {
            // reset structures
            Arrays.fill(dfHashCodes, initHash);
            for (int i = 0; i < t; i++)
                Arrays.fill(diffSetsBools[i], true);        // true iff different

            // update pli
            for (int e = 0; e < nAttributes; e++) {
                List<List<Integer>> pliE = pli.get(e);
                int clstId = inversePli.get(t).get(e);
                if (clstId < pliE.size()) {                     // existing cluster
                    int mask = ~(1 << (nAttributes - 1 - e));
                    for (int neighbor : pliE.get(clstId)) {
                        diffSetsBools[neighbor][e] = false;
                        dfHashCodes[neighbor] &= mask;
                    }
                } else {                                        // new cluster
                    pliE.add(new ArrayList<>());
                }
                pliE.get(clstId).add(t);
            }

            // generate diff
            for (int i = 0; i < t; i++) {
                if (dfFreq.addValue(dfHashCodes[i], 1, 0) == 1)
                    newDiffSets.add(Utils.boolArrayToBitSet(diffSetsBools[i]));
            }
        }

        nTuples = inversePli.size();

        return newDiffSets;
    }

    public List<BitSet> getDiffSets() {
        return new ArrayList<>(diffSets.keySet());
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

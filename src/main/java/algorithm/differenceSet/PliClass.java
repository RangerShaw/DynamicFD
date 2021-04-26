package algorithm.differenceSet;

import java.util.*;
import java.util.stream.Collectors;

public class PliClass {

    public int nTuples;

    public int nAttributes;

    /**
     * pliMap[i]["abc"] -> on attribute [i], which cluster "abc" belongs to
     */
    List<Map<String, Integer>> pliMap = new ArrayList<>();

    /**
     * pli[i]: clusters on attribute[i];
     * pli[i][j]: on attribute[i], what tuples belong to cluster[j];
     * pli[i][j][k]: tuple index
     */
    List<List<List<Integer>>> pli = new ArrayList<>();

    /**
     * inversePli[t][e]: inverse mapping tuple t on attribute e to its cluster ID
     */
    List<List<Integer>> inversePli = new ArrayList<>();

    /**
     * next cluster IDs for new clusters on each attribute
     */
    int[] nextClusterId;


    public PliClass() {
    }

    void initiateDataStructure(List<List<String>> data) {
        nTuples = data.size();
        nAttributes = data.isEmpty() ? 0 : data.get(0).size();

        nextClusterId = new int[nAttributes];

        for (int i = 0; i < nAttributes; i++) {
            pli.add(new ArrayList<>());
            pliMap.add(new HashMap<>());
        }
        for (int t = 0; t < nTuples; t++)
            inversePli.add(new ArrayList<>(nAttributes));
    }

    List<List<List<Integer>>> generatePLI(List<List<String>> data) {
        initiateDataStructure(data);

        for (int e = 0; e < nAttributes; e++) {
            Map<String, Integer> pliMapE = pliMap.get(e);
            List<List<Integer>> pliE = pli.get(e);

            for (int t = 0; t < data.size(); t++) {
                Integer clstId = pliMapE.get(data.get(t).get(e));
                if (clstId == null) {
                    clstId = pliE.size();
                    pliMapE.put(data.get(t).get(e), clstId);
                    pliE.add(new ArrayList<>());
                }

                pliE.get(clstId).add(t);
                inversePli.get(t).add(clstId);
            }

            nextClusterId[e] = pliE.size();
        }

        return pli;
    }

    List<List<List<Integer>>> getPli() {
        return pli;
    }

    public List<List<Integer>> getInversePli() {
        return inversePli;
    }

    void insertData(List<List<String>> insertedData) {
        int offset = nTuples;
        nTuples += insertedData.size();

        for (int i = 0; i < insertedData.size(); i++)
            inversePli.add(new ArrayList<>());

        // pli is untouched and will be updated in DifferenceSet
        for (int e = 0; e < nAttributes; e++) {
            for (int t = 0; t < insertedData.size(); t++) {
                Integer clstId = pliMap.get(e).get(insertedData.get(t).get(e));
                if (clstId == null) {
                    clstId = nextClusterId[e]++;
                    pliMap.get(e).put(insertedData.get(t).get(e), clstId);
                }
                inversePli.get(t + offset).add(clstId);
            }
        }
    }


//    void insertData(List<List<String>> insertedData, List<Set<Integer>> updatedClusters, List<Integer> insertedClusters) {
//        int offset = nTuples;
//        nTuples += insertedData.size();
//        pli.stream().map(List::size).forEach(insertedClusters::add);
//
//        for (int e = 0; e < nAttributes; e++) {
//            updatedClusters.add(new HashSet<>());
//
//            Map<String, Integer> pliMapE = pliMap.get(e);
//            List<List<Integer>> pliE = pli.get(e);
//            int cluster;
//
//            for (int t = 0; t < insertedData.size(); t++) {
//                if (pliMapE.containsKey(insertedData.get(t).get(e))) {
//                    cluster = pliMapE.get(insertedData.get(t).get(e));
//                    updatedClusters.get(e).add(cluster);
//                } else {
//                    cluster = pliE.size();
//                    pliMapE.put(insertedData.get(t).get(e), cluster);
//                    pliE.add(new ArrayList<>());
//                }
//                pliE.get(cluster).add(t + offset);
//            }
//        }
//    }


}

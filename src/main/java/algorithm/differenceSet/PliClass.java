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
     * next tuple ID for new data, to avoid collision
     */
    int nextTupleId;

    public PliClass() {
    }

    void initiateDataStructure(List<List<String>> data) {
        nTuples = data.size();
        nAttributes = data.isEmpty() ? 0 : data.get(0).size();

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
        }

        nextTupleId = nTuples;

        return pli;
    }

    public List<List<List<Integer>>> getPli() {
        return pli;
    }

    public List<List<Integer>> getInversePli() {
        return inversePli;
    }

    public void insertData(List<List<String>> insertedData) {
        for (int i = 0; i < insertedData.size(); i++)
            inversePli.add(new ArrayList<>());

        // update pliMap and inversePli, pli will be updated in DifferenceSet
        for (int e = 0; e < nAttributes; e++) {
            for (int t = 0; t < insertedData.size(); t++) {
                Integer clstId = pliMap.get(e).get(insertedData.get(t).get(e));
                if (clstId == null) {
                    clstId = pliMap.get(e).size();
                    pliMap.get(e).put(insertedData.get(t).get(e), clstId);
                }
                inversePli.get(t + nTuples).add(clstId);
            }
        }

        nTuples += insertedData.size();
        nextTupleId += insertedData.size();
    }

    public void removeData(List<Integer> removedTuples, boolean[] removed) {
        int[] newId = new int[inversePli.size()];

        removedTuples.add(removedTuples.size(), inversePli.size());
        for (int i = 0; i < removedTuples.size() - 1; i++) {
            int l = removedTuples.get(i), r = removedTuples.get(i + 1);
            newId[l] = -1;
            for (int j = l + 1; j < r; j++) {
                int currId = j - i - 1;
                newId[j] = currId;
                inversePli.set(currId, inversePli.get(j));
            }
        }
        removedTuples.remove(removedTuples.size() - 1);

        inversePli.subList(inversePli.size() - removedTuples.size(), inversePli.size()).clear();

        for (List<List<Integer>> pliE : pli) {
            for (List<Integer> clst : pliE) {
                clst.removeIf(i -> removed[i]);
                for (int i = 0; i < clst.size(); i++)
                    if (clst.get(i) > removedTuples.get(0))
                        clst.set(i, newId[clst.get(i)]);
            }
        }

        nTuples -= removedTuples.size();
    }
}

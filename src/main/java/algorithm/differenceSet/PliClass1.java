package algorithm.differenceSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PliClass1 {

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
    List<List<List<Tuple>>> pli = new ArrayList<>();

    /**
     * inversePli[t]: inverse mapping of each cell in tuple t to its cluster ID
     */
    List<Tuple> inversePli = new ArrayList<>();

    /**
     * next cluster IDs for new clusters on each attribute
     */
    int[] nextClusterId;

    public PliClass1() {
    }

    void initiateDataStructure(List<List<String>> data) {
        nTuples = data.size();
        nAttributes = data.isEmpty() ? 0 : data.get(0).size();

        nextClusterId = new int[nAttributes];

        for (int e = 0; e < nAttributes; e++) {
            pli.add(new ArrayList<>());
            pliMap.add(new HashMap<>());
        }
        for (int t = 0; t < nTuples; t++)
            inversePli.add(new Tuple(nAttributes, inversePli.size()));
    }

    List<List<List<Tuple>>> generatePLI(List<List<String>> data) {
        initiateDataStructure(data);

        for (int e = 0; e < nAttributes; e++) {
            Map<String, Integer> pliMapE = pliMap.get(e);
            List<List<Tuple>> pliE = pli.get(e);

            for (int t = 0; t < data.size(); t++) {
                Integer clstId = pliMapE.get(data.get(t).get(e));
                if (clstId == null) {
                    clstId = pliE.size();
                    pliMapE.put(data.get(t).get(e), clstId);
                    pliE.add(new ArrayList<>());
                }

                pliE.get(clstId).add(inversePli.get(t));
                inversePli.get(t).cells[e] = clstId;
            }

            nextClusterId[e] = pliE.size();
        }

        return pli;
    }

    public List<List<List<Tuple>>> getPli() {
        return pli;
    }

    public List<Tuple> getInversePli() {
        return inversePli;
    }

    public void insertData(List<List<String>> insertedData) {
        for (int i = 0; i < insertedData.size(); i++)
            inversePli.add(new Tuple(nAttributes, inversePli.size()));

        // pli is untouched and will be updated in DifferenceSet
        for (int e = 0; e < nAttributes; e++) {
            for (int t = 0; t < insertedData.size(); t++) {
                Integer clstId = pliMap.get(e).get(insertedData.get(t).get(e));
                if (clstId == null) {
                    clstId = nextClusterId[e]++;
                    pliMap.get(e).put(insertedData.get(t).get(e), clstId);
                }
                inversePli.get(t + nTuples).cells[e] = clstId;
            }
        }

        nTuples += insertedData.size();
    }

    public void removeData(List<Integer> removedTupleIds) {
        // pliMap remains unchanged, empty cluster remains there, remove data from inversePli

    }
}

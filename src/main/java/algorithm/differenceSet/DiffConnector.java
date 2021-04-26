package algorithm.differenceSet;

import java.util.*;

public class DiffConnector {

    public int nTuples;

    public int nAttributes;

    PliClass pliClass;

    DifferenceSet differenceSet;

    public DiffConnector() {
        pliClass = new PliClass();
        differenceSet = new DifferenceSet();
    }

    public List<BitSet> generatePliAndDiff(List<List<String>> data) {
        nTuples = data.size();
        nAttributes = data.isEmpty() ? 0 : data.get(0).size();

        pliClass.generatePLI(data);

        differenceSet.generateDiffSets(pliClass.getInversePli());

        return differenceSet.getDiffSets();
    }

    public List<BitSet> generatePliAndDiff(List<List<String>> data, String diffFp) {
        nTuples = data.size();
        nAttributes = data.isEmpty() ? 0 : data.get(0).size();

        pliClass.generatePLI(data);

        differenceSet.generateDiffSets(pliClass.getInversePli(), diffFp);

        return differenceSet.getDiffSets();
    }


    public List<BitSet> getDiffSets() {
        return differenceSet.getDiffSets();
    }

    public List<BitSet> insertData(List<List<String>> insertedData) {
        pliClass.insertData(insertedData);
        return differenceSet.insertData(pliClass.getPli(), pliClass.getInversePli());
    }

//    public List<BitSet> insertData(List<List<String>> insertedData) {
//        List<Set<Integer>> updatedClusters = new ArrayList<>();
//        List<Integer> insertedClusters = new ArrayList<>();
//
//        pliClass.insertData(insertedData, updatedClusters, insertedClusters);
//
//        return differenceSet.insertData(pliClass.getPli(), insertedData.size(), updatedClusters, insertedClusters);
//    }

}

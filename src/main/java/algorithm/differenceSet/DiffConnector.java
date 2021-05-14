package algorithm.differenceSet;

import java.util.*;

public class DiffConnector {

    PliClass pliClass;

    DifferenceSetInterface differenceSet;


    public DiffConnector() {
    }

    void initiateDataStructure(List<List<String>> data) {
        int nAttributes = data.isEmpty() ? 0 : data.get(0).size();
        pliClass = new PliClass();
        //differenceSet = nAttributes <= 32 ? new DifferenceSet() : new DifferenceSet64();
        differenceSet = new DifferenceSet() ;
    }

    public List<Integer> generatePliAndDiff(List<List<String>> data) {
        initiateDataStructure(data);

        pliClass.generatePLI(data);
        differenceSet.generateDiffSet(pliClass.getInversePli());

        return differenceSet.getDiffSet();
    }

    public Map<BitSet, Integer> generatePliAndDiffMap(List<List<String>> data) {
        initiateDataStructure(data);

        pliClass.generatePLI(data);
        return differenceSet.generateDiffSet(pliClass.getInversePli());
    }

    /**
     * read Diff Set from diffFp directly, generate all other structures as usual
     *
     * @param data input data, each tuple must be unique
     */
    public List<Integer> generatePliAndDiff(List<List<String>> data, String diffFp) {
        initiateDataStructure(data);

        pliClass.generatePLI(data);
        differenceSet.generateDiffSet(pliClass.getInversePli(), diffFp);

        return differenceSet.getDiffSet();
    }


    public List<Integer> getDiffSet() {
        return differenceSet.getDiffSet();
    }


    /**
     * @return new Diffs
     */
    public List<Integer> insertData(List<List<String>> insertedData) {
        pliClass.insertData(insertedData);
        return differenceSet.insertData(pliClass.getPli(), pliClass.getInversePli());
    }

    /**
     * @return remain Diffs
     */
    public List<Integer> removeData(List<Integer> removedData, Set<Integer> removedDiffs) {
        removedData.sort(Integer::compareTo);

        boolean[] removed = new boolean[pliClass.getTupleCount()];
        for (int i : removedData)
            removed[i] = true;

        List<Integer> leftDiffs = differenceSet.removeData(pliClass.getPli(), pliClass.getInversePli(), removedData, removed, removedDiffs);

        pliClass.removeData(removedData, removed);

        return leftDiffs;
    }

}

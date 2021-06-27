package algorithm.differenceSet;

import java.util.*;

public class DiffConnector {

    public int nElements;

    PliClass pliClass;

    DifferenceSetInterface differenceSet;


    public DiffConnector() {
    }

    void initiateDataStructure(List<List<String>> data) {
        nElements = data.isEmpty() ? 0 : data.get(0).size();
        pliClass = new PliClass();
        differenceSet = new DifferenceSet64();
        //differenceSet = nElements <= 32 ? new DifferenceSet() : new DifferenceSet64();
    }

    public List<? extends Number> generatePliAndDiff(List<List<String>> data) {
        initiateDataStructure(data);

        pliClass.generatePLI(data);
        differenceSet.generateDiffSet(pliClass.getPli(), pliClass.getInversePli());

        return differenceSet.getDiffSet();
    }

    public Map<BitSet, Long> generatePliAndDiffMap(List<List<String>> data) {
        initiateDataStructure(data);

        pliClass.genPliMapAndInversePli(data);
        return differenceSet.generateDiffSet(pliClass.getPli(), pliClass.getInversePli());
    }

    /**
     * read Diff Set from diffFp directly, generate all other structures as usual
     *
     * @param data input data, each tuple must be unique
     */
    public List<? extends Number> generatePliAndDiff(List<List<String>> data, String diffFp) {
        initiateDataStructure(data);

        pliClass.generatePLI(data);
        differenceSet.generateDiffSet(pliClass.getInversePli(), diffFp);

        return differenceSet.getDiffSet();
    }


    public List<? extends Number> getDiffSet() {
        return differenceSet.getDiffSet();
    }

    public Map<? extends Number, Long> getDiffFreq() {
        return differenceSet.getDiffFreq();
    }

    /**
     * @return new Diffs
     */
    public List<? extends Number> insertData(List<List<String>> insertedData) {
        pliClass.insertData(insertedData);
        return differenceSet.insertData(pliClass.getPli(), pliClass.getInversePli());
    }

    /**
     * @return remain Diffs
     */
    public Set<? extends Number> removeData(List<Integer> removedData) {
        removedData.sort(Integer::compareTo);

        boolean[] removed = new boolean[pliClass.getTupleCount()];
        for (int i : removedData)
            removed[i] = true;

        Set<? extends Number> leftDiffs = differenceSet.removeData(pliClass.getPli(), pliClass.getInversePli(), removedData, removed);

        pliClass.removeData(removedData, removed);

        return leftDiffs;
    }

}

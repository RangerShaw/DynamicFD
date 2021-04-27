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

        return differenceSet.getDiffSet();
    }

    /**
     * read Diff Set from diffFp directly, generate all other structures as usual
     * @param data input data, each tuple must be unique
     */
    public List<BitSet> generatePliAndDiff(List<List<String>> data, String diffFp) {
        nTuples = data.size();
        nAttributes = data.isEmpty() ? 0 : data.get(0).size();

        pliClass.generatePLI(data);

        return differenceSet.generateDiffSets(pliClass.getInversePli(), diffFp);
    }


    public List<BitSet> getDiffSets() {
        return differenceSet.getDiffSet();
    }

    /**
     * @return new Diffs
     */
    public List<BitSet> insertData(List<List<String>> insertedData) {
        pliClass.insertData(insertedData);
        return differenceSet.insertData(pliClass.getPli(), pliClass.getInversePli());
    }

    /**
     * @return remain Diffs
     */
    public List<BitSet> removeData(List<Integer> removedData) {
        pliClass.removeData(removedData);
        return differenceSet.removeData(pliClass.getPli(), pliClass.getInversePli());
    }

}

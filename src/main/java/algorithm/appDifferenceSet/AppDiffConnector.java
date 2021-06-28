package algorithm.appDifferenceSet;

import algorithm.differenceSet.PliClass;
import algorithm.hittingSet.AMMCS.Subset;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppDiffConnector {

    public int nElements;

    PliClass pliClass;

    AppDifferenceSet64 appDifferenceSet;


    public AppDiffConnector() {
    }

    void initiateDataStructure(List<List<String>> data) {
        nElements = data.isEmpty() ? 0 : data.get(0).size();
        pliClass = new PliClass();
        appDifferenceSet = new AppDifferenceSet64();
    }

//    public List<Subset> generatePliAndDiff(List<List<String>> data) {
//        initiateDataStructure(data);
//
//        pliClass.generatePLI(data);
//        appDifferenceSet.generateDiffSet(pliClass.getPli(), pliClass.getInversePli());
//
//        return appDifferenceSet.getDiffSet();
//    }

//    public Map<BitSet, Long> generatePliAndDiffMap(List<List<String>> data) {
//        initiateDataStructure(data);
//
//        pliClass.genPliMapAndInversePli(data);
//        return appDifferenceSet.generateDiffSet(pliClass.getPli(), pliClass.getInversePli());
//    }

    /**
     * read Diff Set from diffFp directly, generate all other structures as usual
     *
     * @param data input data, each tuple must be unique
     */
    public List<Subset> generatePliAndDiff(List<List<String>> data, String diffFp) {
        initiateDataStructure(data);

        pliClass.generatePLI(data);
        appDifferenceSet.generateDiffSet(pliClass.getInversePli(), diffFp);

        return appDifferenceSet.getDiffSet();
    }


    public List<Subset> getDiffSet() {
        return appDifferenceSet.getDiffSet();
    }

}

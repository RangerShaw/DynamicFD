package algorithm.hittingSet.AFDEnum;

import algorithm.hittingSet.MMCS.Mmcs;
import algorithm.hittingSet.Subset;
import algorithm.hittingSet.fdConnectors.FdConnector;

import java.util.*;

public class MmcsFdConnector extends FdConnector {

    /**
     * Mmcs algorithms on different rhs
     */
    List<AFDEnum> MmcsList = new ArrayList<>();

    double maxErr;

    public MmcsFdConnector(int nElements, double maxErr) {
        super(nElements);
        this.maxErr = maxErr;
//        for (int i = 0; i < nElements; i++)
//            MmcsList.add(new AFDEnum(nElements, maxErr));
    }


    /**
     * @param toCover all subsets (different sets) to be covered
     */
    public void initiate(List<BitSet> toCover, HashMap<Subset, Integer> toCoverNum) {
        super.initiate(nElements);
        int allPairNum = 0;
        for (Subset subset : toCoverNum.keySet()){
            allPairNum += toCoverNum.get(subset);
        }
        /**
         * 计算 {1,7,10,13}->0 error rate = 0.004515550777538877
         */
        int all1 = 0;
        for(Subset subset : toCoverNum.keySet()){
            if(subset.elements.get(0) && !subset.elements.get(1) && !subset.elements.get(7) && !subset.elements.get(10)&& !subset.elements.get(10)){
                all1 += toCoverNum.get(subset);
            }
        }
        System.out.println(all1 / (double) allPairNum);
        /**
         * 计算 {1,2,4}->7 error rate = 0.009990244512225611
         */
        all1 = 0;
        for(Subset subset : toCoverNum.keySet()){
            if(subset.elements.get(7) && !subset.elements.get(1) && !subset.elements.get(4) && !subset.elements.get(2)){
                all1 += toCoverNum.get(subset);
            }
        }
        System.out.println(all1 / (double) allPairNum);

        for (int rhs = 0; rhs < nElements; rhs++) {
            System.out.println("  [FdConnector] initiating on rhs " + rhs + "...");
            List<BitSet> diffSets = generateDiffsOnRhs(toCover, rhs);
            MmcsList.add(new AFDEnum(nElements, maxErr, rhs));
            MmcsList.get(rhs).initiate(diffSets, toCoverNum, allPairNum);
            System.out.println(MmcsList.get(rhs).getWalked().size());
            minFDs.add(MmcsList.get(rhs).getMinCoverSets());

        }
    }

}

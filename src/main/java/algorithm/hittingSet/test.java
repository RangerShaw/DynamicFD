package algorithm.hittingSet;


import algorithm.hittingSet.fdConnectors.MmcsFdConnector;
import util.DataIO;

import java.util.*;

public class test {
    public static void main(String[] args) {
        Map<BitSet, Integer> diffSetMap = DataIO.readDiffSetsMap("D:\\Desktop\\DynamicFD\\dataFiles\\letter\\remove\\test.txt");
        HashMap<Subset, Integer> toCoverNum = new HashMap<>();
        List<BitSet> bitSetsToCover = new ArrayList<>();
        for(Map.Entry<BitSet, Integer> df : diffSetMap.entrySet()){
            bitSetsToCover.add((BitSet)df.getKey().clone());
            toCoverNum.put(new Subset(df.getKey()), df.getValue());
        }
        MmcsFdConnector mmcs = new MmcsFdConnector();
        mmcs.initiate(bitSetsToCover);
        List<List<BitSet>> fd = mmcs.getMinFDs();
        int fdRes = 0;
        for(List<BitSet> list : fd){
            System.out.println(list.size());
            fdRes += list.size();
        }
        System.out.println(fdRes);
    }
}

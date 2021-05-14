package algorithm.hittingSet.AFDEnum;

import algorithm.hittingSet.Subset;
import util.DataIO;

import java.util.*;

public class Test {
    public static void main(String[] args) {



        Map<BitSet, Integer> diffSetMap = DataIO.readDiffSetsMap("D:\\Desktop\\DynamicFD\\dataFiles\\letter\\remove\\letter_DS_20000.txt");
        HashMap<Subset, Integer> toCoverNum = new HashMap<>();
        List<BitSet> bitSetsToCover = new ArrayList<>();
        int allPair = 0;
        for(Map.Entry<BitSet, Integer> df : diffSetMap.entrySet()){
            bitSetsToCover.add((BitSet)df.getKey().clone());
            toCoverNum.put(new Subset(df.getKey()), df.getValue());
            allPair += df.getValue();
        }

        HashMap<BitSet, BitSet> AFD = DataIO.readAFD("D:\\Desktop\\DynamicFD\\dataFiles\\letter\\remove\\letterAFD");
        MmcsFdConnector mmcs = new MmcsFdConnector(17,0.01);
        mmcs.initiate(bitSetsToCover, toCoverNum);
        List<List<BitSet>> fd = mmcs.getMinFDs();
        List<Set<BitSet>> fds = new ArrayList<>();
        for(int i = 0; i < fd.size(); ++i){
            fds.add(new HashSet<>(fd.get(i)));
        }
        int fdRes = 0;
        for(List<BitSet> list : fd){
            System.out.println(list.size());
            fdRes += list.size();
        }
        System.out.println(fdRes);

        System.out.println("----------------------over  result ----------------------");
        /**
         * pyro 有些结果并不是最小集合，存在错误，
         * 在error rate = 0.001 中 {1,7,10,13}->0 error rate = 0.004515550777538877
         * 在pyro的结果中不是最小
         *
         * error rate = 0.01 {1, 2, 4}->7 error rate
         */
        for(int i = 0; i < 17; ++i){
            List<BitSet> lhs = fd.get(i);
            for(BitSet bitSet : lhs){
                if(AFD.containsKey(bitSet) && AFD.get(bitSet).get(i)){
                    continue;
                }else{
                    System.out.println(bitSet + "->" + i);
                }
            }
        }

        System.out.println("------------------not in this result -------------------");
        for(BitSet bitSet : AFD.keySet()){
            BitSet rhs = AFD.get(bitSet);
            int next = rhs.nextSetBit(0);
            while(next != -1){
                if(!fds.get(next).contains(bitSet)){
                    System.out.println(bitSet + "->" + next);
                }
                next = rhs.nextSetBit(next + 1);
            }
        }

    }
}

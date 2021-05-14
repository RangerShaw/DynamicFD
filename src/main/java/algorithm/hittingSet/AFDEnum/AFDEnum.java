package algorithm.hittingSet.AFDEnum;

import algorithm.hittingSet.MMCS.MmcsNode;
import algorithm.hittingSet.Subset;
import util.Utils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AFDEnum {
    /**
     *  Max Error Rate For AFD
     */
    private BigDecimal maxError;

    private int curRHS;
    /**
     * all tuple pair Num
     */
    int curtuplePairNum;

    int allTuplePair;

    Set<BitSet> minCheck = new HashSet<>();
    /**
     * number of elements or attributes
     */
    private int nElements;

    /**
     * each node represents a minimal cover set
     */
    private List<AFDEnumNode> coverNodes = new ArrayList<>();

    /**
     * true iff there's an empty subset to cover (which could never be covered).
     * return no cover set if true but walk down without the empty subset
     */
    private boolean hasEmptySubset = false;

    private HashMap<Subset, Boolean> canHit;

    /**
     * record nodes walked to avoid duplication
     */
    private Set<String> walked = new HashSet<>();



    public AFDEnum(int nEle, double maxError, int rhs) {

        nElements = nEle;

        this.maxError = new BigDecimal(maxError);

        this.curRHS = rhs;
    }

    /**
     * @param bitSetsToCover unique BitSets representing Subsets to be covered
     */
    public void initiate(List<BitSet> bitSetsToCover, HashMap<Subset, Integer> toCoverNum, int allPair) {
        hasEmptySubset = bitSetsToCover.stream().anyMatch(BitSet::isEmpty);

        canHit = new HashMap<>();
        List<Subset> subsets = bitSetsToCover.stream().filter(bs -> !bs.isEmpty()).map(Subset::new).collect(Collectors.toList());


        this.allTuplePair = allPair;
        int allTuplePair = 0;
        for (Subset subset : subsets){
            canHit.put(subset, true);
            allTuplePair += toCoverNum.get(subset);
        }
        this.curtuplePairNum = allTuplePair;
        AFDEnumNode initNode = new AFDEnumNode(allPair, curRHS, nElements, subsets, allTuplePair, maxError, toCoverNum);

        walkDown(initNode, coverNodes, toCoverNum);
    }

    /**
     * down from nd on the search tree, find all minimal hitting sets
     */
    void walkDown(AFDEnumNode nd, List<AFDEnumNode> newNodes, HashMap<Subset, Integer> toCoverNum) {
        //System.out.println(walked.size());
//        if(walked.size() == 911){
//            System.out.println();
//        }
        if (walked.contains(nd.hashCode2()) ) return;
        walked.add(nd.hashCode2());

//        if(nd.getElements().get(2) && nd.getElements().get(11)){
//            System.out.println();
//        }
        BigDecimal curErr = new BigDecimal((curtuplePairNum - nd.getCurCoverNum() )/ (double)this.allTuplePair);
        if (maxError.compareTo(curErr) >= 0  && !nd.getElements().get(curRHS) ) {
            //nd.setCanHit(canHit);
            BitSet bitSet = nd.getElements();
            int next = bitSet.nextSetBit(0);
            while (next != - 1){
                bitSet.set(next, false);
                if(minCheck.contains(bitSet)){
                    return;
                }
                bitSet.set(next, true);
                next = bitSet.nextSetBit(next + 1);
            }
//            if(nd.getElements().cardinality() == 5){
//                System.out.println();
//            }

            if(curRHS == 8 && nd.getElements().cardinality() == 2 && nd.getElements().get(0) && nd.getElements().get(1)){
                System.out.println();
            }
            newNodes.add(nd);
            minCheck.add(nd.getElements());

            //System.out.println(nd.getElements() + " " + newNodes.size());
            return;
        }

        // configure cand for child nodes
        /**
         * childCand for cover F, childCandDel for uncover F
         *
         * addCandidates = C, curChooseF = F
         *
         */
        BitSet curChooseF = nd.getAddCandidates(canHit);
        if(curChooseF.cardinality() == nElements || curChooseF.cardinality() == 0){
            return ;
        }
        HashMap<Subset, Boolean> tmpCanHit = new HashMap<>();
        for (Subset subset : canHit.keySet()){
            tmpCanHit.put(subset, canHit.get(subset));
        }
        /**
         * get childcand and addcandidate for cover F
         */
        BitSet childCand = (BitSet)nd.getCand().clone();

        BitSet addCandidates = (BitSet)nd.getCand().clone();

        addCandidates.and(curChooseF);

        childCand.andNot(addCandidates);

        /**
         * get childcanddel for uncover F
         */
        //HashMap<Subset, Boolean> tmpCanHit = nd.getCanHit();

        BitSet childCandDel = (BitSet)nd.getCand().clone();

        childCandDel.andNot(curChooseF);

        //int curCover = nd.getCurCoverNum();
        //nd.updateCanCover( childCandDel);

        AFDEnumNode childNode1 = nd.getChildNodeDel(curChooseF,childCandDel,canHit);
        //System.out.println("------删除 " + curChooseF +  " " + toCoverNum.get(new Subset(curChooseF) ) + " canCover： "  + childNode1.getCanCoverNum() + "  ---------");
        if(childNode1.willCover() && childNode1.isGlobalMinimal()){
            boolean tmp = true;
            for(BitSet bitSet : minCheck){
                BitSet bitSet1 = (BitSet)childNode1.getElements().clone();
                bitSet1.and(bitSet);
                if(bitSet1.equals(bitSet)){
                    tmp = false;
                    break;
                }
            }
//            boolean tmp = true;
//            BitSet bitSet = childNode1.getElements();
//            int next = bitSet.nextSetBit(0);
//            while (next != - 1){
//                bitSet.set(next, false);
//                if(minCheck.contains(bitSet)){
//                    tmp = false;
//                    break;
//                }
//                bitSet.set(next, true);
//                next = bitSet.nextSetBit(next + 1);
//            }
            if(tmp){
                walkDown(childNode1, newNodes, toCoverNum);
            }

        }
        for (Subset subset : tmpCanHit.keySet()){
            canHit.put(subset, tmpCanHit.get(subset));
        }




//        int e = addCandidates.nextSetBit(0);
//        while(e != -1){
//            AFDEnumNode childNode = nd.getChildNode(e, childCand);
//            if (childNode.isGlobalMinimal()) {
//                walkDown(childNode, newNodes, toCoverNum);
//                childCand.set(e);
//            }
//            e = addCandidates.nextClearBit( e + 1);
//        }
        //System.out.println("------ 保留 " + curChooseF + " " + toCoverNum.get(new Subset(curChooseF) ) + "canCover： "  + nd.getCanCoverNum() + " ---------");
        addCandidates.stream().forEach(e -> {
            AFDEnumNode childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                boolean tmp = true;
                for(BitSet bitSet : minCheck){
                    BitSet bitSet1 = (BitSet)childNode.getElements().clone();
                    bitSet1.and(bitSet);
                    if(bitSet1.equals(bitSet)){
                        tmp = false;
                        break;
                    }
                }
//                boolean tmp = true;
//                BitSet bitSet = childNode.getElements();
//                int next = bitSet.nextSetBit(0);
//                while (next != - 1){
//                    bitSet.set(next, false);
//                    if(minCheck.contains(bitSet)){
//                        tmp = false;
//                        break;
//                    }
//                    bitSet.set(next, true);
//                    next = bitSet.nextSetBit(next + 1);
//                }
                if(tmp){
                    walkDown(childNode, newNodes, toCoverNum);
                    childCand.set(e);
                }

            }
        });
    }


    public Set<String> getWalked() {
        return walked;
    }

    public void setWalked(Set<String> walked) {
        this.walked = walked;
    }

    public List<BitSet> getMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(AFDEnumNode::getElements)
                .sorted(Utils.BitsetComparator())
                .collect(Collectors.toList());
    }

}

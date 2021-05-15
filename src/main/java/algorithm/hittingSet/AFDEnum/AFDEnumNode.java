package algorithm.hittingSet.AFDEnum;


import algorithm.hittingSet.Subset;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.*;

public class AFDEnumNode {

    private int nElements;

    /**
     * elements of current node
     */
    private BitSet elements;

    private BitSet cand;

    private BigDecimal maxError;
    /**
     * for canHit the num of true
     */
    private int canCoverNum;

    private int curCoverNum;

    private int allTuplePair;

    private int curTuplePair;

    private HashMap<Subset, Boolean> canHit;

    private HashMap<Subset, Integer> bitsetToPair;

    /**
     * uncovered subsets
     */
    private List<Subset> uncov;

    /**
     * crit[i]: subsets for which element i is crucial
     */
    private ArrayList<ArrayList<Subset>> crit;

    private AFDEnumNode(int nEle,  int allTuplePair, int curTuplePair) {

        nElements = nEle;

        curCoverNum = 0;

        this.allTuplePair = allTuplePair;

        canCoverNum = curTuplePair;

        this.curTuplePair = curTuplePair;



    }

    /**
     * for initiation only
     */
    AFDEnumNode(int allTuplePair, int rhs, int nEle, List<Subset> subsetsToCover, int curTuplePair, BigDecimal minError, HashMap<Subset, Integer> bitsetToPair) {
        this.curTuplePair = curTuplePair;
        nElements = nEle;
        elements = new BitSet(nElements);
        uncov = new ArrayList<>(subsetsToCover);
        crit = new ArrayList<>(nElements);

        cand = new BitSet(nElements);
        cand.set(0, nElements);

        //cand.set(rhs, false);
        for (int i = 0; i < nElements; i++) {
            crit.add(new ArrayList<>());
        }

        curCoverNum = 0;

        this.allTuplePair = allTuplePair;

        canCoverNum = curTuplePair;

        maxError = minError;

        this.bitsetToPair = bitsetToPair;
    }

    public String  hashCode2() {
        return elements.hashCode()+"_"+cand.hashCode();
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof algorithm.hittingSet.MMCS.MmcsNode && ((algorithm.hittingSet.AFDEnum.AFDEnumNode) obj).elements.equals(elements);
    }

    BitSet getElements() {
        return (BitSet) elements.clone();
    }

    BitSet getCand() {
        return cand;
    }

    public HashMap<Subset, Boolean> getCanHit() {
        HashMap<Subset, Boolean> tmp = new HashMap<>();
        for(Subset bitSet : this.canHit.keySet()){
            tmp.put(new Subset((BitSet)bitSet.elements.clone()), this.canHit.get(bitSet));
        }
        return tmp;
    }

    public void setCand(BitSet cand) {
        this.cand = cand;
    }

//    public void setCanHit(HashMap<Subset, Boolean> canHit) {
//        HashMap<Subset, Boolean> tmp = new HashMap<>();
//        for(Subset bitSet : canHit.keySet()){
//            tmp.put(new Subset((BitSet)bitSet.elements.clone()), canHit.get(bitSet));
//        }
//        this.canHit = tmp;
//    }

    public BigDecimal getError() {
        return maxError;
    }

    public void setError(BigDecimal error) {
        this.maxError = error;
    }

    boolean isCover() {
        return uncov.isEmpty();
    }

    public boolean isGlobalMinimal() {

        return elements.stream().noneMatch(e -> crit.get(e).isEmpty());
    }

    /**
     * find an uncovered subset with the optimal intersection with cand,
     * return its intersection with cand
     */
    BitSet getAddCandidates(HashMap<Subset, Boolean> canhit) {
        if(uncov.isEmpty())return new BitSet();
        Comparator<Subset> cmp = Comparator.comparing(sb -> {
            if(!canhit.get(sb)) return nElements;
            BitSet t = ((BitSet) cand.clone());
            t.and(sb.elements);
            return t.cardinality();
        });

        //BitSet C = (BitSet) cand.clone();

        /* different strategies: min may be the fastest */
        //C.and(Collections.min(uncov, cmp).elements);
        // C.and(Collections.max(uncov, cmp).elements);
        // C.and(uncov.get(0).elements);

        try {
            return Collections.min(uncov, cmp).elements;
        }catch (Exception e){
            System.out.println(e.fillInStackTrace());
        }
        return new BitSet();
    }

    void updateCanCover( BitSet childCandDel){
        for(Subset bitSet : uncov){
            boolean canhit = false;
            int next = childCandDel.nextSetBit(0);
            while(next != -1){
                if(bitSet.elements.get(next)){
                    canhit = true;
                    break;
                }
                next = childCandDel.nextSetBit(next + 1);
            }
            if(!canhit){
                canHit.put(bitSet, false);
                canCoverNum -= bitsetToPair.get(bitSet);
            }
        }
    }
    boolean willCover(){
        if(maxError.compareTo( new BigDecimal((curCoverNum - canCoverNum) / (double)allTuplePair)) >= 0){
            return true;
        }else{
            return false;
        }
    }




    void setCanCoverNum(int canCoverNum){
        this.canCoverNum = canCoverNum;
    }

    int getCanCoverNum(){
        return this.canCoverNum;
    }

    public int getCurCoverNum() {
        return curCoverNum;
    }

    public void setCurCoverNum(int curCoverNum) {
        this.curCoverNum = curCoverNum;
    }

    algorithm.hittingSet.AFDEnum.AFDEnumNode getChildNode(int e, BitSet childCand) {
        algorithm.hittingSet.AFDEnum.AFDEnumNode childNode = new algorithm.hittingSet.AFDEnum.AFDEnumNode(nElements, allTuplePair, curTuplePair);
        childNode.cloneContext(childCand, this);
        childNode.setCurCoverNum(childNode.updateContextFromParent(e, this) + childNode.getCurCoverNum());
        return childNode;
    }



    void cloneContext(BitSet outerCand, algorithm.hittingSet.AFDEnum.AFDEnumNode originalNode) {
        elements = (BitSet) originalNode.elements.clone();
        cand = (BitSet) outerCand.clone();

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++) {
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
        }

        curCoverNum = originalNode.getCurCoverNum();
        canCoverNum = originalNode.getCanCoverNum();
        maxError = originalNode.getError();
        bitsetToPair = originalNode.bitsetToPair;
    }
    algorithm.hittingSet.AFDEnum.AFDEnumNode getChildNodeDel(BitSet e, BitSet childCand, HashMap<Subset, Boolean> canHit) {
        algorithm.hittingSet.AFDEnum.AFDEnumNode childNode = new algorithm.hittingSet.AFDEnum.AFDEnumNode(nElements, allTuplePair, curTuplePair);
        childNode.cloneContext(childCand, this);
        childNode.updateContextFromParentDel(childCand, this, canHit);
        return childNode;
    }
    void updateContextFromParentDel(BitSet e, algorithm.hittingSet.AFDEnum.AFDEnumNode parentNode,HashMap<Subset, Boolean> canHit) {
        uncov = new ArrayList<>();

        for(Subset bitSet : parentNode.uncov){
            boolean canhit = false;
            int next = e.nextSetBit(0);
            while(next != -1){
                if(bitSet.elements.get(next)){
                    canhit = true;
                    break;
                }
                next = e.nextSetBit(next + 1);
            }
            if(!canhit){
                canHit.put(bitSet, false);
                canCoverNum -= bitsetToPair.get(bitSet);
            }else{
                uncov.add(bitSet);
            }
        }

    }
    int updateContextFromParent(int e, algorithm.hittingSet.AFDEnum.AFDEnumNode parentNode) {
        uncov = new ArrayList<>();

        int coverNum = 0;
        for (Subset sb : parentNode.uncov) {
            if (sb.hasElement(e)) {
                crit.get(e).add(sb);
                coverNum += bitsetToPair.get(sb);
            }
            else uncov.add(sb);
        }

        elements.stream().forEach(u -> {
            crit.get(u).removeIf(F -> F.hasElement(e));
        });

        elements.set(e);
        return coverNum;
    }

}

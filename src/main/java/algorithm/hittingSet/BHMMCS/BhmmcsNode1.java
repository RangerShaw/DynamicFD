//package algorithm.hittingSet.BHMMCS;
//
//import algorithm.hittingSet.Subset;
//import util.Utils;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//public class BhmmcsNode1 {
//
//    private int nElements;
//
//    /**
//     * elements of current node
//     */
//    int elements;
//
//    private int cand;
//
//    /**
//     * uncovered subsets
//     */
//    private List<Integer> uncov;
//
//    /**
//     * crit[i]: subsets for which element i is crucial
//     */
//    private ArrayList<ArrayList<Integer>> crit;
//
//    List<Integer> redundantEles;
//
//
//    private BhmmcsNode1(int nEle) {
//        nElements = nEle;
//    }
//
//    /**
//     * for initiation only
//     */
//    BhmmcsNode1(int nEle, List<Integer> subsetsToCover) {
//        nElements = nEle;
//        elements = 0;
//        uncov = new ArrayList<>(subsetsToCover);
//
//        cand = 0;
//        for (int i = 0; i < elements; i++)
//            cand |= 1 << i;
//
//        crit = new ArrayList<>(nElements);
//        for (int i = 0; i < nElements; i++)
//            crit.add(new ArrayList<>());
//    }
//
//    @Override
//    public int hashCode() {
//        return elements;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return obj instanceof BhmmcsNode1 && ((BhmmcsNode1) obj).elements == elements;
//    }
//
//    int getElements() {
//        return elements;
//    }
//
//    int getCand() {
//        return cand;
//    }
//
//    boolean isCover() {
//        return uncov.isEmpty();
//    }
//
//    public boolean isGlobalMinimal() {
//        return elements.stream().noneMatch(e -> crit.get(e).isEmpty());
//    }
//
//    /**
//     * find an uncovered subset with the optimal intersection with cand,
//     * return its intersection with cand
//     */
//    int getAddCandidates() {
//        Comparator<Integer> cmp = Comparator.comparing(sb -> Integer.bitCount(cand & sb));
//
//        /* different strategies: min may be the fastest */
//        // return cand & Collections.max(uncov, cmp);
//        // return cand & uncov.get(0);
//        return cand & Collections.min(uncov, cmp);
//    }
//
//    BhmmcsNode1 getChildNode(int e, BitSet childCand) {
//        BhmmcsNode1 childNode = new BhmmcsNode1(nElements);
//        childNode.cloneContextFromParent(childCand, this);
//        childNode.updateContextFromParent(e, this);
//        return childNode;
//    }
//
//    void cloneContextFromParent(BitSet outerCand, BhmmcsNode1 originalNode) {
//        elements = (BitSet) originalNode.elements.clone();
//        cand = (BitSet) outerCand.clone();
//
//        crit = new ArrayList<>(nElements);
//        for (int i = 0; i < nElements; i++) {
//            crit.add(new ArrayList<>(originalNode.crit.get(i)));
//        }
//    }
//
//    void updateContextFromParent(int e, BhmmcsNode1 parentNode) {
//        uncov = new ArrayList<>();
//
//        for (Subset sb : parentNode.uncov) {
//            if (sb.hasElement(e)) crit.get(e).add(sb);
//            else uncov.add(sb);
//        }
//
//        elements.stream().forEach(u -> {
//            crit.get(u).removeIf(F -> F.hasElement(e));
//        });
//
//        elements.set(e);
//    }
//
//    BhmmcsNode1 getParentNode(int e, List<Subset> subsetsWithE) {
//        BhmmcsNode1 parentNode = new BhmmcsNode1(nElements);
//        parentNode.updateContextFromChild(e, this, subsetsWithE);
//        return parentNode;
//    }
//
//    BhmmcsNode1 removeEle(int e, List<Subset> subsetsWithE) {
//        elements.clear(e);
//
//        cand = (BitSet) elements.clone();
//        cand.flip(0, nElements);
//
//        crit.get(e).clear();
//        for (Subset sb : subsetsWithE) {
//            if (!sb.isCoveredBy(elements)) uncov.add(sb);
//            else {
//                int critCover = sb.getCritCover(elements);
//                if (critCover >= 0) crit.get(critCover).add(sb);
//            }
//        }
//
//        return this;
//    }
//
//    void updateContextFromChild(int e, BhmmcsNode1 originalNode, List<Subset> subsetsWithE) {
//        elements = (BitSet) originalNode.elements.clone();
//        elements.clear(e);
//
//        cand = (BitSet) elements.clone();
//        cand.flip(0, nElements);
//
//        uncov = new ArrayList<>();
//
//        crit = new ArrayList<>(nElements);
//        for (int i = 0; i < nElements; i++)
//            crit.add(new ArrayList<>(originalNode.crit.get(i)));
//        for (Subset sb : subsetsWithE) {
//            int critCover = sb.getCritCover(elements);
//            if (critCover >= 0) crit.get(critCover).add(sb);
//        }
//
//        redundantEles = originalNode.redundantEles.stream().filter(i -> i != e).collect(Collectors.toList());
//    }
//
//    void insertSubsets(List<Subset> newSubsets) {
//        cand = (BitSet) elements.clone();
//        cand.flip(0, nElements);
//
//        for (Subset newSb : newSubsets) {
//            BitSet intersec = (BitSet) elements.clone();
//            intersec.and(newSb.elements);
//
//            if (intersec.isEmpty()) uncov.add(newSb);
//            if (intersec.cardinality() == 1) crit.get(intersec.nextSetBit(0)).add(newSb);
//        }
//    }
//
//    void removeSubsets(List<Subset> remainedSets, Set<Subset> removedSets) {
//        cand = (BitSet) elements.clone();
//        cand.flip(0, nElements);
//
//        redundantEles = new ArrayList<>();
//
//        elements.stream().forEach(e -> {
//            crit.get(e).removeIf(removedSets::contains);
//            if (crit.get(e).isEmpty()) redundantEles.add(e);
//        });
//    }
//
//}

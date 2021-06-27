//package algorithm.hittingSet.BHMMCS;
//
//import algorithm.hittingSet.NumSet;
//
//import java.util.*;
//
//public class BhmmcsNode {
//
//    int elements;
//
//    int cand;
//
//
//    private List<Integer> uncov;
//
//    List<List<Integer>> crit;
//
//
//    private BhmmcsNode() {
//    }
//
//    /**
//     * for initiation only
//     */
//    BhmmcsNode(int nElements, List<Integer> setsToCover) {
//        elements = 0;
//        uncov = new ArrayList<>(setsToCover);
//
//        cand = 0;
//        for (int i = 0; i < nElements; i++)
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
//        return obj instanceof BhmmcsNode && ((BhmmcsNode) obj).elements == elements;
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
//        return NumSet.indicesOfOnes(elements).stream().noneMatch(e -> crit.get(e).isEmpty());
//    }
//
//    /**
//     * find an uncovered int with the optimal intersection with cand,
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
//    BhmmcsNode getChildNode(int e, int childCand) {
//        BhmmcsNode childNode = new BhmmcsNode();
//        childNode.cloneContextFromParent(childCand, this);
//        childNode.updateContextFromParent(e, this);
//        return childNode;
//    }
//
//    void cloneContextFromParent(int outerCand, BhmmcsNode originalNode) {
//        elements = originalNode.elements;
//        cand = outerCand;
//
//        crit = new ArrayList<>();
//        for (int i = 0; i < originalNode.crit.size(); i++)
//            crit.add(new ArrayList<>(originalNode.crit.get(i)));
//    }
//
//    void updateContextFromParent(int e, BhmmcsNode parentNode) {
//        uncov = new ArrayList<>();
//
//        for (int sb : parentNode.uncov) {
//            if ((sb & (1 << e)) != 0) crit.get(e).add(sb);
//            else uncov.add(sb);
//        }
//
//        for (int u : NumSet.indicesOfOnes(elements))
//            crit.get(u).removeIf(F -> (F & (1 << e)) != 0);
//
//        elements |= 1 << e;
//    }
//
//    void removeElesAndSubsets(int newElements, Set<Integer> removedSets, List<Integer> removedEles, List<Integer> revealed) {
//        elements = newElements;
//
//        cand = (~elements) & Bhmmcs.elementsMask;
//
//        for (int e : NumSet.indicesOfOnes(elements))
//            crit.get(e).removeAll(removedSets);
//
//        for (int e : removedEles)
//            crit.get(e).clear();
//
//        for (int sb : revealed) {
//            int critCover = getCritCover(sb);
//            if (critCover == -1) uncov.add(sb);
//            else if (critCover >= 0) crit.get(critCover).add(sb);
//        }
//    }
//
//    boolean insertSubsets(List<Integer> newSubsets, Set<Integer> rmvMinSubsets) {
//        List<Integer> eles = NumSet.indicesOfOnes(elements);
//
//        for (int e : eles)
//            crit.get(e).removeAll(rmvMinSubsets);
//
//        for (int newSb : newSubsets) {
//            int critCover = getCritCover(newSb);
//            if (critCover == -1) uncov.add(newSb);
//            else if (critCover >= 0) crit.get(critCover).add(newSb);
//        }
//
//        return eles.stream().noneMatch(e -> crit.get(e).isEmpty());
//    }
//
//    void resetCand() {
//        cand = ~elements & Bhmmcs.elementsMask;
//    }
//
//    /**
//     * @return -1 iff sb is NOT covered by this node; -2 iff sb is covered by at least 2 elements
//     */
//    int getCritCover(int sb) {
//        int and = sb & elements;
//        if (and == 0) return -1;
//
//        int ffs = Integer.numberOfTrailingZeros(and);
//        return and == (1 << ffs) ? ffs : -2;
//    }
//}

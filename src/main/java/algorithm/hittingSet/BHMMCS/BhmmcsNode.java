package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.NumSet;

import java.util.*;

public class BhmmcsNode {

    private int nElements;

    /**
     * elements of current node
     */
    int elements;

    int cand;

    /**
     * uncovered sets
     */
    private List<Integer> uncov;

    /**
     * crit[i]: sets for which element i is crucial
     */
    ArrayList<ArrayList<Integer>> crit;


    private BhmmcsNode(int nEle) {
        nElements = nEle;
    }

    /**
     * for initiation only
     */
    BhmmcsNode(int nEle, List<Integer> setsToCover) {
        nElements = nEle;
        elements = 0;
        uncov = new ArrayList<>(setsToCover);

        cand = 0;
        for (int i = 0; i < nElements; i++)
            cand |= 1 << i;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>());
    }

    @Override
    public int hashCode() {
        return elements;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BhmmcsNode && ((BhmmcsNode) obj).elements == elements;
    }

    int getElements() {
        return elements;
    }

    int getCand() {
        return cand;
    }

    boolean isCover() {
        return uncov.isEmpty();
    }

    public boolean isGlobalMinimal() {
        return NumSet.indicesOfOnes(elements).stream().noneMatch(e -> crit.get(e).isEmpty());
    }

    /**
     * find an uncovered int with the optimal intersection with cand,
     * return its intersection with cand
     */
    int getAddCandidates() {
        Comparator<Integer> cmp = Comparator.comparing(sb -> Integer.bitCount(cand & sb));

        /* different strategies: min may be the fastest */
        // return cand & Collections.max(uncov, cmp);
        // return cand & uncov.get(0);
        return cand & Collections.min(uncov, cmp);
    }

    BhmmcsNode getChildNode(int e, int childCand) {
        BhmmcsNode childNode = new BhmmcsNode(nElements);
        childNode.cloneContextFromParent(childCand, this);
        childNode.updateContextFromParent(e, this);
        return childNode;
    }

    void cloneContextFromParent(int outerCand, BhmmcsNode originalNode) {
        elements = originalNode.elements;
        cand = outerCand;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
    }

    void updateContextFromParent(int e, BhmmcsNode parentNode) {
        uncov = new ArrayList<>();

        for (int sb : parentNode.uncov) {
            if ((sb & (1 << e)) != 0) crit.get(e).add(sb);
            else uncov.add(sb);
        }

        for (int u : NumSet.indicesOfOnes(elements))
            crit.get(u).removeIf(F -> (F & (1 << e)) != 0);

        elements |= 1 << e;
    }

    BhmmcsNode getParentNode(int e, List<Integer> setsWithE) {
        BhmmcsNode parentNode = new BhmmcsNode(nElements);
        parentNode.updateContextFromChild(e, this, setsWithE);
        return parentNode;
    }

    void removeEles(int e, List<Integer> setsWithE) {
        if ((elements & (1 << e)) == 0) return;

        elements &= ~(1 << e);

        cand = (~elements) & Bhmmcs.elementsMask;

        crit.get(e).clear();

        for (int sb : setsWithE) {
            int critCover = getCritCover(sb);
            if (critCover == -1) uncov.add(sb);
            else if (critCover >= 0) crit.get(critCover).add(sb);
        }
    }

    void removeEles(int newElements, List<Integer> removedEles, List<List<Integer>> subsetParts) {
        //if (newElements == elements) return;

        elements = newElements;

        cand = (~elements) & Bhmmcs.elementsMask;

        Set<Integer> potentialCrit = new HashSet<>();
        for (int e : removedEles) {
            crit.get(e).clear();
            potentialCrit.addAll(subsetParts.get(e));
        }

        for (int sb : potentialCrit) {
            int critCover = getCritCover(sb);
            if (critCover == -1) uncov.add(sb);
            else if (critCover >= 0) crit.get(critCover).add(sb);
        }
    }

    void removeElesAndSubsets(int newElements, Set<Integer> removedSets, List<Integer> removedEles, List<List<Integer>> subsetParts) {
         elements = newElements;

        cand = (~elements) & Bhmmcs.elementsMask;

        for (int e : NumSet.indicesOfOnes(elements))
            crit.get(e).removeAll(removedSets);

        Set<Integer> potentialCrit = new HashSet<>();
        for (int e : removedEles) {
            crit.get(e).clear();
            potentialCrit.addAll(subsetParts.get(e));
        }

        for (int sb : potentialCrit) {
            int critCover = getCritCover(sb);
            if (critCover == -1) uncov.add(sb);
            else if (critCover >= 0) crit.get(critCover).add(sb);
        }
    }

    void updateContextFromChild(int e, BhmmcsNode originalNode, List<Integer> setsWithE) {
        elements = originalNode.elements;
        elements &= ~(1 << e);

        cand = (~elements) & Bhmmcs.elementsMask;

        uncov = new ArrayList<>();

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
        for (int sb : setsWithE) {
            int critCover = getCritCover(sb);
            if (critCover >= 0) crit.get(critCover).add(sb);
        }
    }

    boolean insertSubsets(List<Integer> newSubsets, Set<Integer> rmvMinSubsets) {
        List<Integer> eles = NumSet.indicesOfOnes(elements);

        for (int e : eles)
            crit.get(e).removeAll(rmvMinSubsets);

        for (int newSb : newSubsets) {
            int critCover = getCritCover(newSb);
            if (critCover == -1) uncov.add(newSb);
            else if (critCover >= 0) crit.get(critCover).add(newSb);
        }

        return eles.stream().noneMatch(e -> crit.get(e).isEmpty());
    }

    List<Integer> removeSubsets(Set<Integer> removedSets) {
        cand = ~elements & Bhmmcs.elementsMask;

        List<Integer> redundantEles = new ArrayList<>();

        for (int e : NumSet.indicesOfOnes(elements)) {
            crit.get(e).removeIf(removedSets::contains);
            if (crit.get(e).isEmpty()) redundantEles.add(e);
        }

        return redundantEles;
    }

    int getParentElements(List<Integer> redundantEles) {
        int parentElements = elements;
        for (int i : redundantEles)
            parentElements &= ~(1 << i);
        return parentElements;
    }

    int getParentElements(int redundantEles) {
        return elements & ~redundantEles;
    }

    void resetCand() {
        cand = ~elements & Bhmmcs.elementsMask;
    }

    /**
     * @return -1 iff sb is NOT covered by this node; -2 iff sb is covered by at least 2 elements
     */
    int getCritCover(int sb) {
        int and = sb & elements;
        if (and == 0) return -1;

        int ffs = Integer.numberOfTrailingZeros(and);
        return and == (1 << ffs) ? ffs : -2;
    }
}

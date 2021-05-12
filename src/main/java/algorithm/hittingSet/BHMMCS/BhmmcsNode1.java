package algorithm.hittingSet.BHMMCS;

import util.IntSet;

import java.util.*;
import java.util.stream.Collectors;

public class BhmmcsNode1 {

    private int nElements;

    /**
     * elements of current node
     */
    int elements;

    private int cand;

    /**
     * uncovered ints
     */
    private List<Integer> uncov;

    /**
     * crit[i]: ints for which element i is crucial
     */
    private ArrayList<ArrayList<Integer>> crit;

    List<Integer> redundantEles;


    private BhmmcsNode1(int nEle) {
        nElements = nEle;
    }

    /**
     * for initiation only
     */
    BhmmcsNode1(int nEle, List<Integer> intsToCover) {
        nElements = nEle;
        elements = 0;
        uncov = new ArrayList<>(intsToCover);

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
        return obj instanceof BhmmcsNode1 && ((BhmmcsNode1) obj).elements == elements;
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
        return IntSet.indicesOfOnes(elements).stream().noneMatch(e -> crit.get(e).isEmpty());
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

    BhmmcsNode1 getChildNode(int e, int childCand) {
        BhmmcsNode1 childNode = new BhmmcsNode1(nElements);
        childNode.cloneContextFromParent(childCand, this);
        childNode.updateContextFromParent(e, this);
        return childNode;
    }

    void cloneContextFromParent(int outerCand, BhmmcsNode1 originalNode) {
        elements = originalNode.elements;
        cand = outerCand;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++) {
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
        }
    }

    void updateContextFromParent(int e, BhmmcsNode1 parentNode) {
        uncov = new ArrayList<>();

        for (int sb : parentNode.uncov) {
            if ((sb & (1 << e)) != 0) crit.get(e).add(sb);
            else uncov.add(sb);
        }

        for (int u : IntSet.indicesOfOnes(elements))
            crit.get(u).removeIf(F -> (F & (1 << e)) != 0);

        elements |= 1 << e;
    }

    BhmmcsNode1 getParentNode(int e, List<Integer> intsWithE) {
        BhmmcsNode1 parentNode = new BhmmcsNode1(nElements);
        parentNode.updateContextFromChild(e, this, intsWithE);
        return parentNode;
    }

    BhmmcsNode1 removeEle(int e, List<Integer> intsWithE) {
        elements &= ~(1 << e);

        cand = (~elements) & Bhmmcs1.elementsMask;

        crit.get(e).clear();
        for (int sb : intsWithE) {
            if ((sb & elements) == 0) uncov.add(sb);
            else {
                int critCover = getCritCover(sb);
                if (critCover >= 0) crit.get(critCover).add(sb);
            }
        }

        return this;
    }

    void updateContextFromChild(int e, BhmmcsNode1 originalNode, List<Integer> intsWithE) {
        elements = originalNode.elements;
        elements &= ~(1 << e);

        cand = (~elements) & Bhmmcs1.elementsMask;

        uncov = new ArrayList<>();

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
        for (int sb : intsWithE) {
            int critCover = getCritCover(sb);
            if (critCover >= 0) crit.get(critCover).add(sb);
        }

        redundantEles = originalNode.redundantEles.stream().filter(i -> i != e).collect(Collectors.toList());
    }

    void insertSubsets(List<Integer> newSubsets) {
        cand = ~elements & Bhmmcs1.elementsMask;

        for (int newSb : newSubsets) {
            int critCover = getCritCover(newSb);
            if (critCover == -1) uncov.add(newSb);
            else if (critCover >= 0) crit.get(critCover).add(newSb);
        }
    }

    void removeSubsets(Set<Integer> removedSets) {
        cand = ~elements & Bhmmcs1.elementsMask;

        redundantEles = new ArrayList<>();

        for (int e : IntSet.indicesOfOnes(elements)) {
            crit.get(e).removeIf(removedSets::contains);
            if (crit.get(e).isEmpty()) redundantEles.add(e);
        }
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

package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.NumSet;

import java.util.*;
import java.util.stream.Collectors;

public class BhmmcsNode64 {

    private int nElements;

    /**
     * elements of current node
     */
    long elements;

    long cand;

    /**
     * uncovered ints
     */
    private List<Long> uncov;

    /**
     * crit[i]: ints for which element i is crucial
     */
    ArrayList<ArrayList<Long>> crit;

    List<Integer> redundantEles;


    private BhmmcsNode64(int nEle) {
        nElements = nEle;
    }

    /**
     * for initiation only
     */
    BhmmcsNode64(int nEle, List<Long> intsToCover) {
        nElements = nEle;
        elements = 0;
        uncov = new ArrayList<>(intsToCover);

        cand = 0;
        for (int i = 0; i < nElements; i++)
            cand |= 1L << i;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>());
    }

    @Override
    public int hashCode() {
        // TODO: avoid hashCode
        return (int) elements;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BhmmcsNode64 && ((BhmmcsNode64) obj).elements == elements;
    }

    long getElements() {
        return elements;
    }

    long getCand() {
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
    long getAddCandidates() {
        Comparator<Long> cmp = Comparator.comparing(sb -> Long.bitCount(cand & sb));

        /* different strategies: min may be the fastest */
        // return cand & Collections.max(uncov, cmp);
        // return cand & uncov.get(0);
        return cand & Collections.min(uncov, cmp);
    }

    BhmmcsNode64 getChildNode(int e, long childCand) {
        BhmmcsNode64 childNode = new BhmmcsNode64(nElements);
        childNode.cloneContextFromParent(childCand, this);
        childNode.updateContextFromParent(e, this);
        return childNode;
    }

    void cloneContextFromParent(long outerCand, BhmmcsNode64 originalNode) {
        elements = originalNode.elements;
        cand = outerCand;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
    }

    void updateContextFromParent(int e, BhmmcsNode64 parentNode) {
        uncov = new ArrayList<>();

        for (long sb : parentNode.uncov) {
            if ((sb & (1L << e)) != 0) crit.get(e).add(sb);
            else uncov.add(sb);
        }

        for (int u : NumSet.indicesOfOnes(elements))
            crit.get(u).removeIf(F -> (F & (1L << e)) != 0);

        elements |= 1L << e;
    }

    BhmmcsNode64 getParentNode(int e, List<Long> intsWithE) {
        BhmmcsNode64 parentNode = new BhmmcsNode64(nElements);
        parentNode.updateContextFromChild(e, this, intsWithE);
        return parentNode;
    }

    void removeEle(int e, List<Long> intsWithE) {
        elements &= ~(1L << e);

        cand = (~elements) & Bhmmcs64.elementsMask;

        crit.get(e).clear();
        for (long sb : intsWithE) {
            if ((sb & elements) == 0) uncov.add(sb);
            else {
                int critCover = getCritCover(sb);
                if (critCover >= 0) crit.get(critCover).add(sb);
            }
        }
    }

    void updateContextFromChild(int e, BhmmcsNode64 originalNode, List<Long> intsWithE) {
        elements = originalNode.elements;
        elements &= ~(1L << e);

        cand = (~elements) & Bhmmcs64.elementsMask;

        uncov = new ArrayList<>();

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
        for (long sb : intsWithE) {
            int critCover = getCritCover(sb);
            if (critCover >= 0) crit.get(critCover).add(sb);
        }

        redundantEles = originalNode.redundantEles.stream().filter(i -> i != e).collect(Collectors.toList());
    }

    void insertSubsets(List<Long> newSubsets, Set<Long> rmvMinSubsets) {
        cand = ~elements & Bhmmcs64.elementsMask;

        for (long newSb : newSubsets) {
            int critCover = getCritCover(newSb);
            if (critCover == -1) uncov.add(newSb);
            else if (critCover >= 0) crit.get(critCover).add(newSb);
        }

        for (int e : NumSet.indicesOfOnes(elements))
            crit.get(e).removeAll(rmvMinSubsets);
    }

    void removeSubsets(Set<Long> removedSets) {
        cand = ~elements & Bhmmcs64.elementsMask;

        redundantEles = new ArrayList<>();

        for (int e : NumSet.indicesOfOnes(elements)) {
            crit.get(e).removeIf(removedSets::contains);
            if (crit.get(e).isEmpty()) redundantEles.add(e);
        }
    }

    /**
     * @return -1 iff sb is NOT covered by this node; -2 iff sb is covered by at least 2 elements
     */
    int getCritCover(long sb) {
        long and = sb & elements;
        if (and == 0) return -1;

        int ffs = Long.numberOfTrailingZeros(and);
        return and == (1L << ffs) ? ffs : -2;
    }
}

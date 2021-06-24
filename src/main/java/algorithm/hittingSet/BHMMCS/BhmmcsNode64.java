package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.NumSet;

import java.util.*;

public class BhmmcsNode64 {

    private int nElements;

    long elements;

    long cand;


    private List<Long> uncov;

    ArrayList<ArrayList<Long>> crit;


    private BhmmcsNode64(int nEle) {
        nElements = nEle;
    }

    /**
     * for initiation only
     */
    BhmmcsNode64(int nEle, List<Long> setsToCover) {
        nElements = nEle;
        elements = 0;
        uncov = new ArrayList<>(setsToCover);

        cand = 0;
        for (int i = 0; i < nElements; i++)
            cand |= 1L << i;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>());
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

    void removeEle(long newElements, List<Integer> removedEles, List<List<Long>> subsetParts) {
        if (newElements == elements) return;

        elements = newElements;

        cand = (~elements) & Bhmmcs64.elementsMask;

        Set<Long> potentialCrit = new HashSet<>();
        for (int e : removedEles) {
            crit.get(e).clear();
            potentialCrit.addAll(subsetParts.get(e));
        }

        for (long sb : potentialCrit) {
            int critCover = getCritCover(sb);
            if (critCover == -1) uncov.add(sb);
            else if (critCover >= 0) crit.get(critCover).add(sb);
        }
    }

    boolean insertSubsets(List<Long> newSubsets, Set<Long> rmvMinSubsets) {
        List<Integer> eles = NumSet.indicesOfOnes(elements);

        for (int e : eles)
            crit.get(e).removeAll(rmvMinSubsets);

        for (long newSb : newSubsets) {
            int critCover = getCritCover(newSb);
            if (critCover == -1) uncov.add(newSb);
            else if (critCover >= 0) crit.get(critCover).add(newSb);
        }

        return eles.stream().noneMatch(e -> crit.get(e).isEmpty());
    }

    List<Integer> removeSubsets(Set<Long> removedSets) {
        cand = ~elements & Bhmmcs64.elementsMask;

        List<Integer> redundantEles = new ArrayList<>();

        for (int e : NumSet.indicesOfOnes(elements)) {
            crit.get(e).removeIf(removedSets::contains);
            if (crit.get(e).isEmpty()) redundantEles.add(e);
        }

        return redundantEles;
    }

    void removeElesAndSubsets(long newElements, Set<Long> removedSets, List<Integer> removedEles, List<Long> revealed) {
        elements = newElements;

        cand = (~elements) & Bhmmcs64.elementsMask;

        for (int e : NumSet.indicesOfOnes(elements))
            crit.get(e).removeAll(removedSets);

        for (int e : removedEles)
            crit.get(e).clear();

        for (long sb : revealed) {
            int critCover = getCritCover(sb);
            if (critCover == -1) uncov.add(sb);
            else if (critCover >= 0) crit.get(critCover).add(sb);
        }
    }


    long getParentElements(List<Integer> redundantEles) {
        long parentElements = elements;
        for (int i : redundantEles)
            parentElements &= ~(1L << i);
        return parentElements;
    }

    long getParentElements(long redundantEles) {
        return elements & ~redundantEles;
    }

    void resetCand() {
        cand = ~elements & Bhmmcs64.elementsMask;
    }

    /**
     * @return -1 iff sb is NOT covered by this node; -2 iff sb is covered by at least 2 elements
     */
    int getCritCover(long sb) {
        long and = sb & elements;
        if (and == 0L) return -1;

        int ffs = Long.numberOfTrailingZeros(and);
        return and == (1L << ffs) ? ffs : -2;
    }
}

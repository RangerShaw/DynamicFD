package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.NumSet;

import java.util.*;

public class BhmmcsNode64 {

    long elements;

    long cand;


    private List<Long> uncov;

    List<List<Long>> crit;


    private BhmmcsNode64() {
    }

    /**
     * for initiation only
     */
    BhmmcsNode64(int nEle, List<Long> setsToCover) {
        elements = 0;
        uncov = new ArrayList<>(setsToCover);

        cand = Bhmmcs64.elementsMask;

        crit = new ArrayList<>(nEle);
        for (int i = 0; i < nEle; i++)
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
        int e = 0;
        long ele = elements;
        while (ele > 0) {
            if ((ele & 1) != 0L && crit.get(e).isEmpty()) return false;
            e++;
            ele >>>= 1;
        }
        return true;
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
        BhmmcsNode64 childNode = new BhmmcsNode64();
        childNode.cloneContextFromParent(childCand, this);
        childNode.updateContextFromParent(e, this);
        return childNode;
    }

    void cloneContextFromParent(long outerCand, BhmmcsNode64 originalNode) {
        elements = originalNode.elements;
        cand = outerCand;

        crit = new ArrayList<>();
        for (int i = 0; i < originalNode.crit.size(); i++)
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

    void resetCand() {
        cand = ~elements & Bhmmcs64.elementsMask;
    }

    /**
     * @return -1 if sb is NOT covered by this node; -2 if sb is covered by at least 2 elements
     */
    int getCritCover(long sb) {
        long and = sb & elements;
        if (and == 0L) return -1;

        int ffs = Long.numberOfTrailingZeros(and);
        return and == (1L << ffs) ? ffs : -2;
    }
}

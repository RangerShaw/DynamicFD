package algorithm.hittingSet.AMMCS;

import algorithm.hittingSet.BHMMCS.Bhmmcs64;
import algorithm.hittingSet.NumSet;

import java.util.*;

public class AmmcsNode64 {

    long elements;

    long nError;

    long cand;


    List<Subset> uncov;

    List<List<Subset>> crit;

    boolean[] canHit;


    private AmmcsNode64() {
    }

    /**
     * for initiation only
     */
    AmmcsNode64(int nElements, List<Subset> setsToCover) {
        elements = 0L;
        uncov = new ArrayList<>(setsToCover);

        cand = Ammcs64.elementsMask;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>());

        canHit = new boolean[setsToCover.size()];
        Arrays.fill(canHit, true);
    }

    long getCand() {
        return cand;
    }

    boolean isCover() {
        return nError <= Ammcs64.nMaxError;
    }

    public boolean isGlobalMinimal() {
        for (int e : NumSet.indicesOfOnes(elements)) {
            long newError = nError;
            for (Subset set : crit.get(e))
                newError += set.count;
            if (newError <= Ammcs64.nMaxError) return false;
        }
        return true;
    }

    /**
     * find an uncovered int with the optimal intersection with cand,
     * return its intersection with cand
     */
    long getAddCandidates() {
        Comparator<Subset> cmp = Comparator.comparing(set -> Long.bitCount(cand & set.set));

        /* different strategies: min may be the fastest */
        // return cand & Collections.max(uncov, cmp);
        // return cand & uncov.get(0);
        return cand & Collections.min(uncov, cmp).set;
    }

    AmmcsNode64 getChildNode(int e, long childCand) {
        AmmcsNode64 childNode = new AmmcsNode64();
        childNode.cloneContextFromParent(childCand, this);
        childNode.updateContextFromParent(e, this);
        return childNode;
    }

    void cloneContextFromParent(long outerCand, AmmcsNode64 originalNode) {
        elements = originalNode.elements;
        cand = outerCand;

        crit = new ArrayList<>();
        for (int i = 0; i < originalNode.crit.size(); i++)
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
    }

    void updateContextFromParent(int e, AmmcsNode64 parentNode) {
        uncov = new ArrayList<>();

        for (Subset sb : parentNode.uncov) {
            if ((sb.set & (1L << e)) != 0) crit.get(e).add(sb);
            else uncov.add(sb);
        }

        for (int u : NumSet.indicesOfOnes(elements))
            crit.get(u).removeIf(F -> (F.set & (1L << e)) != 0);

        elements |= 1L << e;
    }

    void resetCand() {
        cand = ~elements & Ammcs64.elementsMask;
    }

}

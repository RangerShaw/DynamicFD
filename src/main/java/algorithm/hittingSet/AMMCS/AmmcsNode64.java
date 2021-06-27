package algorithm.hittingSet.AMMCS;

import algorithm.hittingSet.BHMMCS.Bhmmcs64;
import algorithm.hittingSet.NumSet;

import java.util.*;

public class AmmcsNode64 {

    long nError;

    long elements;

    long cand;


    List<Subset> uncov;

    List<List<Subset>> crit;

    List<Boolean> canHit;


    private AmmcsNode64() {
    }

    /**
     * for initiation only
     */
    AmmcsNode64(int nElements, List<Subset> setsToCover, long nError) {
        this.nError = nError;

        elements = 0L;
        uncov = new ArrayList<>(setsToCover);

        cand = Ammcs64.elementsMask;

        crit = new ArrayList<>(nElements);
        for (int i = 0; i < nElements; i++)
            crit.add(new ArrayList<>());

        canHit = new ArrayList<>(uncov.size());
        for (int i = 0; i < uncov.size(); i++)
            canHit.add(true);
    }

    long getCand() {
        return cand;
    }

    boolean isCover(long nMaxError) {
        return nError <= nMaxError;
    }

    public boolean isGlobalMinimal(long nMaxError) {
        for (int e : NumSet.indicesOfOnes(elements)) {
            long newError = nError;
            for (Subset set : crit.get(e))
                newError += set.count;
            if (newError <= nMaxError) return false;
        }
        return true;
    }

    public boolean allCrit() {
        int e = 0;
        long ele = elements;
        while (ele > 0L) {
            if ((ele & 1) != 0L && crit.get(e).isEmpty()) return false;
            e++;
            ele >>>= 1;
        }
        return true;
    }

    Long chooseF() {
        Long F = null;
        int nInter = -1;

        for (int i = 0; i < uncov.size(); i++) {
            if (!canHit.get(i)) continue;
            int n = Long.bitCount(cand & uncov.get(i).set);
            if (nInter < n) {
                nInter = n;
                F = uncov.get(i).set;
            }
        }

        return F;
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

    AmmcsNode64 getChildNode(long childCand) {
        AmmcsNode64 childNode = new AmmcsNode64();

        childNode.elements = elements;
        childNode.cand = childCand;
        childNode.nError = nError;
        childNode.uncov = new ArrayList<>(uncov);

        childNode.crit = new ArrayList<>();
        for (int i = 0; i < crit.size(); i++)
            childNode.crit.add(new ArrayList<>(crit.get(i)));

        childNode.canHit = new ArrayList<>(canHit.size());
        for (int i = 0; i < canHit.size(); i++) {
            if (canHit.get(i) && (uncov.get(i).set & childCand) != 0) childNode.canHit.add(true);
            else childNode.canHit.add(false);
        }

        return childNode;
    }

    boolean willCover(long nMaxError) {
        long minNError = nError;
        for (Subset sb : uncov) {
            if ((sb.set & cand) != 0) {
                minNError -= sb.count;
                if (minNError <= nMaxError) return true;
            }
        }
        return minNError <= nMaxError;
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
        nError = originalNode.nError;

        crit = new ArrayList<>();
        for (int i = 0; i < originalNode.crit.size(); i++)
            crit.add(new ArrayList<>(originalNode.crit.get(i)));
    }

    void updateContextFromParent(int e, AmmcsNode64 parentNode) {
        uncov = new ArrayList<>();
        canHit = new ArrayList<>();

        for (int i=0; i<parentNode.uncov.size(); i++) {
            Subset sb = parentNode.uncov.get(i);
            if ((sb.set & (1L << e)) != 0) {
                crit.get(e).add(sb);
                nError -= sb.count;
            } else {
                uncov.add(sb);
                canHit.add(parentNode.canHit.get(i));
            }
        }

        for (int u : NumSet.indicesOfOnes(elements))
            crit.get(u).removeIf(F -> (F.set & (1L << e)) != 0);

        elements |= 1L << e;
    }

    void resetCand() {
        cand = ~elements & Ammcs64.elementsMask;
    }

}

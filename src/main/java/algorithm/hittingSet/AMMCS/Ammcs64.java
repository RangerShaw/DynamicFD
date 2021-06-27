package algorithm.hittingSet.AMMCS;

import algorithm.hittingSet.BHMMCS.BhmmcsNode64;
import algorithm.hittingSet.NumSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Ammcs64 {

    private final int nElements;

    static long elementsMask;

    long nMaxError;


    List<AmmcsNode64> coverNodes;


    public Ammcs64(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1L << i;
    }

    public void initiate(List<Subset> subsets, double threshold) {
        long nSubsets =subsets.stream().map(sb -> sb.count).reduce(0L, Long::sum);
        nMaxError = (long) threshold * nSubsets;
        coverNodes = walkDown(new AmmcsNode64(nElements, subsets, nSubsets));
    }


    List<AmmcsNode64> walkDown(AmmcsNode64 root) {
        Set<Long> walked = new HashSet<>();
        List<AmmcsNode64> newCoverNodes = new ArrayList<>();

        walkDown(root, newCoverNodes, walked);

        return newCoverNodes;
    }

    void walkDown(AmmcsNode64 nd, List<AmmcsNode64> newNodes, Set<Long> walked) {
        if (!walked.add(nd.elements)) return;

        if (nd.isCover(nMaxError) && nd.isGlobalMinimal(nMaxError)) {
            nd.resetCand();
            newNodes.add(nd);
            return;
        }

        Long F = nd.chooseF();
        if (F == null) return;

        // 1. doesn't hit F
        long childCand1 = nd.cand & ~F;
        AmmcsNode64 childNode1 = nd.getChildNode(childCand1);

        if (childNode1.willCover(nMaxError)) walkDown(childNode1, newNodes, walked);

        // 2. hit F
        long addCandidates = nd.cand & F;
        long childCand = nd.cand & ~addCandidates;

        for (int e : NumSet.indicesOfOnes(addCandidates)) {
            AmmcsNode64 childNode = nd.getChildNode(e, childCand);
            if (childNode.allCrit()) {
                walkDown(childNode, newNodes, walked);
                childCand |= 1L << e;
            }
        }
    }

    public List<Long> getMinCoverSets() {
        return coverNodes.stream().map(nd -> nd.elements).collect(Collectors.toList());
    }

}

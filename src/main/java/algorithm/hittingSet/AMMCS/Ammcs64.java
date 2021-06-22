package algorithm.hittingSet.AMMCS;

import algorithm.hittingSet.NumSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ammcs64 {

    private final int nElements;

    static long elementsMask;

    static long nMaxError;

    /**
     * true iff there's an empty int to cover (which could never be covered).
     * return no cover set if true but walk down without the empty set
     */
    boolean hasEmptySubset;

    List<List<Long>> minSubsetParts;

    List<AmmcsNode64> coverNodes;


    public Ammcs64(int nEle, long MaxError) {
        nElements = nEle;
        nMaxError = MaxError;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1L << i;
    }

    public void initiate(List<Subset> subsets) {
        if (NumSet.removeEmptySubSet(subsets)) hasEmptySubset = true;

        for (int i = 0; i < nElements; i++)
            minSubsetParts.add(new ArrayList<>());

        coverNodes = walkDown(new AmmcsNode64(nElements, subsets));
    }


    List<AmmcsNode64> walkDown(AmmcsNode64 root) {
        Set<Long> walked = new HashSet<>();
        List<AmmcsNode64> newCoverNodes = new ArrayList<>();

        walkDown(root, newCoverNodes, walked);

        return newCoverNodes;
    }

    void walkDown(AmmcsNode64 nd, List<AmmcsNode64> newNodes, Set<Long> walked) {
        if (!walked.add(nd.elements)) return;

        if (nd.isCover() && nd.isGlobalMinimal()) {
            nd.resetCand();
            newNodes.add(nd);
            return;
        }

        if(nd.uncov.isEmpty()) return;

        // configure cand for child nodes
        long childCand = nd.cand;
        long addCandidates = nd.getAddCandidates();
        childCand &= ~(addCandidates);

        for (int e : NumSet.indicesOfOnes(addCandidates)) {
            AmmcsNode64 childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes, walked);
                childCand |= 1L << e;
            }
        }
    }

}

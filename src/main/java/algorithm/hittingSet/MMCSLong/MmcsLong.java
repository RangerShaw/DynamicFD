package algorithm.hittingSet.MMCSLong;

import algorithm.hittingSet.NumSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * original MMCS algorithm for discovering minimal hitting sets
 */
public class MmcsLong {

    int nElements;

    static long elementsMask;

    private List<MmcsLongNode> coverNodes = new ArrayList<>();

    /**
     * true iff there's an empty subset to cover (which could never be covered).
     * return no cover set if true but walk down without the empty subset
     */
    private boolean hasEmptySubset = false;


    public MmcsLong(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1L << i;
    }

    public void initiate(List<Long> subsets) {
        if (NumSet.removeEmptyLongSetUnsorted(subsets)) hasEmptySubset = true;
        // minSubsets = NumSet.findMinLongSets(subsets);
        coverNodes = walkDown(new MmcsLongNode(nElements, subsets));
    }

    List<MmcsLongNode> walkDown(MmcsLongNode root) {
        List<MmcsLongNode> newCoverNodes = new ArrayList<>();

        walkDown(root, newCoverNodes);

        return newCoverNodes;
    }

    void walkDown(MmcsLongNode nd, List<MmcsLongNode> newNodes) {
        if (nd.isCover()) {
            nd.resetCand(elementsMask);
            newNodes.add(nd);
            return;
        }

        // configure cand for child nodes
        long childCand = nd.cand;
        long addCandidates = nd.getAddCandidates();
        childCand &= ~(addCandidates);

        for (int e : NumSet.indicesOfOnes(addCandidates)) {
            MmcsLongNode childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes);
                childCand |= 1L << e;
            }
        }
    }

    public List<Long> getMinCoverSets() {
        return coverNodes.stream().map(MmcsLongNode::getElements).collect(Collectors.toList());

//        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
//                .map(MmcsLongNode::getElements)
//                .collect(Collectors.toList());
    }

}

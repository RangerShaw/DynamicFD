package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.NumSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bidirectional Hybrid MMCS algorithm
 * that supports inserting and deleting difference set
 */
public class Bhmmcs64 {

    private final int nElements;

    static long elementsMask;


    /**
     * true iff there's an empty int to cover (which could never be covered).
     * return no cover set if true but walk down without the empty set
     */
    boolean hasEmptySubset;

    List<Long> minSubsets;


    List<BhmmcsNode64> coverNodes;


    public Bhmmcs64(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1L << i;
    }

    public void initiate(List<Long> subsets) {
        if (NumSet.removeEmptyLongSet(subsets)) hasEmptySubset = true;

        minSubsets = NumSet.findMinLongSets(subsets);

        coverNodes = walkDown(new BhmmcsNode64(nElements, minSubsets));
    }


    List<BhmmcsNode64> walkDown(BhmmcsNode64 root) {
        Set<Long> walked = new HashSet<>();
        List<BhmmcsNode64> newCoverNodes = new ArrayList<>();

        walkDown(root, newCoverNodes, walked);

        return newCoverNodes;
    }

    void walkDown(BhmmcsNode64 nd, List<BhmmcsNode64> newNodes, Set<Long> walked) {
        if (!walked.add(nd.elements)) return;

        if (nd.isCover()) {
            nd.resetCand();
            newNodes.add(nd);
            return;
        }

        // configure cand for child nodes
        long childCand = nd.getCand();
        long addCandidates = nd.getAddCandidates();
        childCand &= ~(addCandidates);

        for (int e : NumSet.indicesOfOnes(addCandidates)) {
            BhmmcsNode64 childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes, walked);
                childCand |= 1L << e;
            }
        }
    }


    public void insertSubsets(List<Long> insertedSubsets) {
        if (NumSet.removeEmptyLongSet(insertedSubsets)) hasEmptySubset = true;

        List<Long> newMinSubsets = new ArrayList<>();
        Set<Long> rmvMinSubsets = new HashSet<>();
        minSubsets = NumSet.findMinLongSets(nElements, minSubsets, insertedSubsets, newMinSubsets, rmvMinSubsets);

        List<BhmmcsNode64> coverNodes1 = new ArrayList<>();
        for (BhmmcsNode64 prevNode : coverNodes) {
            if (prevNode.insertSubsets(newMinSubsets, rmvMinSubsets))
                coverNodes1.add(prevNode);
        }

        coverNodes = walkDown(coverNodes1);
    }

    List<BhmmcsNode64> walkDown(List<BhmmcsNode64> oldCoverNodes) {
        Set<Long> walked = new HashSet<>();
        List<BhmmcsNode64> newCoverNodes = new ArrayList<>();

        for (BhmmcsNode64 oldNode : oldCoverNodes)
            walkDown(oldNode, newCoverNodes, walked);

        return newCoverNodes;
    }


    public void removeSubsets(List<Long> leftSubsets, List<Long> rmvdSubsets) {
        if (NumSet.removeEmptyLongSet(leftSubsets)) hasEmptySubset = true;
        if (NumSet.removeEmptyLongSet(rmvdSubsets)) hasEmptySubset = false;

        // 1 find all min removed subsets from minSubsets and update
        List<Long> minRmvdSubsets = new ArrayList<>();
        minSubsets = NumSet.findRemovedMinLongSets(rmvdSubsets, minSubsets, minRmvdSubsets);

        Set<Long> minRemoved = new HashSet<>(minRmvdSubsets);

        // 2 find all min exposed subsets in leftSubsets and update
        List<Long> minExposedSets = NumSet.findMinExposedLongSets(minRmvdSubsets, leftSubsets);

        minSubsets.addAll(minExposedSets);
        NumSet.sortLongSets(nElements, minSubsets);

        // 3 remove affected vertices from nodes
        coverNodes = walkUp(minRmvdSubsets, minRemoved);

        // 4 find all coverNode that intersect with minRemoved and re-walk
        coverNodes = walkDown(coverNodes);
    }

    List<BhmmcsNode64> walkUp(List<Long> minRmvdSubsets, Set<Long> removedSets) {
        long removeCand = 0;
        for (long minRmvdSubset : minRmvdSubsets)
            removeCand |= minRmvdSubset;

        List<Long> revealed = new ArrayList<>();
        for (long set : minSubsets) {
            if ((set & removeCand) != 0) revealed.add(set);
        }

        Set<Long> walked = new HashSet<>();
        List<BhmmcsNode64> newCoverNodes = new ArrayList<>(coverNodes.size());

        List<Integer> removedEles = NumSet.indicesOfOnes(removeCand);
        for (BhmmcsNode64 nd : coverNodes) {
            long parentElements = nd.elements & ~removeCand;
            if (walked.add(parentElements)) {
                nd.removeElesAndSubsets(parentElements, removedSets, removedEles, revealed);
                newCoverNodes.add(nd);
            }
        }

        return newCoverNodes;
    }


    public void removeSubsetsRestart(List<Long> leftSubsets) {
        initiate(leftSubsets);
    }

    public List<Long> getSortedMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode64::getElements)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Long> getMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode64::getElements)
                .collect(Collectors.toList());
    }

}

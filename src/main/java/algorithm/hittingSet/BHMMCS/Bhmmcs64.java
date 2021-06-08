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

    static long  elementsMask;


    /**
     * true iff there's an empty int to cover (which could never be covered).
     * return no cover set if true but walk down without the empty set
     */
    boolean hasEmptySubset;

    List<Long> minSubsets;

    List<List<Long>> minSubsetParts;

    List<BhmmcsNode64> coverNodes;


    public Bhmmcs64(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1L << i;
    }

    public void initiate(List<Long> subsets) {
        if (NumSet.removeEmptyLongSet(subsets)) hasEmptySubset = true;

        minSubsets = NumSet.findMinLongSets(subsets);

        minSubsetParts = new ArrayList<>();
        for (int i = 0; i < nElements; i++)
            minSubsetParts.add(new ArrayList<>());
        for (long sb : minSubsets)
            for (int e : NumSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

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

        for (long sb : newMinSubsets)
            for (int e : NumSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

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
        NumSet.removeEmptyLongSet(rmvdSubsets);

        // 1 find all min removed subsets from minSubsets and update
        List<Long> minRmvdSubsets = NumSet.findRemovedMinLongSets(rmvdSubsets, minSubsets);
        Set<Long> minRemoved = new HashSet<>(minRmvdSubsets);

        minSubsets.removeAll(minRemoved);
        for (List<Long> minSubsetPart : minSubsetParts)
            minSubsetPart.removeAll(minRemoved);

        // 2 find all min exposed subsets in leftSubsets and update
        List<Long> minExposedSets = NumSet.findMinExposedLongSets(minRmvdSubsets, leftSubsets);

        for (long sb : minExposedSets)
            for (int e : NumSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

        minSubsets.addAll(minExposedSets);
        NumSet.sortLongSets(nElements, minSubsets);

        // 3 remove subsets from nodes' crit and walk up if some crit is empty
        coverNodes = removeSubsetsFromNodes(minRemoved);

        // 4 find all coverNode that intersect with minRemoved and re-walk
        coverNodes = rewalk(minRmvdSubsets);
    }

    List<BhmmcsNode64> removeSubsetsFromNodes(Set<Long> minRemoved) {
        List<BhmmcsNode64> newCoverNodes = new ArrayList<>(coverNodes.size());
        Set<Long> walked = new HashSet<>();

        for (BhmmcsNode64 nd : coverNodes) {
            List<Integer> redundantEles = nd.removeSubsets(minRemoved);
            long parentElements = nd.getParentElements(redundantEles);
            if (walked.add(parentElements)) {
                nd.removeEle(parentElements, redundantEles, minSubsetParts);
                newCoverNodes.add(nd);
            }
        }

        return newCoverNodes;
    }

    List<BhmmcsNode64> rewalk(List<Long> minRmvdSubsets) {
        // remove elements appearing in minRmvdSubsets
        List<BhmmcsNode64> newCoverNodes = new ArrayList<>();

        int removeCand = 0;
        for (Long minRmvdSubset : minRmvdSubsets)
            removeCand |= minRmvdSubset;
        List<Integer> removeEles = NumSet.indicesOfOnes(removeCand);

        // re-walk down
        Set<Long> walked = new HashSet<>();
        for (BhmmcsNode64 nd : coverNodes) {
            long parentElements = nd.getParentElements(removeCand);
            if (walked.add(parentElements)) {
                nd.removeEle(parentElements, removeEles, minSubsetParts);
                newCoverNodes.add(nd);
            }
        }

        return walkDown(newCoverNodes);
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

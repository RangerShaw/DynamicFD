package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.NumSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bidirectional Hybrid MMCS algorithm
 * that supports inserting and deleting difference set
 */
public class Bhmmcs {

    private final int nElements;

    static int elementsMask;


    /**
     * true iff there's an empty int to cover (which could never be covered).
     * return no cover set if true but walk down without the empty set
     */
    boolean hasEmptySubset;

    List<Integer> minSubsets;

    List<List<Integer>> minSubsetParts;

    List<BhmmcsNode> coverNodes;


    public Bhmmcs(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1 << i;
    }

    public void initiate(List<Integer> subsets) {
        if (NumSet.removeEmptySubset(subsets)) hasEmptySubset = true;

        minSubsets = NumSet.findMinIntSets(subsets);

        minSubsetParts = new ArrayList<>();
        for (int i = 0; i < nElements; i++)
            minSubsetParts.add(new ArrayList<>());
        for (int sb : minSubsets)
            for (int e : NumSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

        coverNodes = walkDown(new BhmmcsNode(nElements, minSubsets));
    }


    List<BhmmcsNode> walkDown(BhmmcsNode root) {
        Set<Integer> walked = new HashSet<>();
        List<BhmmcsNode> newCoverNodes = new ArrayList<>();

        walkDown(root, newCoverNodes, walked);

        return newCoverNodes;
    }

    void walkDown(BhmmcsNode nd, List<BhmmcsNode> newNodes, Set<Integer> walked) {
        if (!walked.add(nd.elements)) return;

        if (nd.isCover()) {
            nd.resetCand();
            newNodes.add(nd);
            return;
        }

        // configure cand for child nodes
        int childCand = nd.getCand();
        int addCandidates = nd.getAddCandidates();
        childCand &= ~(addCandidates);

        for (int e : NumSet.indicesOfOnes(addCandidates)) {
            BhmmcsNode childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes, walked);
                childCand |= 1 << e;
            }
        }
    }


    public void insertSubsets(List<Integer> insertedSubsets) {
        if (NumSet.removeEmptySubset(insertedSubsets)) hasEmptySubset = true;

        List<Integer> newMinSubsets = new ArrayList<>();
        Set<Integer> rmvMinSubsets = new HashSet<>();
        minSubsets = NumSet.findMinIntSets(nElements, minSubsets, insertedSubsets, newMinSubsets, rmvMinSubsets);

        for (int sb : newMinSubsets)
            for (int e : NumSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

        List<BhmmcsNode> coverNodes1 = new ArrayList<>();
        for (BhmmcsNode prevNode : coverNodes) {
            if (prevNode.insertSubsets(newMinSubsets, rmvMinSubsets))
                coverNodes1.add(prevNode);
        }

        coverNodes = walkDown(coverNodes1);
    }

    List<BhmmcsNode> walkDown(List<BhmmcsNode> oldCoverNodes) {
        Set<Integer> walked = new HashSet<>();
        List<BhmmcsNode> newCoverNodes = new ArrayList<>();

        for (BhmmcsNode oldNode : oldCoverNodes)
            walkDown(oldNode, newCoverNodes, walked);

        return newCoverNodes;
    }


    public void removeSubsets(List<Integer> leftSubsets, List<Integer> rmvdSubsets) {
        if (NumSet.removeEmptySubset(leftSubsets)) hasEmptySubset = true;
        NumSet.removeEmptySubset(rmvdSubsets);

        // 1 find all min removed subsets from minSubsets and update
        List<Integer> minRmvdSubsets = NumSet.findRemovedMinSubsets(rmvdSubsets, minSubsets);
        Set<Integer> minRemoved = new HashSet<>(minRmvdSubsets);

        minSubsets.removeAll(minRemoved);
        for (List<Integer> minSubsetPart : minSubsetParts)
            minSubsetPart.removeAll(minRemoved);

        // 2 find all min exposed subsets in leftSubsets and update
        List<Integer> minExposedSets = NumSet.findMinExposedSets(minRmvdSubsets, leftSubsets);

        for (int sb : minExposedSets)
            for (int e : NumSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

        minSubsets.addAll(minExposedSets);
        NumSet.sortIntSets(nElements, minSubsets);

        // 3 remove subsets from nodes' crit and walk up if some crit is empty
        coverNodes = removeSubsetsFromNodes(minRemoved);

        // 4 find all coverNode that intersect with minRemoved and re-walk
        coverNodes = rewalk(minRmvdSubsets);
    }

    List<BhmmcsNode> removeSubsetsFromNodes(Set<Integer> minRemoved) {
        List<BhmmcsNode> newCoverNodes = new ArrayList<>();
        Set<Integer> walked = new HashSet<>();

        for (BhmmcsNode nd : coverNodes) {
            List<Integer> redundantEles = nd.removeSubsets(minRemoved);
            int parentElements = nd.getParentElements(redundantEles);
            if (walked.add(parentElements)) {
                nd.removeEle(parentElements, redundantEles, minSubsetParts);
                newCoverNodes.add(nd);
            }
        }

        return newCoverNodes;
    }

    List<BhmmcsNode> rewalk(List<Integer> minRmvdSubsets) {
        // remove elements appearing in minRmvdSubsets
        List<BhmmcsNode> newCoverNodes = new ArrayList<>();

        int removeCand = 0;
        for (Integer minRmvdSubset : minRmvdSubsets)
            removeCand |= minRmvdSubset;
        List<Integer> removeEles = NumSet.indicesOfOnes(removeCand);

        // re-walk down
        Set<Integer> walked = new HashSet<>();
        for (BhmmcsNode nd : coverNodes) {
            int parentElements = nd.getParentElements(removeCand);
            if (walked.add(parentElements)) {
                nd.removeEle(parentElements, removeEles, minSubsetParts);
                newCoverNodes.add(nd);
            }
        }

        return walkDown(newCoverNodes);
    }


    List<BhmmcsNode> rewalk(List<Integer> minRmvdSubsets, boolean[] hasExposed) {
        List<BhmmcsNode> newCoverNodes = new ArrayList<>();

        int removeCand = 0;
        for (int i = 0; i < minRmvdSubsets.size(); i++)
            // TODO: may miss some covers
            //if (hasExposed[i])
            removeCand |= minRmvdSubsets.get(i);
        List<Integer> removeEles = NumSet.indicesOfOnes(removeCand);

        Set<Integer> walked = new HashSet<>();
        for (BhmmcsNode nd : coverNodes) {
            int parentElements = nd.getParentElements(removeCand);
            if (walked.add(parentElements)) {
                nd.removeEle(parentElements, removeEles, minSubsetParts);
                newCoverNodes.add(nd);
            }
        }

        return walkDown(newCoverNodes);
    }

    public void removeSubsetsRestart(List<Integer> leftSubsets) {
        initiate(leftSubsets);
    }

    public List<Integer> getSortedMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode::getElements)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Integer> getMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode::getElements)
                .collect(Collectors.toList());
    }

}

package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.IntSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bidirectional Hybrid MMCS algorithm
 * that supports inserting and deleting difference set
 */
public class Bhmmcs {

    /**
     * number of elements or attributes
     */
    private final int nElements;

    static int elementsMask;

    /**
     * each node represents a minimal cover set
     */
    private List<BhmmcsNode> coverNodes = new ArrayList<>();

    /**
     * true iff there's an empty int to cover (which could never be covered).
     * return no cover set if true but walk down without the empty int
     */
    private boolean hasEmptySubset = false;

    /**
     * record nodes walked to avoid duplication
     */
    private Set<Integer> walked = new HashSet<>();

    List<List<Integer>> minSubsetParts = new ArrayList<>();

    List<Integer> minSubsets = new ArrayList<>();


    public Bhmmcs(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1 << i;
    }

    /**
     * @param subsets unique ints representing ints to be covered
     */
    public void initiate(List<Integer> subsets) {
        removeEmptySubset(subsets, false);

        minSubsets = initMinSubsets(subsets);

        BhmmcsNode initNode = new BhmmcsNode(nElements, minSubsets);

        walkDown(initNode, coverNodes);

        for (int i = 0; i < nElements; i++)
            minSubsetParts.add(new ArrayList<>());
        for (int sb : minSubsets)
            for (int e : IntSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);
    }

    List<Integer> initMinSubsets(List<Integer> subsets) {
        // subsets are already sorted
        List<Integer> res = new ArrayList<>();
        boolean[] notMin = new boolean[subsets.size()];
        int[] cardinalities = subsets.stream().mapToInt(Integer::bitCount).toArray();

        for (int i = 0; i < subsets.size(); i++) {
            if (notMin[i]) continue;
            res.add(subsets.get(i));
            for (int j = subsets.size() - 1; j > i; j--) {
                if (cardinalities[i] >= cardinalities[j]) break;
                if (!notMin[j] && IntSet.isSubset(subsets.get(i), subsets.get(j))) notMin[j] = true;
            }
        }

        return res;
    }

    List<Integer> genNewMinSubsets(List<Integer> newSubsets, Set<Integer> rmvMinSubsets) {
        // newSubsets are already sorted
        List<Integer> newMinSubsets = new ArrayList<>();    // min subsets of new subsets
        List<Integer> allMinSubsets = new ArrayList<>();    // min subsets of old and new subsets

        boolean[] notMinNew = new boolean[newSubsets.size()];
        boolean[] notMinOld = new boolean[minSubsets.size()];
        int[] cardinalities = newSubsets.stream().mapToInt(Integer::bitCount).toArray();

        int i = 0, j = 0;
        for (int car = 1; car <= nElements; car++) {
            if (i == minSubsets.size() && j == newSubsets.size()) break;

            for (; i < minSubsets.size() && Integer.bitCount(minSubsets.get(i)) == car; i++) {   // use old min to filter new min
                if (notMinOld[i]) {
                    rmvMinSubsets.add(minSubsets.get(i));
                    continue;
                }
                allMinSubsets.add(minSubsets.get(i));
                for (int k = newSubsets.size() - 1; k >= 0 && car < cardinalities[k]; k--)
                    if (!notMinNew[k] && IntSet.isSubset(minSubsets.get(i), newSubsets.get(k))) notMinNew[k] = true;
            }

            for (; j < newSubsets.size() && cardinalities[j] == car; j++) {                      // use new min to filter old and new min
                if (notMinNew[j]) continue;
                int sbj = newSubsets.get(j);
                allMinSubsets.add(sbj);
                newMinSubsets.add(sbj);
                for (int k = minSubsets.size() - 1; k >= 0 && car < Integer.bitCount(minSubsets.get(k)); k--)
                    if (!notMinOld[k] && IntSet.isSubset(sbj, minSubsets.get(k))) notMinOld[k] = true;
                for (int k = newSubsets.size() - 1; k >= 0 && car < cardinalities[k]; k--)
                    if (!notMinNew[k] && IntSet.isSubset(sbj, newSubsets.get(k))) notMinNew[k] = true;
            }
        }

        minSubsets = allMinSubsets;
        return newMinSubsets;
    }

    /**
     * @param insertedSubsets unique ints representing newly inserted ints to be covered
     */
    public void insertSubsets(List<Integer> insertedSubsets) {
        walked.clear();

        removeEmptySubset(insertedSubsets, false);

        Set<Integer> rmvMinSubsets = new HashSet<>();
        List<Integer> newMinSubsets = genNewMinSubsets(insertedSubsets, rmvMinSubsets);

        for (int sb : newMinSubsets)
            for (int e : IntSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

        for (BhmmcsNode prevNode : coverNodes)
            prevNode.insertSubsets(newMinSubsets, rmvMinSubsets);

        List<BhmmcsNode> newCoverNodes = new ArrayList<>();
        for (BhmmcsNode prevNode : coverNodes)
            walkDown(prevNode, newCoverNodes);

        coverNodes = newCoverNodes;
    }

    /**
     * down from nd on the search tree, find all minimal hitting sets
     */
    void walkDown(BhmmcsNode nd, List<BhmmcsNode> newNodes) {
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

        for (int e : IntSet.indicesOfOnes(addCandidates)) {
            BhmmcsNode childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes);
                childCand |= 1 << e;
            }
        }
    }

    List<Integer> genMinRemovedSubsets(List<Integer> removedSubsets) {
        Set<Integer> removed = new HashSet<>(removedSubsets);
        List<Integer> minRmvdSubsets = new ArrayList<>();

        for (Integer minSubset : minSubsets)
            if (removed.contains(minSubset)) minRmvdSubsets.add(minSubset);

        return minRmvdSubsets;
    }

    /**
     * @param leftSubsets unique ints representing remained ints after removal
     */
    public void removeSubsets(List<Integer> leftSubsets, List<Integer> rmvdSubsets) {
        walked.clear();

        removeEmptySubset(leftSubsets, false);
        removeEmptySubset(rmvdSubsets, true);

        // 1 find all min removed subsets and update
        long startTime = System.nanoTime();

        List<Integer> minRmvdSubsets = genMinRemovedSubsets(rmvdSubsets);
        Set<Integer> minRemoved = new HashSet<>(minRmvdSubsets);

        minSubsets.removeAll(minRemoved);
        for (List<Integer> minSubsetPart : minSubsetParts)
            minSubsetPart.removeAll(minRemoved);

        // 2 find all min exposed subsets in leftSubsets and update
        boolean[] hasExposed = new boolean[minRmvdSubsets.size()];
        List<Integer> minExposedSets = genMinExposedSubsets(minRmvdSubsets, leftSubsets, hasExposed);

        for (int sb : minExposedSets) {
            minSubsets.add(sb);
            for (int e : IntSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);
        }

        minSubsets = minSubsets.stream().distinct().collect(Collectors.toList());
        IntSet.sortIntSets(nElements, minSubsets);

        // 3 remove subsets from nodes' crit and walk up if some crit is empty
        coverNodes = removeSubsetsFromNodes(minRemoved);
        //System.out.println("time 3: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // 4 find all coverNode that intersect with minRemoved and re-walk
        startTime = System.nanoTime();
        coverNodes = rewalk(minRmvdSubsets, hasExposed);
        // System.out.println("time 4: " + ((System.nanoTime() - startTime) / 1000000) + "ms");
    }

    List<BhmmcsNode> removeSubsetsFromNodes(Set<Integer> minRemoved) {
        List<BhmmcsNode> newCoverNodes = new ArrayList<>();
        Set<Integer> existNodes = new HashSet<>();

        for (BhmmcsNode nd : coverNodes) {
            List<Integer> redundantEles = nd.removeSubsets(minRemoved);
            if (existNodes.add(nd.getParentElements(redundantEles))) {
                for (int e : redundantEles)
                    nd.removeEle(e, minSubsetParts.get(e));
                newCoverNodes.add(nd);
            }
        }

        return newCoverNodes;
    }

    List<BhmmcsNode> rewalk(List<Integer> minRmvdSubsets, boolean[] hasExposed) {
        // remove elements appearing in minRmvdSubsets that have exposed subsets
        List<BhmmcsNode> newCoverNodes1 = new ArrayList<>();

        int removeCand = 0;
        for (int i = 0; i < minRmvdSubsets.size(); i++)
            if (hasExposed[i]) removeCand |= minRmvdSubsets.get(i);
        List<Integer> removeEles = IntSet.indicesOfOnes(removeCand);

        Set<Integer> existNodes = new HashSet<>();
        for (BhmmcsNode nd : coverNodes) {
            if (existNodes.add(nd.getParentElements(removeCand))) {
                for (int e : removeEles)
                    nd.removeEle(e, minSubsetParts.get(e));
                newCoverNodes1.add(nd);
            }
        }

        // walk down
        walked.clear();
        List<BhmmcsNode> newCoverNodes2 = new ArrayList<>();
        for (BhmmcsNode nd : newCoverNodes1)
            walkDown(nd, newCoverNodes2);

        return newCoverNodes2;
    }

    List<Integer> genMinExposedSubsets(List<Integer> minRemovedSets, List<Integer> leftSubsets, boolean[] hasExposed) {
        Set<Integer> minExposedSets = new HashSet<>();
        int[] leftCar = leftSubsets.stream().mapToInt(Integer::bitCount).toArray();

        for (int i = 0; i < minRemovedSets.size(); i++) {
            int minRemovedSet = minRemovedSets.get(i);
            for (int j = leftSubsets.size() - 1; j >= 0; j--) {
                if (Integer.bitCount(minRemovedSet) >= leftCar[j]) break;
                if (IntSet.isSubset(minRemovedSet, leftSubsets.get(j))) {
                    boolean min = true;
                    for (int k = 0; leftCar[k] < leftCar[j]; k++) {
                        if (IntSet.isSubset(leftSubsets.get(k), leftSubsets.get(j))) {
                            min = false;
                            break;
                        }
                    }
                    if (min) {
                        hasExposed[i] = true;
                        minExposedSets.add(leftSubsets.get(j));
                    }
                }
            }
        }
        return new ArrayList<>(minExposedSets);
    }

    void removeEmptySubset(List<Integer> subsets, boolean remove) {
        for (int i = 0; i < subsets.size(); i++) {
            if (subsets.get(i) == 0) {
                hasEmptySubset = !remove;
                subsets.remove(i);
                break;
            }
        }
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

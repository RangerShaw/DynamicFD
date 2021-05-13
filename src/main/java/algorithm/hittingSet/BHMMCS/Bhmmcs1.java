package algorithm.hittingSet.BHMMCS;

import util.IntSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bidirectional Hybrid MMCS algorithm
 * that supports inserting and deleting difference set
 */
public class Bhmmcs1 {

    /**
     * number of elements or attributes
     */
    private final int nElements;

    static int elementsMask;

    /**
     * each node represents a minimal cover set
     */
    private List<BhmmcsNode1> coverNodes = new ArrayList<>();

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


    public Bhmmcs1(int nEle) {
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

        BhmmcsNode1 initNode = new BhmmcsNode1(nElements, minSubsets);

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

        for (BhmmcsNode1 prevNode : coverNodes)
            prevNode.insertSubsets(newMinSubsets, rmvMinSubsets);

        List<BhmmcsNode1> newCoverSets = new ArrayList<>();
        for (BhmmcsNode1 prevNode : coverNodes)
            walkDown(prevNode, newCoverSets);

        coverNodes = newCoverSets;
    }

    /**
     * down from nd on the search tree, find all minimal hitting sets
     */
    void walkDown(BhmmcsNode1 nd, List<BhmmcsNode1> newNodes) {
        if (!walked.add(nd.hashCode())) return;

        if (nd.isCover()) {
            newNodes.add(nd);
            return;
        }

        // configure cand for child nodes
        int childCand = nd.getCand();
        int addCandidates = nd.getAddCandidates();
        childCand &= ~(addCandidates);

        for (int e : IntSet.indicesOfOnes(addCandidates)) {
            BhmmcsNode1 childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes);
                childCand |= 1 << e;
            }
        }
    }

    List<Integer> genMinRemovedSubsets(Set<Integer> removed) {
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

        Set<Integer> removed = new HashSet<>(rmvdSubsets);

        // 1 find all min removed subsets and update
        long startTime = System.nanoTime();
        List<Integer> minRmvdSubsets = genMinRemovedSubsets(removed);
        Set<Integer> minRemoved = new HashSet<>(minRmvdSubsets);

        minSubsets.removeAll(minRemoved);
        for (List<Integer> minSubsetPart : minSubsetParts)
            minSubsetPart.removeAll(minRemoved);
        //System.out.println("time 1: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // 2 find all min exposed subsets in leftSubsets and update
        startTime = System.nanoTime();
        List<List<Integer>> minExposedSets = genMinExposedSubsets(minRmvdSubsets, leftSubsets);
        for (List<Integer> exposed : minExposedSets) {
            for (int sb : exposed) {
                minSubsets.add(sb);
                for (int e : IntSet.indicesOfOnes(sb))
                    minSubsetParts.get(e).add(sb);
            }
        }
        minSubsets = minSubsets.stream().distinct().sorted(Comparator.comparing(Integer::bitCount)).collect(Collectors.toList());
        //System.out.println("time 2: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // 3 remove subsets from nodes' crit and walk up if some crit is empty
        startTime = System.nanoTime();
        for (BhmmcsNode1 nd : coverNodes) {
            nd.removeSubsets(minRemoved);
            for (int e : nd.redundantEles)
                nd.removeEle(e, minSubsetParts.get(e));
        }
        coverNodes = coverNodes.stream().distinct().collect(Collectors.toList());

        // 4 find all coverNode that intersect with minRemoved
        startTime = System.nanoTime();
        List<BhmmcsNode1> newCoverSets2 = new ArrayList<>();
        walked.clear();

        for (BhmmcsNode1 nd : coverNodes) {
            boolean potential = false;
            for (int i = 0; i < minRemoved.size(); i++) {
                int and = minRmvdSubsets.get(i) & nd.elements;
                if (!minExposedSets.get(i).isEmpty()) {
                    potential = true;
                    for (int e : IntSet.indicesOfOnes(and))
                        nd.removeEle(e, minSubsetParts.get(e));
                }
            }
            if (potential) walkDown(nd, newCoverSets2);
            else newCoverSets2.add(nd);
        }
        //System.out.println("time 4: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        coverNodes = newCoverSets2;
        //coverNodes = newCoverSets2.stream().distinct().collect(Collectors.toList());
    }

    List<List<Integer>> genMinExposedSubsets(List<Integer> minRemovedSets, List<Integer> leftSubsets) {
        List<List<Integer>> minExposedSets = new ArrayList<>();
        int[] leftCar = leftSubsets.stream().mapToInt(Integer::bitCount).toArray();

        for (int minRemovedSet : minRemovedSets) {
            List<Integer> exposed = new ArrayList<>();
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
                    if (min) exposed.add(leftSubsets.get(j));
                }
            }
            minExposedSets.add(exposed);
        }
        return minExposedSets;
    }

    void walkUp(BhmmcsNode1 nd, List<BhmmcsNode1> newNodes) {
        if (!walked.add(nd.hashCode())) return;

        if (nd.isGlobalMinimal()) {
            newNodes.add(nd);
            return;
        }

        for (int e : nd.redundantEles) {
            BhmmcsNode1 parentNode = nd.getParentNode(e, minSubsetParts.get(e));
            walkUp(parentNode, newNodes);
        }
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
                .map(BhmmcsNode1::getElements)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Integer> getMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode1::getElements)
                .collect(Collectors.toList());
    }

}

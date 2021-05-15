package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.IntSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bidirectional Hybrid MMCS algorithm
 * that supports inserting and deleting difference set
 */
public class Bhmmcs64 {

    /**
     * number of elements or attributes
     */
    private final int nElements;

    static int elementsMask;

    /**
     * each node represents a minimal cover set
     */
    private List<BhmmcsNode64> coverNodes = new ArrayList<>();

    /**
     * true iff there's an empty int to cover (which could never be covered).
     * return no cover set if true but walk down without the empty int
     */
    private boolean hasEmptySubset = false;

    /**
     * record nodes walked to avoid duplication
     */
    private Set<Long> walked = new HashSet<>();

    List<List<Long>> minSubsetParts = new ArrayList<>();

    List<Long> minSubsets = new ArrayList<>();


    public Bhmmcs64(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1 << i;
    }

    /**
     * @param subsets unique ints representing ints to be covered
     */
    public void initiate(List<Long> subsets) {
        removeEmptySubset(subsets, false);

        minSubsets = initMinSubsets(subsets);

        BhmmcsNode64 initNode = new BhmmcsNode64(nElements, minSubsets);

        walkDown(initNode, coverNodes);

        for (int i = 0; i < nElements; i++)
            minSubsetParts.add(new ArrayList<>());
        for (long sb : minSubsets)
            for (int e : IntSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);
    }

    List<Long> initMinSubsets(List<Long> subsets) {
        // subsets are already sorted
        List<Long> res = new ArrayList<>();
        boolean[] notMin = new boolean[subsets.size()];
        int[] cardinalities = subsets.stream().mapToInt(Long::bitCount).toArray();

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

    List<Long> genNewMinSubsets(List<Long> newSubsets, Set<Long> rmvMinSubsets) {
        // newSubsets are already sorted
        List<Long> newMinSubsets = new ArrayList<>();    // min subsets of new subsets
        List<Long> allMinSubsets = new ArrayList<>();    // min subsets of old and new subsets

        boolean[] notMinNew = new boolean[newSubsets.size()];
        boolean[] notMinOld = new boolean[minSubsets.size()];
        int[] cardinalities = newSubsets.stream().mapToInt(Long::bitCount).toArray();

        int i = 0, j = 0;
        for (int car = 1; car <= nElements; car++) {
            if (i == minSubsets.size() && j == newSubsets.size()) break;

            for (; i < minSubsets.size() && Long.bitCount(minSubsets.get(i)) == car; i++) {   // use old min to filter new min
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
                long sbj = newSubsets.get(j);
                allMinSubsets.add(sbj);
                newMinSubsets.add(sbj);
                for (int k = minSubsets.size() - 1; k >= 0 && car < Long.bitCount(minSubsets.get(k)); k--)
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
    public void insertSubsets(List<Long> insertedSubsets) {
        walked.clear();

        removeEmptySubset(insertedSubsets, false);

        Set<Long> rmvMinSubsets = new HashSet<>();
        List<Long> newMinSubsets = genNewMinSubsets(insertedSubsets, rmvMinSubsets);

        for (long sb : newMinSubsets)
            for (int e : IntSet.indicesOfOnes(sb))
                minSubsetParts.get(e).add(sb);

        for (BhmmcsNode64 prevNode : coverNodes)
            prevNode.insertSubsets(newMinSubsets, rmvMinSubsets);

        List<BhmmcsNode64> newCoverSets = new ArrayList<>();
        for (BhmmcsNode64 prevNode : coverNodes)
            walkDown(prevNode, newCoverSets);

        coverNodes = newCoverSets;
    }

    /**
     * down from nd on the search tree, find all minimal hitting sets
     */
    void walkDown(BhmmcsNode64 nd, List<BhmmcsNode64> newNodes) {
        if (!walked.add(nd.elements)) return;

        if (nd.isCover()) {
            newNodes.add(nd);
            return;
        }

        // configure cand for child nodes
        long childCand = nd.getCand();
        long addCandidates = nd.getAddCandidates();
        childCand &= ~(addCandidates);

        for (int e : IntSet.indicesOfOnes(addCandidates)) {
            BhmmcsNode64 childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes);
                childCand |= 1L << e;
            }
        }
    }

    List<Long> genMinRemovedSubsets(List<Long> removedSubsets) {
        Set<Long> removed = new HashSet<>(removedSubsets);
        List<Long> minRmvdSubsets = new ArrayList<>();

        for (Long minSubset : minSubsets)
            if (removed.contains(minSubset)) minRmvdSubsets.add(minSubset);

        return minRmvdSubsets;
    }

    /**
     * @param leftSubsets unique ints representing remained ints after removal
     */
    public void removeSubsets(List<Long> leftSubsets, List<Long> rmvdSubsets) {
        walked.clear();

        removeEmptySubset(leftSubsets, false);
        removeEmptySubset(rmvdSubsets, true);

        // 1 find all min removed subsets and update
        long startTime = System.nanoTime();

        List<Long> minRmvdSubsets = genMinRemovedSubsets(rmvdSubsets);
        Set<Long> minRemoved = new HashSet<>(minRmvdSubsets);

        minSubsets.removeAll(minRemoved);
        for (List<Long> minSubsetPart : minSubsetParts)
            minSubsetPart.removeAll(minRemoved);

        // 2 find all min exposed subsets in leftSubsets and update
        List<List<Long>> minExposedSets = genMinExposedSubsets(minRmvdSubsets, leftSubsets);
        for (List<Long> exposed : minExposedSets) {
            for (long sb : exposed) {
                minSubsets.add(sb);
                for (int e : IntSet.indicesOfOnes(sb))
                    minSubsetParts.get(e).add(sb);
            }
        }
        minSubsets = minSubsets.stream().distinct().collect(Collectors.toList());
        IntSet.sortLongSets(nElements, minSubsets);

        // 3 remove subsets from nodes' crit and walk up if some crit is empty
        for (BhmmcsNode64 nd : coverNodes) {
            nd.removeSubsets(minRemoved);
            // TODO: update redundantEles
            for (int e : nd.redundantEles)
                nd.removeEle(e, minSubsetParts.get(e));
        }
        coverNodes = coverNodes.stream().distinct().collect(Collectors.toList());
        //System.out.println("time 3: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // 4 find all coverNode that intersect with minRemoved and re-walk
        startTime = System.nanoTime();
        coverNodes = rewalk(minRmvdSubsets, minExposedSets);
        //System.out.println("time 4: " + ((System.nanoTime() - startTime) / 1000000) + "ms");
        //coverNodes = newCoverSets2.stream().distinct().collect(Collectors.toList());
    }

    List<BhmmcsNode64> rewalk(List<Long> minRmvdSubsets, List<List<Long>> minExposedSets) {
        List<BhmmcsNode64> newCoverSets2 = new ArrayList<>();
        walked.clear();

        for (BhmmcsNode64 nd : coverNodes) {
            boolean potential = false;
            for (int i = 0; i < minRmvdSubsets.size(); i++) {
                long and = minRmvdSubsets.get(i) & nd.elements;
                if (!minExposedSets.get(i).isEmpty()) {
                    potential = true;
                    for (int e : IntSet.indicesOfOnes(and))
                        nd.removeEle(e, minSubsetParts.get(e));
                }
            }
            if (potential) walkDown(nd, newCoverSets2);
            else newCoverSets2.add(nd);
        }
        return newCoverSets2;
    }

    List<List<Long>> genMinExposedSubsets(List<Long> minRemovedSets, List<Long> leftSubsets) {
        List<List<Long>> minExposedSets = new ArrayList<>();
        int[] leftCar = leftSubsets.stream().mapToInt(Long::bitCount).toArray();

        for (long minRemovedSet : minRemovedSets) {
            List<Long> exposed = new ArrayList<>();
            for (int j = leftSubsets.size() - 1; j >= 0; j--) {
                if (Long.bitCount(minRemovedSet) >= leftCar[j]) break;
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

    void walkUp(BhmmcsNode64 nd, List<BhmmcsNode64> newNodes) {
        if (!walked.add(nd.elements)) return;

        if (nd.isGlobalMinimal()) {
            newNodes.add(nd);
            return;
        }

        for (int e : nd.redundantEles) {
            BhmmcsNode64 parentNode = nd.getParentNode(e, minSubsetParts.get(e));
            walkUp(parentNode, newNodes);
        }
    }

    void removeEmptySubset(List<Long> subsets, boolean remove) {
        for (int i = 0; i < subsets.size(); i++) {
            if (subsets.get(i) == 0) {
                hasEmptySubset = !remove;
                subsets.remove(i);
                break;
            }
        }
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

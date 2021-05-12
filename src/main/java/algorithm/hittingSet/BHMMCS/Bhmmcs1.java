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
    private boolean hasEmptyint = false;

    /**
     * record nodes walked to avoid duplication
     */
    private Set<Integer> walked = new HashSet<>();

    List<List<Integer>> subsetParts = new ArrayList<>();


    public Bhmmcs1(int nEle) {
        nElements = nEle;

        for (int i = 0; i < nEle; i++)
            elementsMask |= 1 << i;
    }

    /**
     * @param setsToCover unique ints representing ints to be covered
     */
    public void initiate(List<Integer> setsToCover) {
        coverNodes = new ArrayList<>();

        hasEmptyint = setsToCover.stream().anyMatch(i -> i == 0);

        List<Integer> subsets = setsToCover.stream().filter(i -> i != 0).collect(Collectors.toList());

        BhmmcsNode1 initNode = new BhmmcsNode1(nElements, subsets);

        walkDown(initNode, coverNodes);

        for (int i = 0; i < nElements; i++)
            subsetParts.add(new ArrayList<>());
        for (int sb : subsets)
            for (int e : IntSet.indicesOfOnes(sb))
                subsetParts.get(e).add(sb);
    }

    /**
     * @param insertedSets unique ints representing newly inserted ints to be covered
     */
    public void insertSubsets(List<Integer> insertedSets) {
        walked.clear();

        hasEmptyint |= insertedSets.stream().anyMatch(sb -> sb == 0);

        insertedSets.remove((Integer) 0);

        for (int sb : insertedSets)
            for (int e : IntSet.indicesOfOnes(sb))
                subsetParts.get(e).add(sb);

        for (BhmmcsNode1 prevNode : coverNodes)
            prevNode.insertSubsets(insertedSets);

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

    /**
     * @param left unique ints representing remained ints after removal
     */
    public void removeSubsets(List<Integer> left, List<Integer> removed) {
        walked.clear();

        hasEmptyint &= removed.stream().noneMatch(sb -> sb == 0);

        List<Integer> leftSubsets = left.stream().filter(sb -> sb != 0).collect(Collectors.toList());
        List<Integer> removedSubsets = removed.stream().filter(sb -> sb != 0).collect(Collectors.toList());
        Set<Integer> removedSet = new HashSet<>(removedSubsets);

        long startTime = System.nanoTime();
        for (List<Integer> intsWithE : subsetParts)
            intsWithE.removeAll(removedSet);

        for (BhmmcsNode1 nd : coverNodes)
            nd.removeSubsets(removedSet);

        List<BhmmcsNode1> newCoverSets1 = new ArrayList<>();
        for (BhmmcsNode1 nd : coverNodes)
            walkUp(nd, newCoverSets1);


        leftSubsets.sort(Comparator.comparing(Integer::bitCount));
        removedSubsets.sort(Comparator.comparing(Integer::bitCount));

        int[] leftCar = leftSubsets.stream().mapToInt(Integer::bitCount).toArray();
        int[] removedCar = removedSubsets.stream().mapToInt(Integer::bitCount).toArray();

        //System.out.println("time 1: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // find all minimal ints in removed
        startTime = System.nanoTime();
        List<Integer> minRemovedSets = genMinRemovedSubsets(leftSubsets, removedSubsets, leftCar, removedCar);
        //System.out.println("time 2: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // find all minimal exposed ints in leftSubsets
        startTime = System.nanoTime();
        List<List<Integer>> minExposedSets = genMinExposedSubsets(minRemovedSets, leftSubsets, leftCar);
        //System.out.println("time 3: " + ((System.nanoTime() - startTime) / 1000000) + "ms");


        // find all coverNode that intersect with minRemovedSets
        startTime = System.nanoTime();
        List<BhmmcsNode1> newCoverSets2 = new ArrayList<>();

        walked.clear();

        for (BhmmcsNode1 nd : newCoverSets1) {
            boolean potential = false;
            for (int i = 0; i < minRemovedSets.size(); i++) {
                int and = minRemovedSets.get(i) & nd.elements;
                if (Integer.bitCount(and) > 0 && !minExposedSets.get(i).isEmpty()) {
                    potential = true;
                    for (int e : IntSet.indicesOfOnes(and))
                        nd.removeEle(e, subsetParts.get(e));
                }
            }
            if (potential) walkDown(nd, newCoverSets2);
            else newCoverSets2.add(nd);
        }
        //System.out.println("time 4: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        coverNodes = newCoverSets2.stream().distinct().collect(Collectors.toList());
    }

    List<Integer> genMinRemovedSubsets(List<Integer> leftSubsets, List<Integer> removedSubsets, int[] leftCar, int[] removedCar) {
        List<Integer> minRemovedSets = new ArrayList<>();

        boolean[] isMin = new boolean[removedSubsets.size()];
        Arrays.fill(isMin, true);

        for (int i = 0; i < leftSubsets.size(); i++) {
            for (int j = removedSubsets.size() - 1; j >= 0; j--) {
                if (leftCar[i] >= removedCar[j]) break;
                if (IntSet.isSubset(leftSubsets.get(i), removedSubsets.get(j))) isMin[j] = false;
            }
        }

        for (int i = 0; i < removedSubsets.size() - 1; i++) {
            if (!isMin[i]) continue;
            minRemovedSets.add(removedSubsets.get(i));
            for (int j = removedSubsets.size() - 1; j > i; j--) {
                if (removedCar[i] >= removedCar[j]) break;
                if (IntSet.isSubset(removedSubsets.get(i), removedSubsets.get(j))) isMin[j] = false;
            }
        }
        return minRemovedSets;
    }

    List<List<Integer>> genMinExposedSubsets(List<Integer> minRemovedSets, List<Integer> leftSubsets, int[] leftCar) {
        List<List<Integer>> minExposedSets = new ArrayList<>();

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
            BhmmcsNode1 parentNode = nd.getParentNode(e, subsetParts.get(e));
            walkUp(parentNode, newNodes);
        }
    }

    public List<Integer> getSortedMinCoverSets() {
        return hasEmptyint ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode1::getElements)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Integer> getMinCoverSets() {
        return hasEmptyint ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode1::getElements)
                .collect(Collectors.toList());
    }

}

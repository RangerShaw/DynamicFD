package algorithm.hittingSet.BHMMCS;

import algorithm.hittingSet.Subset;
import util.Utils;

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

    /**
     * each node represents a minimal cover set
     */
    private List<BhmmcsNode1> coverNodes = new ArrayList<>();

    /**
     * true iff there's an empty subset to cover (which could never be covered).
     * return no cover set if true but walk down without the empty subset
     */
    private boolean hasEmptySubset = false;

    // TODO: change Integer to BitSet
    /**
     * record nodes walked to avoid duplication
     */
    private Set<Integer> walked = new HashSet<>();

    List<List<Subset>> subsetMap = new ArrayList<>();


    public Bhmmcs1(int nEle) {
        nElements = nEle;
    }

    /**
     * @param setsToCover unique BitSets representing Subsets to be covered
     */
    public void initiate(List<BitSet> setsToCover) {
        coverNodes = new ArrayList<>();

        hasEmptySubset = setsToCover.stream().anyMatch(BitSet::isEmpty);

        List<Subset> subsets = setsToCover.stream().filter(bs -> !bs.isEmpty()).map(Subset::new).collect(Collectors.toList());

        BhmmcsNode1 initNode = new BhmmcsNode1(nElements, subsets);

        walkDown(initNode, coverNodes);

        for (int i = 0; i < nElements; i++)
            subsetMap.add(new ArrayList<>());
        for (Subset sb : subsets)
            sb.getEleStream().forEach(e -> subsetMap.get(e).add(sb));
    }

    /**
     * @param insertedSets unique BitSets representing newly inserted Subsets to be covered
     */
    public void insertSubsets(List<BitSet> insertedSets) {
        walked.clear();

        hasEmptySubset |= insertedSets.stream().anyMatch(BitSet::isEmpty);

        List<Subset> insertedSubsets = insertedSets.stream().filter(bs -> !bs.isEmpty()).map(Subset::new).collect(Collectors.toList());

        for (Subset sb : insertedSubsets)
            sb.getEleStream().forEach(e -> subsetMap.get(e).add(sb));

        for (BhmmcsNode1 prevNode : coverNodes)
            prevNode.insertSubsets(insertedSubsets);

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
        BitSet childCand = nd.getCand();
        BitSet addCandidates = nd.getAddCandidates();
        childCand.andNot(addCandidates);

        addCandidates.stream().forEach(e -> {
            BhmmcsNode1 childNode = nd.getChildNode(e, childCand);
            if (childNode.isGlobalMinimal()) {
                walkDown(childNode, newNodes);
                childCand.set(e);
            }
        });
    }

    public void removeSubsets(List<BitSet> remainedSets) {
        walked.clear();

        initiate(remainedSets);
    }

    /**
     * @param leftSets unique BitSets representing remained Subsets after removal
     */
    public void removeSubsets(List<BitSet> leftSets, List<BitSet> removedSets) {
        walked.clear();

        hasEmptySubset &= removedSets.stream().noneMatch(BitSet::isEmpty);

        Set<Subset> removedSubsets = removedSets.stream().filter(bs -> !bs.isEmpty()).map(Subset::new).collect(Collectors.toSet());
        List<Subset> remainedSubsets = leftSets.stream().filter(bs -> !bs.isEmpty()).map(Subset::new).collect(Collectors.toList());

        long startTime = System.nanoTime();
        for (List<Subset> subsetsWithE : subsetMap)
            subsetsWithE.removeIf(removedSubsets::contains);

        for (BhmmcsNode1 nd : coverNodes)
            nd.removeSubsets(remainedSubsets, removedSubsets);

        List<BhmmcsNode1> newCoverSets1 = new ArrayList<>();
        for (BhmmcsNode1 nd : coverNodes)
            walkUp(nd, newCoverSets1);


        leftSets.sort(Comparator.comparing(BitSet::cardinality));
        removedSets.sort(Comparator.comparing(BitSet::cardinality));

        int[] leftCar = leftSets.stream().mapToInt(BitSet::cardinality).toArray();
        int[] removedCar = removedSets.stream().mapToInt(BitSet::cardinality).toArray();

        System.out.println("time 1: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // find all minimal subsets in removedSets
        startTime = System.nanoTime();
        List<BitSet> minRemovedSets = new ArrayList<>();

        boolean[] isMin = new boolean[removedSets.size()];
        Arrays.fill(isMin, true);

        for (int i = 0; i < leftSets.size(); i++) {
            for (int j = removedSets.size() - 1; j >= 0; j--) {
                if (leftCar[i] >= removedCar[j]) break;
                if (Utils.isSubset(leftSets.get(i), removedSets.get(j))) isMin[j] = false;
            }
        }

        for (int i = 0; i < removedSets.size() - 1; i++) {
            if (!isMin[i]) continue;
            minRemovedSets.add(removedSets.get(i));
            for (int j = removedSets.size() - 1; j > i; j--) {
                if (removedCar[i] >= removedCar[j]) break;
                if (Utils.isSubset(removedSets.get(i), removedSets.get(j))) isMin[j] = false;
            }
        }
        System.out.println("time 2: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        // find all minimal exposed subsets in leftSets
        startTime = System.nanoTime();
        List<List<BitSet>> minExposedSets = new ArrayList<>();

        for (BitSet minRemovedSet : minRemovedSets) {
            List<BitSet> exposed = new ArrayList<>();
            for (int j = leftSets.size() - 1; j >= 0; j--) {
                if (minRemovedSet.cardinality() >= leftCar[j]) break;
                if (Utils.isSubset(minRemovedSet, leftSets.get(j))) {
                    boolean min = true;
                    for (int k = 0; leftCar[k] < leftCar[j]; k++) {
                        if (Utils.isSubset(leftSets.get(k), leftSets.get(j))) {
                            min = false;
                            break;
                        }
                    }
                    if (min) exposed.add(leftSets.get(j));
                }
            }
            minExposedSets.add(exposed);
        }

        System.out.println("time 3: " + ((System.nanoTime() - startTime) / 1000000) + "ms");


        // find all coverNode that intersect with minRemovedSets on only one element
        startTime = System.nanoTime();
        List<BhmmcsNode1> newCoverSets2 = new ArrayList<>();

        walked.clear();

        for (BhmmcsNode1 nd : newCoverSets1) {
            boolean potential = false;
            for (int i = 0; i < minRemovedSets.size(); i++) {
                Subset sb = new Subset(minRemovedSets.get(i));
                BitSet and = (BitSet) sb.elements.clone();
                and.and(nd.elements);
                if (and.cardinality() > 0 && !minExposedSets.get(i).isEmpty()) {
                    potential = true;
                    and.stream().forEach(e -> nd.removeEle(e, subsetMap.get(e)));
                }
            }
            if (potential) walkDown(nd, newCoverSets2);
            else newCoverSets2.add(nd);
        }
        System.out.println("time 4: " + ((System.nanoTime() - startTime) / 1000000) + "ms");

        coverNodes = newCoverSets2.stream().distinct().collect(Collectors.toList());
    }

    void walkUp(BhmmcsNode1 nd, List<BhmmcsNode1> newNodes) {
        if (!walked.add(nd.hashCode())) return;

        if (nd.isGlobalMinimal()) {
            newNodes.add(nd);
            return;
        }

        for (int e : nd.redundantEles) {
            BhmmcsNode1 parentNode = nd.getParentNode(e, subsetMap.get(e));
            walkUp(parentNode, newNodes);
        }
    }

    public List<BitSet> getSortedMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode1::getElements)
                .sorted(Utils.BitsetComparator())
                .collect(Collectors.toList());
    }

    public List<BitSet> getMinCoverSets() {
        return hasEmptySubset ? new ArrayList<>() : coverNodes.stream()
                .map(BhmmcsNode1::getElements)
                .collect(Collectors.toList());
    }

}

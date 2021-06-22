package algorithm.hittingSet;

import algorithm.hittingSet.AMMCS.Subset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NumSet {

    public static List<Integer> indicesOfOnes(int n) {
        List<Integer> res = new ArrayList<>();
        int pos = 0;
        while (n > 0) {
            if ((n & 1) != 0) res.add(pos);
            pos++;
            n >>>= 1;
        }
        return res;
    }

    public static List<Integer> indicesOfOnes(long n) {
        List<Integer> res = new ArrayList<>();
        int pos = 0;
        while (n > 0) {
            if ((n & 1) != 0L) res.add(pos);
            pos++;
            n >>>= 1;
        }
        return res;
    }

    public static boolean isSubset(int a, int b) {
        return a == (a & b);
    }

    public static boolean isSubset(long a, long b) {
        return a == (a & b);
    }

    public static void sortIntSets(int nEles, List<Integer> sets) {
        List<List<Integer>> buckets = new ArrayList<>(nEles + 1);
        for (int i = 0; i <= nEles; i++)
            buckets.add(new ArrayList<>());

        for (int set : sets)
            buckets.get(Integer.bitCount(set)).add(set);

        sets.clear();
        for (List<Integer> bucket : buckets)
            sets.addAll(bucket);
    }

    public static void sortLongSets(int nEles, List<Long> sets) {
        List<List<Long>> buckets = new ArrayList<>(nEles + 1);
        for (int i = 0; i <= nEles; i++)
            buckets.add(new ArrayList<>());

        for (long set : sets)
            buckets.get(Long.bitCount(set)).add(set);

        sets.clear();
        for (List<Long> bucket : buckets)
            sets.addAll(bucket);
    }

    public static List<Integer> findMinIntSets(List<Integer> sets) {
        // sets should be already sorted
        List<Integer> minSets = new ArrayList<>();

        boolean[] notMin = new boolean[sets.size()];
        int[] cardinalities = sets.stream().mapToInt(Integer::bitCount).toArray();

        for (int i = 0, size = sets.size(); i < size; i++) {
            if (!notMin[i]) {
                int setI = sets.get(i);
                minSets.add(setI);
                for (int j = sets.size() - 1; j > i && cardinalities[j] > cardinalities[i]; j--) {
                    if (!notMin[j] && NumSet.isSubset(setI, sets.get(j)))
                        notMin[j] = true;
                }
            }
        }

        return minSets;
    }

    public static List<Integer> findMinIntSets(List<Integer> sets, List<Integer> cars) {
        // sets should be already sorted
        List<Integer> minSets = new ArrayList<>();

        boolean[] notMin = new boolean[sets.size()];
        int[] cardinalities = sets.stream().mapToInt(Integer::bitCount).toArray();

        for (int i = 0, size = sets.size(); i < size; i++) {
            if (!notMin[i]) {
                int setI = sets.get(i);
                minSets.add(setI);
                cars.add(cardinalities[i]);
                for (int j = sets.size() - 1; j > i && cardinalities[j] > cardinalities[i]; j--) {
                    if (!notMin[j] && NumSet.isSubset(setI, sets.get(j)))
                        notMin[j] = true;
                }
            }
        }

        return minSets;
    }

    public static List<Long> findMinLongSets(List<Long> sets) {
        // sets should be already sorted
        List<Long> minSets = new ArrayList<>();

        boolean[] notMin = new boolean[sets.size()];
        int[] cardinalities = sets.stream().mapToInt(Long::bitCount).toArray();

        for (int i = 0, size = sets.size(); i < size; i++) {
            if (!notMin[i]) {
                long setI = sets.get(i);
                minSets.add(setI);
                for (int j = sets.size() - 1; j > i && cardinalities[j] > cardinalities[i]; j--) {
                    if (!notMin[j] && NumSet.isSubset(setI, sets.get(j)))
                        notMin[j] = true;
                }
            }
        }

        return minSets;
    }

    public static boolean removeEmptyIntSet(List<Integer> sets) {
        // sets should be sorted already
        if (sets.isEmpty() || sets.get(0) != 0) return false;
        sets.remove(0);
        return true;
    }

    public static boolean removeEmptyLongSet(List<Long> sets) {
        // sets should be sorted already
        if (sets.isEmpty() || sets.get(0) != 0) return false;
        sets.remove(0);
        return true;
    }

    public static boolean removeEmptySubSet(List<Subset> sets) {
        // sets should be sorted already
        if (sets.isEmpty() || sets.get(0).set != 0) return false;
        sets.remove(0);
        return true;
    }

    public static boolean removeEmptySubset1(List<Integer> subsets) {
        boolean hasEmptySubset = false;
        for (int i = 0; i < subsets.size(); i++) {
            if (subsets.get(i) == 0) {
                hasEmptySubset = true;
                subsets.remove(i);
                break;
            }
        }
        return hasEmptySubset;
    }

    /**
     * @param oldMinSets input of SORTED old min sets
     * @param newSets    input of SORTED new sets that are different from oldMinSets
     * @param newMinSets output of new min sets that didn't appear in oldMinSets
     * @param removed    output of removed old min sets
     * @return output of all sorted current min subsets
     */
    public static List<Integer> findMinIntSets(int nElements, List<Integer> oldMinSets,
                                               List<Integer> newSets, List<Integer> newMinSets, Set<Integer> removed) {
        List<Integer> allMinSets = new ArrayList<>();    // min sets of all current sets

        boolean[] notMinNew = new boolean[newSets.size()];
        boolean[] notMinOld = new boolean[oldMinSets.size()];
        int[] newCars = newSets.stream().mapToInt(Integer::bitCount).toArray();
        int[] oldCars = oldMinSets.stream().mapToInt(Integer::bitCount).toArray();

        for (int i = 0, j = 0, car = 1; car <= nElements; car++) {          // for each layer of cardinality
            if (i == oldMinSets.size() && j == newSets.size()) break;

            for (; i < oldMinSets.size() && oldCars[i] == car; i++) {   // use old min to filter new min
                int sbi = oldMinSets.get(i);
                if (notMinOld[i])
                    removed.add(sbi);
                else {
                    allMinSets.add(sbi);
                    for (int k = newSets.size() - 1; k >= 0 && car < newCars[k]; k--)
                        if (!notMinNew[k] && isSubset(sbi, newSets.get(k))) notMinNew[k] = true;
                }
            }

            for (; j < newSets.size() && newCars[j] == car; j++) {          // use new min to filter old and new min
                if (notMinNew[j]) continue;
                int sbj = newSets.get(j);
                allMinSets.add(sbj);
                newMinSets.add(sbj);
                for (int k = oldMinSets.size() - 1; k >= 0 && car < oldCars[k]; k--)
                    if (!notMinOld[k] && isSubset(sbj, oldMinSets.get(k))) notMinOld[k] = true;
                for (int k = newSets.size() - 1; k >= 0 && car < newCars[k]; k--)
                    if (!notMinNew[k] && isSubset(sbj, newSets.get(k))) notMinNew[k] = true;
            }
        }

        return allMinSets;
    }

    public static List<Long> findMinLongSets(int nElements, List<Long> oldMinSets,
                                             List<Long> newSets, List<Long> newMinSets, Set<Long> removed) {
        List<Long> allMinSets = new ArrayList<>();    // min sets of all current sets

        boolean[] notMinNew = new boolean[newSets.size()];
        boolean[] notMinOld = new boolean[oldMinSets.size()];
        int[] newCars = newSets.stream().mapToInt(Long::bitCount).toArray();
        int[] oldCars = oldMinSets.stream().mapToInt(Long::bitCount).toArray();

        for (int i = 0, j = 0, car = 1; car <= nElements; car++) {          // for each layer of cardinality
            if (i == oldMinSets.size() && j == newSets.size()) break;

            for (; i < oldMinSets.size() && oldCars[i] == car; i++) {   // use old min to filter new min
                long sbi = oldMinSets.get(i);
                if (notMinOld[i])
                    removed.add(sbi);
                else {
                    allMinSets.add(sbi);
                    for (int k = newSets.size() - 1; k >= 0 && car < newCars[k]; k--)
                        if (!notMinNew[k] && isSubset(sbi, newSets.get(k))) notMinNew[k] = true;
                }
            }

            for (; j < newSets.size() && newCars[j] == car; j++) {          // use new min to filter old and new min
                if (notMinNew[j]) continue;
                long sbj = newSets.get(j);
                allMinSets.add(sbj);
                newMinSets.add(sbj);
                for (int k = oldMinSets.size() - 1; k >= 0 && car < oldCars[k]; k--)
                    if (!notMinOld[k] && isSubset(sbj, oldMinSets.get(k))) notMinOld[k] = true;
                for (int k = newSets.size() - 1; k >= 0 && car < newCars[k]; k--)
                    if (!notMinNew[k] && isSubset(sbj, newSets.get(k))) notMinNew[k] = true;
            }
        }

        return allMinSets;
    }

    public static List<Integer> findRemovedMinIntSets(List<Integer> removedSets, List<Integer> oldMinSets, List<Integer> minRmvdSubsets) {
        Set<Integer> removed = new HashSet<>(removedSets);
        List<Integer> newMinSets = new ArrayList<>(Math.max(10, oldMinSets.size() - removed.size() / 2));

        for (int set : oldMinSets) {
            if (removed.contains(set)) minRmvdSubsets.add(set);
            else newMinSets.add(set);
        }

        return newMinSets;
    }

    public static List<Long> findRemovedMinLongSets(List<Long> removedSets, List<Long> oldMinSets) {
        Set<Long> minRemoved = new HashSet<>(removedSets);
        List<Long> minRmvdSubsets = new ArrayList<>();

        for (long minSubset : oldMinSets)
            if (minRemoved.contains(minSubset)) minRmvdSubsets.add(minSubset);

        return minRmvdSubsets;
    }

    public static List<Integer> findMinExposedIntSets(List<Integer> minRemovedSets, List<Integer> leftSubsets) {
        Set<Integer> minExposedSets = new HashSet<>();
        int[] leftCar = leftSubsets.stream().mapToInt(Integer::bitCount).toArray();

        for (int minRemovedSet : minRemovedSets) {
            int car = Integer.bitCount(minRemovedSet);
            for (int j = leftSubsets.size() - 1; j >= 0 && leftCar[j] > car; j--) {
                if (NumSet.isSubset(minRemovedSet, leftSubsets.get(j)) && !minExposedSets.contains(leftSubsets.get(j))) {
                    int k = 0;
                    for (; leftCar[k] < leftCar[j] && !NumSet.isSubset(leftSubsets.get(k), leftSubsets.get(j)); k++) ;
                    if (leftCar[k] >= leftCar[j]) minExposedSets.add(leftSubsets.get(j));
                }
            }
        }
        return new ArrayList<>(minExposedSets);
    }

    public static List<Long> findMinExposedLongSets(List<Long> minRemovedSets, List<Long> leftSubsets) {
        Set<Long> minExposedSets = new HashSet<>();
        int[] leftCar = leftSubsets.stream().mapToInt(Long::bitCount).toArray();

        for (long minRemovedSet : minRemovedSets) {
            int car = Long.bitCount(minRemovedSet);
            for (int j = leftSubsets.size() - 1; j >= 0 && leftCar[j] > car; j--) {
                if (NumSet.isSubset(minRemovedSet, leftSubsets.get(j)) && !minExposedSets.contains(leftSubsets.get(j))) {
                    int k = 0;
                    for (; leftCar[k] < leftCar[j] && !NumSet.isSubset(leftSubsets.get(k), leftSubsets.get(j)); k++) ;
                    if (leftCar[k] >= leftCar[j]) minExposedSets.add(leftSubsets.get(j));
                }
            }
        }
        return new ArrayList<>(minExposedSets);
    }

    public static List<Integer> findMinExposedSets(List<Integer> leftCar, List<Integer> minRemovedSets, List<Integer> leftSubsets) {
        Set<Integer> minExposedSets = new HashSet<>();

        for (int minRemovedSet : minRemovedSets) {
            int car = Integer.bitCount(minRemovedSet);
            for (int j = leftSubsets.size() - 1; j >= 0 && leftCar.get(j) > car; j--) {
                if (NumSet.isSubset(minRemovedSet, leftSubsets.get(j)) && !minExposedSets.contains(leftSubsets.get(j))) {
                    int k = 0;
                    for (; leftCar.get(k) < leftCar.get(j) && !NumSet.isSubset(leftSubsets.get(k), leftSubsets.get(j)); k++)
                        ;
                    if (leftCar.get(k) >= leftCar.get(j)) minExposedSets.add(leftSubsets.get(j));
                }
            }
        }
        return new ArrayList<>(minExposedSets);
    }

    public static List<Integer> findMinExposedSets(List<Integer> minRemovedSets, List<Integer> leftSets,
                                                   List<Integer> minSets, List<Integer> minCars) {
        Set<Integer> minExposedSets = new HashSet<>();
        int[] leftCar = leftSets.stream().mapToInt(Integer::bitCount).toArray();

        for (int minRemovedSet : minRemovedSets) {
            int car = Integer.bitCount(minRemovedSet);
            for (int j = leftSets.size() - 1; j >= 0 && leftCar[j] > car; j--) {
                if (NumSet.isSubset(minRemovedSet, leftSets.get(j)) && !minExposedSets.contains(leftSets.get(j))) {
                    boolean min = true;
                    for (int k = 0; k < minSets.size() && minCars.get(k) < leftCar[j]; k++) {
                        if (NumSet.isSubset(leftSets.get(k), leftSets.get(j))) {
                            min = false;
                            break;
                        }
                    }
                    if (min) minExposedSets.add(leftSets.get(j));
                }
            }
        }
        return new ArrayList<>(minExposedSets);
    }

}

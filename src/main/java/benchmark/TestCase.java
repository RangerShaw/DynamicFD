package benchmark;

import algorithm.differenceSet.DiffConnector;
import algorithm.hittingSet.fdConnectors.BhmmcsFdConnector;
import util.DataIO;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static benchmark.DataFp.*;

public class TestCase {

    public static void testInsert(int dataset) {
        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();

        // load base data
        List<List<String>> csvData = DataIO.readCsvFile(INSERT_BASE_DATA_INPUT[dataset]);

        // initiate pli and differenceSet
        System.out.println("Initializing...");
        DiffConnector diffConnector = new DiffConnector();
        List<BitSet> initDiffSets = diffConnector.generatePliAndDiff(csvData, INSERT_BASE_DIFF_INPUT[dataset]);
        System.out.println("  # of initial Diff: " + initDiffSets.size());

        // initiate FD
        BhmmcsFdConnector fdConnector = new BhmmcsFdConnector(csvData.get(0).size());
        fdConnector.initiate(initDiffSets);
        System.out.println("  # of initial FD: " + fdConnector.getMinFDs().stream().map(List::size).reduce(0, Integer::sum));

        // load inserted data all at once
        List<List<List<String>>> insertDatas = new ArrayList<>();
        for (String fp : INSERT_NEW_DATA_INPUT[dataset])
            insertDatas.add(DataIO.readCsvFile(fp));

        // update pli and differenceSet
        System.out.println("Updating PLI and DF: ");
        List<List<BitSet>> insertDiffSets = new ArrayList<>();
        long startTime;
        for (List<List<String>> insertData : insertDatas) {
            startTime = System.nanoTime();
            insertDiffSets.add(diffConnector.insertData(insertData));
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
        }
        insertDiffSets.forEach(ds -> System.out.println("  # of New diff sets: " + ds.size()));

        // update FD
        System.out.println("Updating FD: ");
        List<Integer> fdSizes = new ArrayList<>();
        for (List<BitSet> insertDiffSet : insertDiffSets) {
            startTime = System.nanoTime();
            fdConnector.insertSubsets(insertDiffSet);
            fdTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            fdSizes.add(fdConnector.getMinFDs().stream().map(List::size).reduce(0, Integer::sum));
        }
        fdSizes.forEach(fd -> System.out.println("  # of FDs: " + fd));

        // print runtime
        printTime(diffTimes, fdTimes);
    }

    public static void testRemove(int dataset) {
        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();

        // load base data
        List<List<String>> csvData = DataIO.readCsvFile(REMOVE_BASE_DATA_INPUT[dataset]);

        // initiate pli and differenceSet
        System.out.println("Initializing...");
        DiffConnector diffConnector = new DiffConnector();
        List<BitSet> initDiffSets = diffConnector.generatePliAndDiff(csvData, REMOVE_BASE_DIFF_INPUT[dataset]);
        System.out.println("  # of initial Diff: " + initDiffSets.size());

        // initiate FD
        BhmmcsFdConnector fdConnector = new BhmmcsFdConnector(csvData.get(0).size());
        fdConnector.initiate(initDiffSets);
        System.out.println("  # of initial FD: " + fdConnector.getMinFDs().get(0).size());

        // load removed data all at once
        List<List<Integer>> removedDatas = new ArrayList<>();
        for (String fp : REMOVE_DELETED_DATA_INPUT[dataset])
            removedDatas.add(DataIO.readRemoveFile(fp));


        // update pli and differenceSet
        System.out.println("Updating PLI and DF: ");
        long startTime;
        List<List<BitSet>> leftDiffSets = new ArrayList<>();
        for (List<Integer> removedData : removedDatas) {
            startTime = System.nanoTime();
            leftDiffSets.add(diffConnector.removeData(removedData));
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
        }
        leftDiffSets.forEach(ds -> System.out.println("  # of left diff sets: " + ds.size()));

        // update FD
        System.out.println("Updating FD: ");
        List<Integer> fdSizes = new ArrayList<>();
        for (List<BitSet> leftDiffSet : leftDiffSets) {
            startTime = System.nanoTime();
            fdConnector.removeSubsets(leftDiffSet);
            fdTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            fdSizes.add(fdConnector.getMinFDs().stream().map(List::size).reduce(0, Integer::sum));
        }
        fdSizes.forEach(fd -> System.out.println("  # of FDs: " + fd));

        // print runtime
        printTime(diffTimes, fdTimes);
    }

    static void printTime(List<Double> diffTimes, List<Double> fdTimes) {
        double diffTimeTotal = diffTimes.stream().reduce(0.0, Double::sum);
        double fdTimeTotal = fdTimes.stream().reduce(0.0, Double::sum);

        System.out.println("[Time]");
        System.out.println("------------------------------------------------------------");
        System.out.println("|  No.  |      Diff      |       FD       |      Total     |");
        System.out.println("------------------------------------------------------------");
        for (int i = 0; i < diffTimes.size(); i++)
            System.out.printf("|  %2d   |   %10.2f   |   %10.2f   |   %10.2f   |\n", i, diffTimes.get(i), fdTimes.get(i), diffTimes.get(i) + fdTimes.get(i));
        System.out.println("------------------------------------------------------------");
        System.out.printf("|  Avg  |   %10.2f   |   %10.2f   |   %10.2f   |\n", diffTimeTotal / diffTimes.size(), fdTimeTotal / fdTimes.size(), (diffTimeTotal + fdTimeTotal) / fdTimes.size());
        System.out.printf("| Total |   %10.2f   |   %10.2f   |   %10.2f   |\n", diffTimeTotal, fdTimeTotal, diffTimeTotal + fdTimeTotal);
        System.out.println("------------------------------------------------------------");
    }


}

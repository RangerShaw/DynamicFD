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
        // initiate
        DiffConnector diffConnector = new DiffConnector();
        BhmmcsFdConnector fdConnector = new BhmmcsFdConnector();
        initiate(diffConnector, fdConnector, INSERT_INPUT_BASE_DATA[dataset], INSERT_INPUT_BASE_DIFF[dataset]);

        // load inserted data all at once
        List<List<List<String>>> insertDatas = new ArrayList<>();
        for (String fp : INSERT_INPUT_NEW_DATA[dataset])
            insertDatas.add(DataIO.readCsvFile(fp));

        // insert data and record running time
        System.out.println("[INSERTING]...");

        long startTime;
        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();

        List<List<BitSet>> insertDiffSets = new ArrayList<>();
        List<List<List<BitSet>>> totalFds = new ArrayList<>();

        for (int i = 0; i < insertDatas.size(); i++) {
            // update pli and differenceSet
            startTime = System.nanoTime();
            List<BitSet> insertDiffSet = diffConnector.insertData(insertDatas.get(i));
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            insertDiffSets.add(insertDiffSet);

            // update FD
            startTime = System.nanoTime();
            List<List<BitSet>> currFDs = fdConnector.insertSubsets(insertDiffSet);
            fdTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            totalFds.add(currFDs);
        }

        // print result and time
        System.out.println("[RESULT]");
        printResult(true, insertDiffSets, totalFds, diffTimes, fdTimes);
    }

    public static void testRemove(int dataset) {
        // initiate
        DiffConnector diffConnector = new DiffConnector();
        BhmmcsFdConnector fdConnector = new BhmmcsFdConnector();
        initiate(diffConnector, fdConnector, REMOVE_INPUT_BASE_DATA[dataset], REMOVE_INPUT_BASE_DIFF[dataset]);

        // load removed data all at once
        List<List<Integer>> removedDatas = new ArrayList<>();
        for (String fp : REMOVE_INPUT_DELETED_DATA[dataset])
            removedDatas.add(DataIO.readRemoveFile(fp));

        // remove data and record running time
        System.out.println("[REMOVING]...");

        long startTime;
        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();

        List<List<BitSet>> leftDiffSets = new ArrayList<>();
        List<List<List<BitSet>>> totalFds = new ArrayList<>();
        for (int i = 0; i < removedDatas.size(); i++) {
            // update pli and differenceSet
            startTime = System.nanoTime();
            List<BitSet> leftDiffSet = diffConnector.removeData(removedDatas.get(i));
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            leftDiffSets.add(leftDiffSet);

            // update FD
            startTime = System.nanoTime();
            List<List<BitSet>> currFDs = fdConnector.removeSubsets(leftDiffSet);
            fdTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            totalFds.add(currFDs);
        }

        // print result and time
        System.out.println("[RESULT]");
        printResult(false, leftDiffSets, totalFds, diffTimes, fdTimes);
    }

    public static void testInsert1(int dataset) {
        // initiate
        DiffConnector diffConnector = new DiffConnector();
        BhmmcsFdConnector fdConnector = new BhmmcsFdConnector();
        initiate(diffConnector, fdConnector, INSERT_INPUT_BASE_DATA[dataset], INSERT_INPUT_BASE_DIFF[dataset]);

        // load inserted data all at once
        List<List<List<String>>> insertDatas = new ArrayList<>();
        for (String fp : INSERT_INPUT_NEW_DATA[dataset])
            insertDatas.add(DataIO.readCsvFile(fp));

        // record running time
        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();
        long startTime;

        // update pli and differenceSet
        System.out.println("Updating PLI and DF: ");
        List<List<BitSet>> insertDiffSets = new ArrayList<>();
        for (List<List<String>> insertData : insertDatas) {
            startTime = System.nanoTime();
            insertDiffSets.add(diffConnector.insertData(insertData));
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
        }
        insertDiffSets.forEach(ds -> System.out.println("  # of new Diffs: " + ds.size()));

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
        //printTime(diffTimes, fdTimes);
    }

    public static void testRemove1(int dataset) {
        // initiate
        DiffConnector diffConnector = new DiffConnector();
        BhmmcsFdConnector fdConnector = new BhmmcsFdConnector();
        initiate(diffConnector, fdConnector, REMOVE_INPUT_BASE_DATA[dataset], REMOVE_INPUT_BASE_DIFF[dataset]);

        // load removed data all at once
        List<List<Integer>> removedDatas = new ArrayList<>();
        for (String fp : REMOVE_INPUT_DELETED_DATA[dataset])
            removedDatas.add(DataIO.readRemoveFile(fp));

        // record running time
        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();
        long startTime;

        // update pli and differenceSet
        System.out.println("Updating PLI and DF: ");
        List<List<BitSet>> leftDiffSets = new ArrayList<>();
        for (List<Integer> removedData : removedDatas) {
            startTime = System.nanoTime();
            leftDiffSets.add(diffConnector.removeData(removedData));
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
        }
        leftDiffSets.forEach(ds -> System.out.println("  # of remaining Diffs : " + ds.size()));

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
        //printTime(diffTimes, fdTimes);
    }

    static void initiate(DiffConnector diffConnector, BhmmcsFdConnector fdConnector, String BASE_DATA_INPUT, String BASE_DIFF_INPUT) {
        // load base data
        System.out.println("[INITIALIZING]...");
        List<List<String>> csvData = DataIO.readCsvFile(BASE_DATA_INPUT);

        // initiate pli and differenceSet
        List<BitSet> initDiffSets = diffConnector.generatePliAndDiff(csvData, BASE_DIFF_INPUT);
        System.out.println("  # of initial Diff: " + initDiffSets.size());

        // initiate FD
        fdConnector.initiate(csvData.get(0).size(), initDiffSets);
        System.out.println("  # of initial FD: " + fdConnector.getMinFDs().stream().map(List::size).reduce(0, Integer::sum));

    }

    static void printResult(boolean isInsert, List<List<BitSet>> diffSets, List<List<List<BitSet>>> fds, List<Double> diffTimes, List<Double> fdTimes) {
        double diffTimeTotal = diffTimes.stream().reduce(0.0, Double::sum);
        double fdTimeTotal = fdTimes.stream().reduce(0.0, Double::sum);

        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("|       |              Size               |                     Time/ms                      |");
        System.out.println("|  No.  |---------------------------------+--------------------------------------------------|");
        System.out.printf ("|       | %s |       FD       |      Diff      |       FD       |      Total     |\n", isInsert ? "   New Diff   " : "Remaining Diff");
        System.out.println("|-------+----------------+----------------+----------------+----------------+----------------|");
        for (int i = 0; i < diffTimes.size(); i++) {
            System.out.printf("|  %2d   |   %10d   |   %10d   |   %10.2f   |   %10.2f   |   %10.2f   |\n", i, diffSets.get(i).size(), fds.get(i).stream().map(List::size).reduce(0, Integer::sum), diffTimes.get(i), fdTimes.get(i), diffTimes.get(i) + fdTimes.get(i));
        }
        System.out.println("|-------+----------------+----------------+----------------+----------------+----------------|");
        System.out.printf ("|  Avg  |      %3c       |       %3c      |   %10.2f   |   %10.2f   |   %10.2f   |\n", ' ', ' ', diffTimeTotal / diffTimes.size(), fdTimeTotal / fdTimes.size(), (diffTimeTotal + fdTimeTotal) / fdTimes.size());
        System.out.printf ("| Total |      %3c       |       %3c      |   %10.2f   |   %10.2f   |   %10.2f   |\n", ' ', ' ', diffTimeTotal, fdTimeTotal, diffTimeTotal + fdTimeTotal);
        System.out.println("----------------------------------------------------------------------------------------------\n");
    }


}

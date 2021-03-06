package benchmark;

import algorithm.appDifferenceSet.AppDiffConnector;
import algorithm.differenceSet.DiffConnector;
import algorithm.hittingSet.AMMCS.AmmcsFdConnector64;
import algorithm.hittingSet.AMMCS.Subset;
import algorithm.hittingSet.BHMMCS.Bhmmcs64;
import algorithm.hittingSet.MMCSLong.MmcsLong;
import algorithm.hittingSet.fdConnector.BhmmcsFdConnector64;
import algorithm.hittingSet.fdConnector.FdConnector;
import util.DataIO;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;

import static benchmark.DataFp.*;
import static benchmark.DataFp.REMOVE_INPUT_BASE_DIFF;

public class TestCase {

    public static void testMMCS(int dataset) {
        int nAttributes = N_ATTRIBUTES[dataset];
        System.out.println("No.\tHS\tTime(ms)");

        for (int i = 0; i < MMCS_INPUT_EDGE[dataset].length; i++) {
            Map<BitSet, Long> diffSetMap = DataIO.readDiffSetsMap(MMCS_INPUT_EDGE[dataset][i]);
            List<Long> diffSet = diffSetMap.keySet().stream().map(bs -> Utils.bitsetToLong(nAttributes, bs)).collect(Collectors.toList());

            long startTime = System.nanoTime();
            MmcsLong mmcsLong = new MmcsLong(nAttributes);
            mmcsLong.initiate(diffSet);
            double totalTime = (double) (System.nanoTime() - startTime) / 1000000;

            System.out.println(i + "\t" + mmcsLong.getMinCoverSets().size() + "\t" +totalTime);
        }
    }

    public static void testApp(int dataset, double threshold) {
        // load base data
        System.out.println("[INITIALIZING]...");
        List<List<String>> csvData = DataIO.readCsvFile(INSERT_INPUT_BASE_DATA[dataset]);

        // initiate pli and differenceSet
        long startTime = System.nanoTime();
        AppDiffConnector diffConnector = new AppDiffConnector();
        List<Subset> initDiffSets = diffConnector.generatePliAndDiff(csvData, INSERT_INPUT_BASE_DIFF[dataset]);
        System.out.println("  diff time: " + (System.nanoTime() - startTime) / 1000000 + "ms");
        System.out.println("  # of initial Diff: " + initDiffSets.size());

        // initiate FD
        long startTime1 = System.nanoTime();
        AmmcsFdConnector64 fdConnector = new AmmcsFdConnector64();
        fdConnector.initiate(threshold, diffConnector.nElements, initDiffSets);
        System.out.println("  diff time: " + (System.nanoTime() - startTime1) / 1000000 + "ms");
        System.out.println("  # of initial FD: " + fdConnector.getMinFDs().stream().map(List::size).reduce(0, Integer::sum));
    }

    public static void testDiff(int dataset) {
        for (int d = 0, size = DIFF_INPUT_DATA[dataset].length; d < size; d++) {
            DiffConnector diffConnector = new DiffConnector();
            // load base data
            System.out.println("[INITIALIZING]...");
            List<List<String>> csvData = DataIO.readCsvFile(DIFF_INPUT_DATA[dataset][d]);

            // initiate pli and differenceSet
            Map<BitSet, Long> diffMap = diffConnector.generatePliAndDiffMap(csvData);
            System.out.println("Size of diff: " + diffMap.size());
            DataIO.printDiffMap(diffMap, DIFF_OUTPUT_DIFF[dataset][d]);
        }
    }

//    public static void testInsertDiff(int dataset) {
//        DiffConnector diffConnector = initiateDiff(INSERT_INPUT_BASE_DATA[dataset], INSERT_INPUT_BASE_DIFF[dataset]);
//
//        // load inserted data all at once
//        List<List<List<String>>> insertDatas = new ArrayList<>();
//        for (String fp : INSERT_INPUT_NEW_DATA[dataset])
//            insertDatas.add(DataIO.readCsvFile(fp));
//
//        // insert data and output diff
//        System.out.println("[INSERTING]...");
//
//        ProgressBar.wrap(IntStream.range(0, insertDatas.size()), "testInsertDiff").forEach(i -> {
//            List<Integer> newDiffs = (List<Integer>) diffConnector.insertData(insertDatas.get(i));
//            for (int sb : newDiffs)
//                System.out.println(Utils.intToBitSet(diffConnector.nElements, sb));
//            DataIO.printLongDiffMap(diffConnector.nElements, (Map<Long, Long>) diffConnector.getDiffFreq(), INSERT_OUTPUT_CURR_DIFF[dataset][i]);
//        });
//    }
//
//    public static void testRemoveDiff(int dataset) {
//        DiffConnector diffConnector = initiateDiff(REMOVE_INPUT_BASE_DATA[dataset], REMOVE_INPUT_BASE_DIFF[dataset]);
//
//        // load inserted data all at once
//        List<List<Integer>> removedDatas = new ArrayList<>();
//        for (String fp : REMOVE_INPUT_DELETED_DATA[dataset])
//            removedDatas.add(DataIO.readRemoveFile(fp));
//
//        // insert data and output diff
//        System.out.println("[REMOVING]...");
//
//        ProgressBar.wrap(IntStream.range(0, removedDatas.size()), "testInsertDiff").forEach(i -> {
//            Set<? extends Number> removedDiffs = diffConnector.removeData(removedDatas.get(i));
//            System.out.println("# of diff " + i + ": " + diffConnector.getDiffSet().size());
//            DataIO.printLongDiffMap(diffConnector.nElements, (Map<Long, Long>) diffConnector.getDiffFreq(), REMOVE_OUTPUT_CURR_DIFF[dataset][i]);
//        });
//    }


    public static void testInsert(int dataset) {
        // 1 initiate
        DiffConnector diffConnector = initiateDiff(INSERT_INPUT_BASE_DATA[dataset], INSERT_INPUT_BASE_DIFF[dataset]);
        FdConnector fdConnector = initiateFd(diffConnector.nElements, diffConnector.getDiffSet());

        // 2 load inserted data all at once
        List<List<List<String>>> insertDatas = new ArrayList<>();
        for (String fp : INSERT_INPUT_NEW_DATA[dataset])
            insertDatas.add(DataIO.readCsvFile(fp));

        // 3 insert data and record running time
        System.out.println("[INSERTING]...");

        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();

        List<List<? extends Number>> insertDiffSets = new ArrayList<>();
        List<List<List<BitSet>>> totalFds = new ArrayList<>();

        for (int i = 0; i < insertDatas.size(); i++) {
            // 3.1 update pli and differenceSet
            long startTime = System.nanoTime();
            List<? extends Number> newDiffs = diffConnector.insertData(insertDatas.get(i));
            DataIO.printLongDiffMap(diffConnector, INSERT_OUTPUT_CURR_DIFF[dataset][i]);
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            insertDiffSets.add(newDiffs);
            //DataIO.printLongDiffMap(diffConnector,INSERT_OUTPUT_CURR_DIFF[dataset][i]);

            // 3.2 update FD
            startTime = System.nanoTime();
            List<List<BitSet>> currFDs = fdConnector.insertSubsets(newDiffs);
            fdTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            totalFds.add(currFDs);
            DataIO.printFDs(fdConnector, INSERT_OUTPUT_CURR_FD[dataset][i]);
        }

        // 4 print result and time
        printResult(true, insertDiffSets, totalFds, diffTimes, fdTimes);
    }

    public static void testRemove(int dataset) {
        // 1 initiate
        DiffConnector diffConnector = initiateDiff(REMOVE_INPUT_BASE_DATA[dataset], REMOVE_INPUT_BASE_DIFF[dataset]);
        FdConnector fdConnector = initiateFd(diffConnector.nElements, diffConnector.getDiffSet());
        //DataIO.printFDs(fdConnector, REMOVE_OUTPUT_BASE_FD[dataset]);

        // 2 load removed data all at once
        List<List<Integer>> removedDatas = new ArrayList<>();
        for (String fp : REMOVE_INPUT_DELETED_DATA[dataset])
            removedDatas.add(DataIO.readRemoveFile(fp));

        // 3 remove data and record running time
        System.out.println("[REMOVING]...");

        List<Double> diffTimes = new ArrayList<>();
        List<Double> fdTimes = new ArrayList<>();

        List<List<? extends Number>> leftDiffSets = new ArrayList<>();
        List<List<List<BitSet>>> totalFds = new ArrayList<>();

        for (int i = 0; i < removedDatas.size(); i++) {         // different rounds
            // 3.1 update pli and differenceSet
            long startTime = System.nanoTime();
            Set<? extends Number> removedDiffs = diffConnector.removeData(removedDatas.get(i));
            List<? extends Number> leftDiffSet = diffConnector.getDiffSet();
            diffTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            leftDiffSets.add(leftDiffSet);
            //DataIO.printLongDiffMap(diffConnector,INSERT_OUTPUT_CURR_DIFF[dataset][i]);

            // 3.2 update FD
            startTime = System.nanoTime();
            List<List<BitSet>> currFDs = fdConnector.removeSubsets(leftDiffSet, removedDiffs);
            fdTimes.add((double) (System.nanoTime() - startTime) / 1000000);
            totalFds.add(currFDs);
            DataIO.printFDs(fdConnector, REMOVE_OUTPUT_DELETED_FD[dataset][i]);
        }

        // 4 print result and time
        printResult(false, leftDiffSets, totalFds, diffTimes, fdTimes);
    }

    public static void testBHMMCS(int dataset) {
        int nAttributes = N_ATTRIBUTES[dataset];

        Map<BitSet, Long> diffSetMap = DataIO.readDiffSetsMap(BHMMCS_INPUT_BASE_EDGE[dataset]);
        List<Long> diffSet = diffSetMap.keySet().stream().map(bs -> Utils.bitsetToLong(nAttributes, bs)).collect(Collectors.toList());

        Bhmmcs64 bhmmcs64 = new Bhmmcs64(nAttributes);
        bhmmcs64.initiate(diffSet);

        System.out.println("No.\tHS\tTime(ms)");

        for (int i = 0; i < BHMMCS_INPUT_LEFT_EDGE[dataset].length; i++) {
            List<Long> left = DataIO.readDiffSetsMap(BHMMCS_INPUT_LEFT_EDGE[dataset][i]).keySet().stream().map(bs -> Utils.bitsetToLong(nAttributes, bs)).collect(Collectors.toList());
            List<Long> rmvd = DataIO.readDiffSetsMap(BHMMCS_INPUT_RMVD_EDGE[dataset][i]).keySet().stream().map(bs -> Utils.bitsetToLong(nAttributes, bs)).collect(Collectors.toList());

            long startTime = System.nanoTime();
            bhmmcs64.removeSubsets(left,rmvd);
            double totalTime = (double) (System.nanoTime() - startTime) / 1000000;

            System.out.println(i + "\t" + bhmmcs64.getMinCoverSets().size() + "\t" +totalTime);
        }
    }


    static DiffConnector initiateDiff(String BASE_DATA_INPUT, String BASE_DIFF_INPUT) {
        // load base data
        System.out.println("[INITIALIZING]...");
        List<List<String>> csvData = DataIO.readCsvFile(BASE_DATA_INPUT);

        // initiate pli and differenceSet
        DiffConnector diffConnector = new DiffConnector();
        List<? extends Number> initDiffSets = diffConnector.generatePliAndDiff(csvData, BASE_DIFF_INPUT);
        System.out.println("  # of initial Diff: " + initDiffSets.size());

        return diffConnector;
    }

    static FdConnector initiateFd(int nElements, List<? extends Number> initDiffSets) {
        // initiate FD
        FdConnector fdConnector = new BhmmcsFdConnector64();
        //FdConnector fdConnector = nElements <= 32 ? new BhmmcsFdConnector() : new BhmmcsFdConnector64();
        fdConnector.initiate(nElements, initDiffSets);
        System.out.println("  # of initial FD: " + fdConnector.getMinFDs().stream().map(List::size).reduce(0, Integer::sum));

        return fdConnector;
    }

    static void printResult(boolean isInsert, List<List<? extends Number>> diffSets, List<List<List<BitSet>>> fds, List<Double> diffTimes, List<Double> fdTimes) {
        double diffTimeTotal = diffTimes.stream().reduce(0.0, Double::sum);
        double fdTimeTotal = fdTimes.stream().reduce(0.0, Double::sum);

        System.out.println("[RESULT]");
        System.out.println("----------------------------------------------------------------------------------------------");
        System.out.println("|       |              Size               |                     Time/ms                      |");
        System.out.println("|  No.  |---------------------------------+--------------------------------------------------|");
        System.out.printf("|       | %s |       FD       |      Diff      |       FD       |      Total     |\n", isInsert ? "   New Diff   " : "Remaining Diff");
        System.out.println("|-------+----------------+----------------+----------------+----------------+----------------|");
        for (int i = 0; i < diffTimes.size(); i++)
            System.out.printf("|  %2d   |   %10d   |   %10d   |   %10.2f   |   %10.2f   |   %10.2f   |\n", i, diffSets.get(i).size(), fds.get(i).stream().map(List::size).reduce(0, Integer::sum), diffTimes.get(i), fdTimes.get(i), diffTimes.get(i) + fdTimes.get(i));
        System.out.println("|-------+----------------+----------------+----------------+----------------+----------------|");
        System.out.printf("|  Avg  |      %3c       |       %3c      |   %10.2f   |   %10.2f   |   %10.2f   |\n", ' ', ' ', diffTimeTotal / diffTimes.size(), fdTimeTotal / fdTimes.size(), (diffTimeTotal + fdTimeTotal) / fdTimes.size());
        System.out.printf("| Total |      %3c       |       %3c      |   %10.2f   |   %10.2f   |   %10.2f   |\n", ' ', ' ', diffTimeTotal, fdTimeTotal, diffTimeTotal + fdTimeTotal);
        System.out.println("----------------------------------------------------------------------------------------------\n");
    }

}

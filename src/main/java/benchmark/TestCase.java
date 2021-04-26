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
        // load base data
        List<List<String>> csvData = DataIO.readCsvFile(CSV_INSERT_INPUT[dataset][0]);

        // initiate pli and differenceSet
        System.out.println("Initializing...");
        DiffConnector diffConnector = new DiffConnector();
        List<BitSet> initDiffSets = diffConnector.generatePliAndDiff(csvData, DIFF_INSERT_INPUT[dataset]);
        System.out.println("  # of initial Diff: " + initDiffSets.size());

        // initiate FD
        BhmmcsFdConnector fdConnector = new BhmmcsFdConnector(csvData.get(0).size());
        fdConnector.initiate(initDiffSets);
        System.out.println("  # of initial FD: " + fdConnector.getMinFDs().get(0).size());

        // load inserted data all at once
        List<List<List<String>>> insertDatas = new ArrayList<>();
        for (int i = 0; i < CSV_INSERT_INPUT[dataset].length - 1; i++) {
            insertDatas.add(DataIO.readCsvFile(CSV_INSERT_INPUT[dataset][i + 1]));
        }

        // update pli and differenceSet
        System.out.println("Updating PLI and DF: ");
        long startTime1 = System.nanoTime();
        List<List<BitSet>> insertDiffSets = new ArrayList<>();
        for (int i = 0; i < CSV_INSERT_INPUT[dataset].length - 1; i++) {
            insertDiffSets.add(diffConnector.insertData(insertDatas.get(i)));
            System.out.println("  " + i + ". # of New diff sets: " + insertDiffSets.get(i).size());
        }
        double runtime1 = (double) (System.nanoTime() - startTime1) / 1000000;

        // update FD
        System.out.println("Updating FD: ");
        long startTime2 = System.nanoTime();
        for (int i = 0; i < CSV_INSERT_INPUT[dataset].length - 1; i++) {
            fdConnector.insertSubsets(insertDiffSets.get(i));
            System.out.println("  " + i + ". # of Fd: " + fdConnector.getMinFDs().get(0).size());
        }
        double runtime2 = (double) (System.nanoTime() - startTime2) / 1000000;

        // print runtime
        System.out.println("[Time]");
        System.out.println("  Update PLI and Diff: \t" + runtime1 + "ms");
        System.out.println("  Update FD: \t\t\t" + runtime2 + "ms");
        System.out.println("  Total: \t\t\t\t" + (runtime1 + runtime2) + "ms");
        System.out.println("  Average: \t\t\t\t" + (runtime1 + runtime2) / (CSV_INSERT_INPUT[dataset].length - 1) + "ms");
        System.out.println("--------------------------------------------------------");
    }

}

package benchmark;

public class Benchmark {

    public static void main(String[] args) {
        int dataset = 0;

        //TestCase.testInsert(dataset);
        //TestCase.testRemove(dataset);
        //TestCase.testDiff(dataset);
        //TestCase.testInsertDiff(dataset);
        //TestCase.testRemoveDiff(dataset);

        //TestCase.testApp(dataset, 0.001);
        //TestCase.testMMCS(dataset);
        TestCase.testBHMMCS(dataset);
    }

}

package benchmark;

public class Benchmark {

    public static void main(String[] args) {
        int dataset = 2;

        TestCase.testInsert(dataset);
        //TestCase.testRemove(dataset);
        //TestCase.testDiff(dataset);
        //TestCase.testInsertDiff(dataset);
    }

}

package algorithm.differenceSet;

public class Tuple {

    /**
     * index of this tuple in inversePli
     */
    int pos;

    int[] cells;

    public Tuple(int n, int pos) {
        cells = new int[n];
        this.pos = pos;
    }

    public int get(int e) {
        return cells[e];
    }
}

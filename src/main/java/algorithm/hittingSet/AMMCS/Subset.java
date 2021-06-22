package algorithm.hittingSet.AMMCS;

public class Subset {

    public long set;

    public long count;

    public Subset(long set) {
        this.set = set;
        this.count = 1;
    }

    public Subset(long set, long count) {
        this.set = set;
        this.count = count;
    }


}

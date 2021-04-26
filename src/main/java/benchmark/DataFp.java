package benchmark;

public class DataFp {

    /*
        1   letter
        2   ...
        3   ...
    */

    // test INSERT
    static String[][] CSV_INSERT_INPUT = new String[][]{
            {
                    "dataFiles\\letter\\insert\\letter_15000.csv",
                    "dataFiles\\letter\\insert\\letter_15000-16000.csv",
                    "dataFiles\\letter\\insert\\letter_16000-17000.csv",
                    "dataFiles\\letter\\insert\\letter_17000-18000.csv",
                    "dataFiles\\letter\\insert\\letter_18000-19000.csv",
                    "dataFiles\\letter\\insert\\letter_19000-20000.csv"
            },
            {
                    // the next dataset file path
            }
    };

    static String[] DIFF_INSERT_INPUT = new String[]{
            "dataFiles\\letter\\insert\\letter_DS_15000.txt"
            // the next dataset file path
    };

    static String[][] FD_INSERT_OUTPUT = new String[][]{
            {
                    "dataFiles\\letter\\insert\\letter_FD_15000.txt",
                    "dataFiles\\letter\\insert\\letter_FD_16000.txt",
                    "dataFiles\\letter\\insert\\letter_FD_17000.txt",
                    "dataFiles\\letter\\insert\\letter_FD_18000.txt",
                    "dataFiles\\letter\\insert\\letter_FD_19000.txt",
                    "dataFiles\\letter\\insert\\letter_FD_20000.txt"
            },
            {
                    // the next dataset file path
            }
    };


    // test REMOVE
    static String[][] CSV_REMOVE_INPUT = new String[][]{
            {

            }
    };

    static String[] DIFF_REMOVE_INPUT = new String[]{

    };

    static String[][] FD_REMOVE_OUTPUT = new String[][]{
            {

            }
    };


}

package benchmark;

/**
 * Data File Path
 */
public class DataFp {

    /*
        1   letter
        2   ...
        3   ...
    */


    /* test INSERT */

    static String[] INSERT_INPUT_BASE_DATA = new String[]{
            "dataFiles/letter/insert/letter_15000.csv"
    };

    static String[] INSERT_INPUT_BASE_DIFF = new String[]{
            "dataFiles/letter/insert/letter_DS_15000.txt"
    };

    static String[] INSERT_OUTPUT_BASE_FD = new String[]{
            "dataFiles/letter/insert/letter_FD_15000.txt"
    };

    static String[][] INSERT_INPUT_NEW_DATA = new String[][]{
            {
                    "dataFiles/letter/insert/letter_15000-16000.csv",
                    "dataFiles/letter/insert/letter_16000-17000.csv",
                    "dataFiles/letter/insert/letter_17000-18000.csv",
                    "dataFiles/letter/insert/letter_18000-19000.csv",
                    "dataFiles/letter/insert/letter_19000-20000.csv"
            },
            {
                    // the next dataset file path
            }
    };

    static String[][] INSERT_OUTPUT_NEW_FD = new String[][]{
            {
                    "dataFiles/letter/insert/letter_FD_16000.txt",
                    "dataFiles/letter/insert/letter_FD_17000.txt",
                    "dataFiles/letter/insert/letter_FD_18000.txt",
                    "dataFiles/letter/insert/letter_FD_19000.txt",
                    "dataFiles/letter/insert/letter_FD_20000.txt"
            },
            {
                    // the next dataset file path
            }
    };



    /* test REMOVE */

    static String[] REMOVE_INPUT_BASE_DATA = new String[]{
            "dataFiles/letter/remove/letter_20000.csv"
    };

    static String[] REMOVE_INPUT_BASE_DIFF = new String[]{
            "dataFiles/letter/remove/letter_DS_20000.txt"
    };
    static String[] REMOVE_OUTPUT_BASE_FD = new String[]{
            "dataFiles/letter/remove/letter_FD_20000.txt"
    };

    static String[][] REMOVE_INPUT_DELETED_DATA = new String[][]{
            {
                    "dataFiles/letter/remove/letter_19000-19999.csv",
                    "dataFiles/letter/remove/letter_18000-18999.csv",
                    "dataFiles/letter/remove/letter_17000-17999.csv",
                    "dataFiles/letter/remove/letter_16000-16999.csv",
                    "dataFiles/letter/remove/letter_15000-15999.csv",
            }
            // the next dataset file path
    };

    static String[][] REMOVE_OUTPUT_DELETED_FD = new String[][]{
            {
                    "dataFiles/letter/remove/letter_FD_20000.txt",
                    "dataFiles/letter/remove/letter_FD_19000.txt",
                    "dataFiles/letter/remove/letter_FD_18000.txt",
                    "dataFiles/letter/remove/letter_FD_17000.txt",
                    "dataFiles/letter/remove/letter_FD_16000.txt"
            }
    };


}

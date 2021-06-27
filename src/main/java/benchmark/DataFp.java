package benchmark;

/**
 * Data File Path
 */
public class DataFp {

    /*
        0   letter
        1   reduced
        2   echo
        3   pitches
        4   hepatitis
    */


    /* test INSERT */

    static String[] INSERT_INPUT_BASE_DATA = new String[]{
            "dataFiles/letter/insert/letter_15000.csv",
            "dataFiles/reduced/insert/fd-reduced_200000.csv",
            "dataFiles/echo/insert/echocardiogram_70.csv",
            "",
            "dataFiles/hepatitis/insert/hepatitis_71.csv"
    };

    static String[] INSERT_INPUT_BASE_DIFF = new String[]{
            "dataFiles/letter/insert/letter_DS_15000.txt",
            "dataFiles/reduced/insert/fd-reduced_DS_200000.csv",
            "dataFiles/echo/insert/echocardiogram_DS_70.csv",
            "",
            "dataFiles/hepatitis/insert/hepatitis_DS_71.csv"
    };

    static String[] INSERT_OUTPUT_BASE_FD = new String[]{
            "dataFiles/letter/insert/letter_FD_15000.txt",
            "dataFiles/reduced/insert/fd-reduced_FD_200000.csv",
            "dataFiles/echo/insert/echocardiogram_FD_70.csv",
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
                    "dataFiles/reduced/insert/fd-reduced_200000-210000.csv",
                    "dataFiles/reduced/insert/fd-reduced_210000-220000.csv",
                    "dataFiles/reduced/insert/fd-reduced_220000-230000.csv",
                    "dataFiles/reduced/insert/fd-reduced_230000-240000.csv",
                    "dataFiles/reduced/insert/fd-reduced_240000-250000.csv",
            },
            {
                    "dataFiles/echo/insert/echocardiogram_70-80.csv",
                    "dataFiles/echo/insert/echocardiogram_80-90.csv",
            },
            {

            },
            {

            }
    };

    static String[][] INSERT_OUTPUT_CURR_DIFF = new String[][]{
            {
                    "dataFiles/letter/insert/letter_DS_16000.txt",
                    "dataFiles/letter/insert/letter_DS_17000.txt",
                    "dataFiles/letter/insert/letter_DS_18000.txt",
                    "dataFiles/letter/insert/letter_DS_19000.txt",
                    "dataFiles/letter/insert/letter_DS_20000.txt"
            },
            {
                    "dataFiles/reduced/insert/fd-reduced_DS_210000.csv",
                    "dataFiles/reduced/insert/fd-reduced_DS_220000.csv",
                    "dataFiles/reduced/insert/fd-reduced_DS_230000.csv",
                    "dataFiles/reduced/insert/fd-reduced_DS_240000.csv",
                    "dataFiles/reduced/insert/fd-reduced_DS_250000.csv",
            },
            {

            },
            {

            },
            {

            }
    };

    static String[][] INSERT_OUTPUT_CURR_FD = new String[][]{
            {
                    "dataFiles/letter/insert/letter_FD_16000.txt",
                    "dataFiles/letter/insert/letter_FD_17000.txt",
                    "dataFiles/letter/insert/letter_FD_18000.txt",
                    "dataFiles/letter/insert/letter_FD_19000.txt",
                    "dataFiles/letter/insert/letter_FD_20000.txt"
            },
            {
                    "dataFiles/reduced/insert/fd-reduced_FD_210000.csv",
                    "dataFiles/reduced/insert/fd-reduced_FD_220000.csv",
                    "dataFiles/reduced/insert/fd-reduced_FD_230000.csv",
                    "dataFiles/reduced/insert/fd-reduced_FD_240000.csv",
                    "dataFiles/reduced/insert/fd-reduced_FD_250000.csv",
            },
            {
                    "dataFiles/echo/insert/echocardiogram_FD_80.csv",
                    "dataFiles/echo/insert/echocardiogram_FD_90.csv",
            },
            {

            },
            {

            }
    };



    /* test REMOVE */

    static String[] REMOVE_INPUT_BASE_DATA = new String[]{
            "dataFiles/letter/remove/letter_20000.csv",
            "dataFiles/reduced/remove/fd-reduced_250000.csv",
            "",
            "dataFiles/pitches/remove/2019_pitches_250000.csv",
            "dataFiles/hepatitis/remove/hepatitis_154.csv"
    };

    static String[] REMOVE_INPUT_BASE_DIFF = new String[]{
            "dataFiles/letter/remove/letter_DS_20000.txt",
            "dataFiles/reduced/remove/fd-reduced_DS_250000.csv",
            "",
            "dataFiles/pitches/remove/2019_pitches_DS_250000.txt",
            "dataFiles/hepatitis/remove/hepatitis_DS_154.txt"
    };
    static String[] REMOVE_OUTPUT_BASE_FD = new String[]{
            "dataFiles/letter/remove/letter_FD_20000.txt",
            "dataFiles/reduced/remove/fd-reduced_FD_250000.csv",
    };

    static String[][] REMOVE_INPUT_DELETED_DATA = new String[][]{
            {
                    "dataFiles/letter/remove/letter_19000-19999.csv",
                    "dataFiles/letter/remove/letter_18000-18999.csv",
                    "dataFiles/letter/remove/letter_17000-17999.csv",
                    "dataFiles/letter/remove/letter_16000-16999.csv",
                    "dataFiles/letter/remove/letter_15000-15999.csv",
            },
            {
                    "dataFiles/reduced/remove/fd-reduced_249999-240000.csv",
                    "dataFiles/reduced/remove/fd-reduced_239999-230000.csv",
                    "dataFiles/reduced/remove/fd-reduced_229999-220000.csv",
                    "dataFiles/reduced/remove/fd-reduced_219999-210000.csv",
                    "dataFiles/reduced/remove/fd-reduced_209999-200000.csv",
            },
            {

            },
            {
                    "dataFiles/pitches/remove/2019_pitchesdel_241667-249999.csv",
            },
            {
                    "dataFiles/hepatitis/remove/hepatities_72-154.csv",
            }
    };

    static String[][] REMOVE_OUTPUT_CURR_DIFF = new String[][]{
            {
                    "dataFiles/letter/remove/letter_DS_16000.txt",
                    "dataFiles/letter/remove/letter_DS_17000.txt",
                    "dataFiles/letter/remove/letter_DS_18000.txt",
                    "dataFiles/letter/remove/letter_DS_19000.txt",
                    "dataFiles/letter/remove/letter_DS_20000.txt"
            },
            {
                    "dataFiles/reduced/remove/fd-reduced_DS_210000.csv",
                    "dataFiles/reduced/remove/fd-reduced_DS_220000.csv",
                    "dataFiles/reduced/remove/fd-reduced_DS_230000.csv",
                    "dataFiles/reduced/remove/fd-reduced_DS_240000.csv",
                    "dataFiles/reduced/remove/fd-reduced_DS_250000.csv",
            },
            {

            },
            {
                    "dataFiles/pitches/remove/2019_pitches_DS_241666.csv"
            }
    };


    static String[][] REMOVE_OUTPUT_DELETED_FD = new String[][]{
            {
                    "dataFiles/letter/remove/letter_FD_19000.txt",
                    "dataFiles/letter/remove/letter_FD_18000.txt",
                    "dataFiles/letter/remove/letter_FD_17000.txt",
                    "dataFiles/letter/remove/letter_FD_16000.txt",
                    "dataFiles/letter/remove/letter_FD_15000.txt",
            },
            {
                    "dataFiles/reduced/remove/fd-reduced_FD_240000.csv",
                    "dataFiles/reduced/remove/fd-reduced_FD_230000.csv",
                    "dataFiles/reduced/remove/fd-reduced_FD_220000.csv",
                    "dataFiles/reduced/remove/fd-reduced_FD_210000.csv",
                    "dataFiles/reduced/remove/fd-reduced_FD_200000.csv",
            },

    };

    /* test Diff */

    static String[][] DIFF_INPUT_DATA = new String[][]{
            {
                    "dataFiles/letter/diff/letter_15000.csv",
                    "dataFiles/letter/diff/letter_16000.csv",
                    "dataFiles/letter/diff/letter_17000.csv",
                    "dataFiles/letter/diff/letter_18000.csv",
                    "dataFiles/letter/diff/letter_19000.csv",
                    "dataFiles/letter/diff/letter_20000.csv",
            },
            {
                    "dataFiles/reduced/diff/fd-reduced_200000.csv",
                    "dataFiles/reduced/diff/fd-reduced_210000.csv",
                    "dataFiles/reduced/diff/fd-reduced_220000.csv",
                    "dataFiles/reduced/diff/fd-reduced_230000.csv",
                    "dataFiles/reduced/diff/fd-reduced_240000.csv",
                    "dataFiles/reduced/diff/fd-reduced_250000.csv",
            },
            {
                    "dataFiles/echo/diff/echocardiogram_70.csv",
                    "dataFiles/echo/diff/echocardiogram_80.csv",
                    "dataFiles/echo/diff/echocardiogram_90.csv",
            },
            {
                    //"dataFiles/pitches/diff/2019_pitches_241666.csv",
                    "dataFiles/pitches/diff/2019_pitches_250000.csv"
            },
            {
                    "dataFiles/hepatitis/diff/hepatitis_71.csv",
                    "dataFiles/hepatitis/diff/hepatitis_154.csv",
                    "dataFiles/hepatitis/diff/hepatitis_70.csv",
                    "dataFiles/hepatitis/diff/hepatitis_72.csv",
            }
    };

    static String[][] DIFF_OUTPUT_DIFF = new String[][]{
            {
                    "dataFiles/letter/diff/letter_DS_15000.csv",
                    "dataFiles/letter/diff/letter_DS_16000.csv",
                    "dataFiles/letter/diff/letter_DS_17000.csv",
                    "dataFiles/letter/diff/letter_DS_18000.csv",
                    "dataFiles/letter/diff/letter_DS_19000.csv",
                    "dataFiles/letter/diff/letter_DS_20000.csv",
            },
            {
                    "dataFiles/reduced/diff/fd-reduced_DS_200000.csv",
                    "dataFiles/reduced/diff/fd-reduced_DS_210000.csv",
                    "dataFiles/reduced/diff/fd-reduced_DS_220000.csv",
                    "dataFiles/reduced/diff/fd-reduced_DS_230000.csv",
                    "dataFiles/reduced/diff/fd-reduced_DS_240000.csv",
                    "dataFiles/reduced/diff/fd-reduced_DS_250000.csv",
            },
            {
                    "dataFiles/echo/diff/echocardiogram_DS_70.csv",
                    "dataFiles/echo/diff/echocardiogram_DS_80.csv",
                    "dataFiles/echo/diff/echocardiogram_DS_90.csv",
            },
            {
                    //"dataFiles/pitches/diff/2019_pitches_DS_241666.csv",
                    "dataFiles/pitches/diff/2019_pitches_DS_250000.csv"
            },
            {
                    "dataFiles/hepatitis/diff/hepatitis_DS_71.csv",
                    "dataFiles/hepatitis/diff/hepatitis_DS_154.csv",
                    "dataFiles/hepatitis/diff/hepatitis_70.csv",
                    "dataFiles/hepatitis/diff/hepatitis_72.csv",
            }
    };


}

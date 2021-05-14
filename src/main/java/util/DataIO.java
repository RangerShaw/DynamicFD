package util;

import algorithm.hittingSet.fdConnectors.FdConnector;
import com.csvreader.CsvReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DataIO {


    public static List<List<String>> readCsvFile(String readCsvFilePath) {
        List<List<String>> content = new ArrayList<>();

        try {
            CsvReader csvReader = new CsvReader(readCsvFilePath, ',', StandardCharsets.UTF_8);
            csvReader.readHeaders();    // skip the header
            while (csvReader.readRecord())
                content.add(new ArrayList<>(Arrays.asList(csvReader.getValues())));
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    public static List<Integer> readRemoveFile(String fp) {
        List<Integer> res = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fp))) {
            String s;
            while ((s = br.readLine()) != null)
                res.add(Integer.parseInt(s));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static HashMap<BitSet, BitSet> readAFD(String dsFilePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(dsFilePath))) {
            String s;
            while ((s = br.readLine()) != null)
                lines.add(s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<BitSet, BitSet> differnceSetAll = new HashMap<>();
        int allTuplePair = 0;
        for (String s : lines) {
            int index = s.indexOf('-');
            BitSet bitSet = new BitSet();
            for (String str : s.substring(0, index).split(",")) {
                if (str != null && str.length() > 0) bitSet.set(Integer.parseInt(str));
            }
            String last = s.substring(index + 2);
            int index2 = last.indexOf(',');
            int rhs = Integer.parseInt(last.substring(0,index2));
            if(differnceSetAll.containsKey(bitSet)){
                differnceSetAll.get(bitSet).set(rhs, true);
            }else{
                BitSet bitSet1 = new BitSet();
                bitSet1.set(rhs);
                differnceSetAll.put(bitSet, bitSet1);
            }
        }


        return differnceSetAll;
    }
    public static Map<BitSet, Integer> readDiffSetsMap(String dsFilePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(dsFilePath))) {
            String s;
            while ((s = br.readLine()) != null)
                lines.add(s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<BitSet, Integer> differnceSetAll = new HashMap<>();
        int allTuplePair = 0;
        for (String s : lines) {
            int index = s.indexOf('}');
            BitSet bitSet = new BitSet();
            for (String str : s.substring(1, index).split(", ")) {
                if (str != null && str.length() > 0) bitSet.set(Integer.parseInt(str));
            }
            differnceSetAll.put(bitSet, Integer.parseInt(s.substring(index + 2)));
        }


        return differnceSetAll;
    }

    public static void printFDs(FdConnector fdConnector, String writeFilePath, boolean append) {
        List<List<BitSet>> fd = fdConnector.getMinFDs();
        for (int i = 0; i < fd.size(); i++) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(writeFilePath, append))) {
                pw.println("FDs for attribute " + i + ":");
                fd.get(i).forEach(pw::println);
                pw.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void printFDs(FdConnector fdConnector, String writeFilePath) {
        List<List<BitSet>> fds = fdConnector.getMinFDs();
        for (List<BitSet> fd : fds)
            fd.sort(Utils.BitsetComparator());

        try (PrintWriter pw = new PrintWriter(new FileWriter(writeFilePath, false))) {
            for (int i = 0; i < fds.size(); i++) {
                pw.println("FDs for attribute " + i + ":");
                fds.get(i).forEach(pw::println);
                pw.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printDiffMap(Map<BitSet, Integer> map, String writeFilePath) {
        List<Map.Entry<BitSet, Integer>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Utils.BitsetMapComparator());
        try (PrintWriter pw = new PrintWriter(new FileWriter(writeFilePath))) {
            for (var entry : map.entrySet()) {
                pw.println(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

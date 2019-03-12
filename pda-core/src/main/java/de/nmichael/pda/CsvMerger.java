package de.nmichael.pda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import de.nmichael.pda.util.Util;

public class CsvMerger {
    
    private final String SEP;
    private final String OUTPUT;
    private final boolean ALLSERIES;

    private String[] files;
    private Hashtable<String, String[]> headers = new Hashtable<String, String[]>();
    private Hashtable<String, ArrayList<String[]>> values = new Hashtable<String, ArrayList<String[]>>();
    
    private String[] myHeaders;
    private String firstHeader;
    
    public CsvMerger(String[] files) {
        this.files = files;
        this.SEP = System.getProperty("csv.sep") != null ? System.getProperty("csv.sep") : ","; 
        this.OUTPUT = System.getProperty("csv.output") != null ? System.getProperty("csv.output") : "pda_grepped_series.csv";
        this.ALLSERIES = System.getProperty("csv.allseries") != null ? System.getProperty("csv.allseries").equalsIgnoreCase("true") : false;
    }
    
    private void readFile(String file) {
        try {
            Logger.info("Reading " + file + " ...");
            BufferedReader f = new BufferedReader(new FileReader(file));
            String s;
            int i = 0;
            String[] myHeaders = null;
            while ( (s = f.readLine()) != null) {
                String[] fields = s.split(SEP, -1);
                if (i++ == 0) {
                    headers.put(file, myHeaders = fields);
                    values.put(file, new ArrayList<String[]>());
                } else {
                    values.get(file).add(fields);
                    if (fields.length != myHeaders.length) {
                        Logger.warning("Mismatch in number of fields (" + fields.length + ", expected " + myHeaders.length + "): " + s);
                    }
                }
            }
            f.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private String[] scanHeaders() {
        Logger.info("Scanning series ...");
        Hashtable<String, Integer> allHeaders = new Hashtable<String, Integer>();
        for (String key : headers.keySet()) {
            boolean first = true;
            for (String hdr : headers.get(key)) {
                if (first) {
                    first = false;
                    if (firstHeader != null && !firstHeader.equals(hdr)) {
                        Logger.error("Mismatch in first column: " + firstHeader + " vs. " + hdr);
                        return null;
                    } else {
                        firstHeader = hdr;
                    }
                    continue;
                }
                Integer cnt = allHeaders.get(hdr);
                if (cnt == null) {
                    cnt = new Integer(1);
                } else {
                    cnt++;
                }
                allHeaders.put(hdr, cnt);
            }
        }
        Logger.info("Found " + allHeaders.size() + " series");
        ArrayList<String> headersList = new ArrayList<String>();
        for (String hdr : allHeaders.keySet()) {
            if (allHeaders.get(hdr) == files.length || ALLSERIES) {
                headersList.add(hdr);
            }
        }
        String[] finalHeaders = headersList.toArray(new String[0]);
        Arrays.sort(finalHeaders);
        Logger.info("Using " + finalHeaders.length + " series (-Dcsv.allseries=" + ALLSERIES + ")");
        return finalHeaders;
    }
    
    private void writeHeader(BufferedWriter f) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append(firstHeader);
        for (String h : myHeaders) {
            s.append(SEP + h);
        }
        f.write(s + "\n");
    }
    
    private void writeData(BufferedWriter f, String file) throws IOException  {
        String[] thisHeader = headers.get(file);
        Hashtable<String, Integer> headerIdx = new Hashtable<String, Integer>();
        for (int i=0; i<thisHeader.length; i++) {
            headerIdx.put(thisHeader[i], i);
        }
        ArrayList<String[]> thisValues = values.get(file);
        for (String[] line : thisValues) {
            StringBuilder s = new StringBuilder();
            s.append(headerIdx.get(firstHeader) != null ? line[headerIdx.get(firstHeader)] : "MISSING_FIRST_COLUMN");
            for (String hdr : myHeaders) {
                s.append(SEP + (headerIdx.get(hdr) != null ? line[headerIdx.get(hdr)] : ""));
            }
            f.write(s + "\n");
        }
    }
    
    public int merge() {
        for (String file : files) {
            readFile(file);
        }
        myHeaders = scanHeaders();
        if (myHeaders == null || myHeaders.length == 0) {
            Logger.error("No headers found.");
            return 1;
        }
        Logger.info("Writing merged data to " + output + " (-Dcsv.output) ... ");
        try {
            BufferedWriter f = new BufferedWriter(new FileWriter(output));
            writeHeader(f);
            for (String file : files) {
                writeData(f, file);
            }
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.info("Done.");
        return 0;
    }

    public static int merge(String[] args, int start) {
        String[] files = new String[args.length - start];
        System.arraycopy(args, start, files, 0, files.length);
        return (new CsvMerger(files)).merge();
    }
    
}

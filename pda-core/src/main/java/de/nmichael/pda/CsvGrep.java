package de.nmichael.pda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Pattern;

import de.nmichael.pda.util.Util;

public class CsvGrep {
    
    private final String SEP;
    private final String OUTPUT;
    private final long TOLERANCE;

    private Pattern pattern;
    private String[] files;
    private String firstHeader;
    private Hashtable<String,String> allHeaders = new Hashtable<String,String>();
    private Hashtable<String, Hashtable<String,String>> data = new Hashtable<String, Hashtable<String,String>>(); // timestamp -> { key = value }
    
    public CsvGrep(String pattern, String[] files) {
    	this.pattern = Pattern.compile(pattern);
        this.files = files;
        this.SEP = System.getProperty("csv.sep") != null ? System.getProperty("csv.sep") : ","; 
        this.TOLERANCE = Util.string2long(System.getProperty("csv.tolerance"), 0);
        this.OUTPUT = System.getProperty("csv.output") != null ? System.getProperty("csv.output") : "pda_grepped_series.csv";
    }
    
    private void readFile(String file) {
        try {
            Logger.info("Reading " + file + " ...");
            BufferedReader f = new BufferedReader(new FileReader(file));
            String s;
            int i = 0;
            String[] myHeaders = null;
            ArrayList<Integer> myColumns = new ArrayList<Integer>();
            while ( (s = f.readLine()) != null) {
                String[] fields = s.split(SEP, -1);
                if (i++ == 0) {
                	myHeaders = fields;
                	if (myHeaders == null || myHeaders.length < 2) {
                        Logger.error("File must have at least two columns. Skipping this file.");
                        return;
                	}
                	if (firstHeader != null && !firstHeader.equals(myHeaders[0])) {
                        Logger.error("Mismatch in first column: " + firstHeader + " vs. " + myHeaders[0] + ". Skipping this file.");
                        return;
                	}
                	firstHeader = myHeaders[0];
                	for (int j=1; j<myHeaders.length; j++) {
                		Logger.debug("  matching " + pattern + " against: " + myHeaders[j]);
                		if (pattern.matcher(myHeaders[j]).matches()) {
                			myColumns.add(j);
                			allHeaders.put(myHeaders[j], myHeaders[j]);
                		}
                	}
                	Logger.info("  " + myColumns.size() + " columns matching.");
                } else {
                    if (fields.length != myHeaders.length) {
                        Logger.warning("Mismatch in number of fields (" + fields.length + ", expected " + myHeaders.length + "): " + s);
                        continue;
                    }
                    Hashtable<String,String> values = data.get(fields[0]);
                    if (values == null) {
                    	values = new Hashtable<String,String>();
                    	data.put(fields[0], values);
                    }
                    for (int j : myColumns) {
                    	values.put(myHeaders[j], fields[j]);
                    }
                }
            }
            f.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void writeHeader(BufferedWriter f, String[] myHeaders) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append(firstHeader);
        for (String h : myHeaders) {
            s.append(SEP + h);
        }
        f.write(s + "\n");
    }
    
    private void writeData(BufferedWriter f, String[] myHeaders, String row) throws IOException  {
    	boolean rowHasValues = false;
    	Hashtable<String,String> myRow = data.get(row);
        for (String hdr : myHeaders) {
        	String val = myRow.get(hdr);
            if (val != null && val.length() > 0) {
            	rowHasValues = true;
            	break;
            }
        }
        if (!rowHasValues) {
        	return;
        }
    	
        StringBuilder s = new StringBuilder();
        s.append(row);
        for (String hdr : myHeaders) {
        	String val = myRow.get(hdr);
            s.append(SEP + (val != null ? val : ""));
        }
        f.write(s + "\n");
    }
    
    private void mergeRows() {
        String[] rows = data.keySet().toArray(new String[0]);
        Arrays.sort(rows);
        String lastrow = null;
        for (String row : rows) {
            if (lastrow != null && Util.string2long(row, 0) - Util.string2long(lastrow, 0) <= TOLERANCE) {
            	// merge with lastrow
            	Hashtable<String,String> mainRow = data.get(lastrow);
            	Hashtable<String,String> myRow = data.get(row);
            	for (String hdr : myRow.keySet()) {
            		String val = myRow.get(hdr);
            		if (val != null && val.length() > 0) {
            			if (mainRow.get(hdr) != null && mainRow.get(hdr).length() > 0) {
                            Logger.warning("Tolerance " + TOLERANCE + " replaced value " + mainRow.get(hdr) + " with " + val +
            						" for " + hdr + " at " + row);
            			}
            			mainRow.put(hdr, val);
            		}
            	}
            	data.remove(row);
            } else {
            	// this is a new row
            	lastrow = row;
            }
        }
    }
    
    public int grep() {
        for (String file : files) {
            readFile(file);
        }
        if (TOLERANCE > 0) {
        	mergeRows();
        }
        String[] myHeaders = allHeaders.keySet().toArray(new String[0]);
        Arrays.sort(myHeaders);
        Logger.info("Writing grepped data to " + OUTPUT + " (-Dcsv.output) ... ");
        try {
            BufferedWriter f = new BufferedWriter(new FileWriter(OUTPUT));
            writeHeader(f, myHeaders);
            String[] rows = data.keySet().toArray(new String[0]);
            Arrays.sort(rows);
            for (String row : rows) {
                writeData(f, myHeaders, row);
            }
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.info("Done.");
        return 0;
    }

    public static int grep(String[] args, int start) {
    	String pattern = args[start];
        String[] files = new String[args.length - start - 1];
        System.arraycopy(args, start + 1, files, 0, files.length);
        return (new CsvGrep(pattern, files)).grep();
    }
    
}

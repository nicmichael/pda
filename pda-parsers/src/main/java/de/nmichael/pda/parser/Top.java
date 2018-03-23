/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2018 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.parser;

import de.nmichael.pda.data.*;
import de.nmichael.pda.util.Util;
import java.util.regex.*;
import java.util.*;
import java.io.*;

public class Top extends Parser {
    
    private static final int MAX_PROCESSES = 8192;
    private static final int MAX_LINES = 1000000;
    private static final String PARAM_THRESHOLD = "Selection Threshold usr+sys [0-100]";
    private static final String TOP_VIRT = "virt";
    private static final String TOP_RES = "res";
    private static final String TOP_SHR = "shr";
    private static final String TOP_CPU = "cpu";
    
    private static Pattern p = Pattern.compile(" *(\\d+) +([^ ]+) +[^ ]+ +[^ ]+ +(\\d+\\.?\\d*[kmgt]?) +(\\d+\\.?\\d*[kmgt]?) +(\\d+\\.?\\d*[kmgt]?) +[^ ]+ +(\\d+\\.?\\d*) +[^ ]+ +[^ ]+ (.+)");

    private int minThreshold = 0;
    protected Hashtable<String,String> pid2args = new Hashtable<String,String>();

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"top");
    }
    
    public Top() {
        super("top");
        setParameter(PARAM_THRESHOLD, "0");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_LINUX,
                null,
                "top",
                new String[][] { { "-b" } },
                "Process Statistics",
                null));
        setDefaultInterval(3); // in case data has no timestamps
    }
    
    private boolean createSeries(String category, String subcategory) {
        if (series().getNumberOfSeries(category, subcategory) > 0) {
            return false; // series already created
        }
        series().addSeries(category, subcategory, TOP_VIRT);
        series().addSeries(category, subcategory, TOP_RES);
        series().addSeries(category, subcategory, TOP_SHR);
        series().addSeries(category, subcategory, TOP_CPU);
        return true;
    }
    
    @Override
    public void createAllSeries() {
        getProcessNames();
        try {
            int countP = 0;
            int countL = 0;
            String s;
            while( (s = readLine(true)) != null) {
            	if (countL++ > MAX_LINES) {
            		return;
            	}
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String pid = m.group(1);
                    String user = m.group(2);
                    String name = m.group(7);
                    String category = getCategory(pid, user, name);
                    String subcategory = pid;
                    if (createSeries(category, subcategory) && ++countP >= MAX_PROCESSES) {
                        return;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // @Override
    public void parse() {
        setNewSamplesHeader("top - .*");
        Hashtable<String,Boolean> needed = new Hashtable<String,Boolean>(); 
        try {
            String s;
            while( (s = readLine(true)) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String pid = m.group(1);
                    String user = m.group(2);
                    String name = m.group(7);
                    String category = getCategory(pid, user, name);
                    String subcategory = pid;
                    String key = category + "," + subcategory;
                    Boolean isNeeded = needed.get(key);
                    if (isNeeded != null && !isNeeded) {
                    	continue;
                    }
                    
                    double virt = Util.stringKMG2double(m.group(3)) / (1024.0*1024.0);
                    double res = Util.stringKMG2double(m.group(4)) / (1024.0*1024.0);
                    double shr = Util.stringKMG2double(m.group(5)) / (1024.0*1024.0);
                    double cpu = Float.parseFloat(m.group(6));
                    
                    if (minThreshold == 0 || cpu >= minThreshold) {
                        long t = getCurrentTimeStamp().getTimeStamp();
                        if (series().getNumberOfSeries(category, subcategory) == 0) {
                            createSeries(category, subcategory);
                        }
                        boolean wasNeeded = false;
                        wasNeeded = series().addSampleIfNeeded(category, subcategory, TOP_VIRT, t, virt) || wasNeeded;
                        wasNeeded = series().addSampleIfNeeded(category, subcategory, TOP_RES, t, res) || wasNeeded;
                        wasNeeded = series().addSampleIfNeeded(category, subcategory, TOP_SHR, t, shr) || wasNeeded;
                        wasNeeded = series().addSampleIfNeeded(category, subcategory, TOP_CPU, t, cpu) || wasNeeded;
                        if (isNeeded == null) {
                        	needed.put(key, wasNeeded);
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // @Override
    public void setParameter(String name, String value) {
        super.setParameter(name, value);
        if (PARAM_THRESHOLD.equals(name)) {
            try {
                minThreshold = Integer.parseInt(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void getProcessNames() {
        try {
            String fname = getFilename().replace("top", "ps");
            if (!Util.isFile(fname)) {
            	if (Util.isFile(fname.replace(".log", ".out"))) {
            		fname = fname.replace(".log", ".out");
            	} else if (Util.isFile(fname.replace(".out", ".log"))) {
            		fname = fname.replace(".out", ".log");
            	} 
            }
            if (!Util.isFile(fname)) {
            	return;
            }
            BufferedReader f = new BufferedReader(new FileReader(fname));
            String s;
            // ps -efa format
            Pattern p1 = Pattern.compile(" *[^ ]+ +([0-9]+) +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +([^ ].*)");
            while ( (s = f.readLine()) != null) {
                Matcher m = p1.matcher(s);
                if (m.matches()) {
                    String pid = m.group(1);
                    String args = m.group(2);
                    pid2args.put(pid, args);
                }
            }
            f.close();
        } catch (Exception e) {
        }
    }
    
    protected String getCategory(String pid, String user, String name) {
        String args = pid2args.get(pid);
        return user.trim() + "_" + name.trim(); 
    }
    
}
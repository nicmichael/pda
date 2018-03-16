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

public class Perf extends Parser {
	
	private static final String PERF_MIPS = "MIPS";
	private static final String PERF_CPI = "CPI";
	private static final String PERF_L1IMISS = "L1-imiss";
	private static final String PERF_L1DMISS = "L1-dmiss";
	private static final String PERF_L3MISS = "L3-miss";
	private static final String PERF_BRANCHMISS = "branch-miss";
    
    private static Pattern p = Pattern.compile(" *(\\d+\\.\\d+) +([0-9,]+) +([^ ]+) +.*");
    private String category = null;

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"perf");
    }
    
    public Perf() {
        super("perf");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_LINUX,
                null,
                "perf",
                new String[][] { { "stat -e" } },
                "Hardware Counters",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
    
    private boolean createSeries(String category, String subcategory) {
        if (series().getNumberOfSeries(category, subcategory) > 0) {
            return false; // series already created
        }
        series().addSeries(category, subcategory, PERF_MIPS);
        series().addSeries(category, subcategory, PERF_CPI);
        series().addSeries(category, subcategory, PERF_L1IMISS);
        series().addSeries(category, subcategory, PERF_L1DMISS);
        series().addSeries(category, subcategory, PERF_L3MISS);
        series().addSeries(category, subcategory, PERF_BRANCHMISS);
        return true;
    }
    
    @Override
    public void createAllSeries() {
        createSeries(getCategory(), "");
    }
    
    // @Override
    public void parse() {
        float lastsec = 0;
        try {
        	Hashtable<String,Long> values = new Hashtable<String,Long>(); 
            String s;
            while( (s = readLine(true)) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    float thissec = Float.parseFloat(m.group(1));
                    long value = Long.parseLong(m.group(2).replaceAll(",", ""));
                    String name = m.group(3);
                    
                    if (thissec > (lastsec+0.9) && lastsec > 0) {
                    	long t = getCurrentTimeStamp().getTimeStamp() + ((long)(lastsec*1000.0));
                    	long diff = (long)(thissec - lastsec);
                    	Long instr = values.get("instructions");
                    	if (instr != null && instr > 0) {
        					series().addSampleIfNeeded(getCategory(), "", PERF_MIPS, t, (instr/diff) / 1000000);
        					if (values.get("cpu-cycles") != null) {
        					    series().addSampleIfNeeded(getCategory(), "", PERF_CPI, t, (double)values.get("cpu-cycles") / (double)instr);
        					}
        					if (values.get("L1-icache-load-misses") != null) {
        					    series().addSampleIfNeeded(getCategory(), "", PERF_L1IMISS, t, 100.0 * (double)values.get("L1-icache-load-misses") / (double)instr);
        					}
        					if (values.get("L1-dcache-load-misses") != null) {
        					    series().addSampleIfNeeded(getCategory(), "", PERF_L1DMISS, t, 100.0 * (double)values.get("L1-dcache-load-misses") / (double)instr);
        					}
        					if (values.get("LLC-load-misses") != null) {
        					    series().addSampleIfNeeded(getCategory(), "", PERF_L3MISS, t, 100.0 * (double)values.get("LLC-load-misses") / (double)instr);
        					}
        					if (values.get("branches") != null && values.get("branch-misses") != null && values.get("branches") > 0) {
        					    series().addSampleIfNeeded(getCategory(), "", PERF_BRANCHMISS, t, 100.0 * (double)values.get("branch-misses") / (double)values.get("branches"));
        					}
                    	}
                    }
                    
                    values.put(name, value);
                    lastsec = thissec;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getCategory() {
		if (category == null) {
			category = Util.getNameOfFile(getFilename());
			int pos = category.lastIndexOf('.');
			if (pos > 0) {
				category = category.substring(0, pos);
			}
		}
		return category;
    }
    
}
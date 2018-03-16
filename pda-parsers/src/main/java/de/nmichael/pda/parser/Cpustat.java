/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.parser;

import de.nmichael.pda.data.*;
import java.util.regex.*;
import java.util.*;
import java.io.*;

public class Cpustat extends Parser {
    
    private static final String CATEGORY_TOTAL = "all";

    private Pattern p = Pattern.compile(" *([0-9\\.]+) +([0-9]+) +tick +([0-9][0-9 ]+[0-9]) +# (.+)");
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"cpustat");
    }
    
    public Cpustat() {
        super("cpustat");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS,
                null,
                "cpustat",
                new String[][] { { "-c events -c events ..." },
                                 },
                "Hardware Counter Statistics",
                null));
        setDefaultInterval(1); // in case data has no timestamps
    }
    
    private long[] getValues(String s) {
        String[] sa = s.split(" +");
        long[] va = new long[sa.length];
        for (int i=0; i<sa.length; i++) {
            va[i] = Long.parseLong(sa[i].trim());
        }
        return va;
    }
    
    private String[] getCounters(String s) {
        String[] ca = s.split(",");
        ArrayList<String> cal = new ArrayList<String>();
        for (String c : ca) {
            if (c.startsWith("pic")) {
                int pos = c.indexOf("=");
                if (pos > 0) {
                    cal.add(c.substring(pos+1));
                }
            }
        }
        return cal.toArray(new String[0]);
    }
    
    private void addSample(long ts, String cat, String scat, String cntr, long value) {
        DataSeries s = series().getSeries(cat, scat, cntr);
        if (s == null) {
            s = series().addSeries(cat, scat, cntr);
        }
        s.addSample(ts, value);
    }
    
    @Override
    public void createAllSeries() {
        parse();
    }

    // @Override
    public void parse() {
        long startts = 0;
        long lastts = 0;
        series().clearSeriesSamples();
        try {
            Hashtable<String,Long> totals = new Hashtable<String,Long>();
            String s;
            
            while( (s = readLine()) != null) {
                if (startts == 0) {
                    startts = getCurrentTimeStamp().getTimeStamp();
                }
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    double ts = Double.parseDouble(m.group(1));
                    int cpu = Integer.parseInt(m.group(2));
                    long[] values = getValues(m.group(3));
                    String[] counters = getCounters(m.group(4));
                    long myts = startts + (long)(1000 * ts);
                    int core = cpu / 8;
                    
                    if (myts - lastts > 600) {
                        if (totals.size() > 0) {
                            for (String c : totals.keySet()) {
                                addSample(lastts, CATEGORY_TOTAL, "", c, totals.get(c));
                            }
                        }
                        totals = new Hashtable<String,Long>();
                        lastts = myts;
                    }
                    
                    String cat = Integer.toString(core);
                    String scat = Integer.toString(cpu);
                    for (int i=0; i<counters.length; i++) {
                        addSample(myts, cat, scat, counters[i], values[i]);
                        Long total = totals.get(counters[i]);
                        if (total == null) {
                            totals.put(counters[i], new Long(values[i]));
                        } else {
                            totals.put(counters[i], new Long(total + values[i]));
                        }
                    }
                    
                }
            }
            
            series().setPreferredScaleIndividual();
           
        } catch (Exception e) {
            logError(e.toString());
        }
    }
  
}
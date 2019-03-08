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

public class PerfSched extends Parser {
	
    private static Pattern p = Pattern.compile(" *([^ ]+) +([0-9]+) +\\[([0-9]+)\\] +([0-9]+)\\.([0-9]+): +([^ ]+): +([^ ].*)");
    private static Pattern pRuntime = Pattern.compile("comm=([^ ]+) pid=([0-9]+) runtime=([0-9]+) .ns. vruntime=([0-9]+) .ns.");
    private String category = null;

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"perf-sched");
    }
    
    public PerfSched() {
        super("perfsched");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_LINUX,
                null,
                "perf sched script",
                null,
                "Scheduling Event Trace",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
    
    @Override
    public void createAllSeries() {
        parse();
    }
    
    // @Override
    public void parse() {
    	long usStart = 0;
        try {
            String s;
            while( (s = readLine(true)) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                	String cmd = m.group(1);
                	String pid = m.group(2);
                	String cpu = m.group(3);
                	long sec = Long.parseLong(m.group(4));
                	long usec = Long.parseLong(m.group(5));
                	if (usStart == 0) {
                		usStart = sec * 1000000l + usec;
                	}
                	long usNow = (sec * 1000000l + usec) - usStart; 
                	String event = m.group(6);
                	String data = m.group(7);
                	if (event.equals("sched:sched_stat_runtime")) {
                		m = pRuntime.matcher(data);
                		if (m.matches()) {
                			cmd = m.group(1);
                			pid = m.group(2);
                			long runtime = Long.parseLong(m.group(3));
                			long vruntime = Long.parseLong(m.group(4));
                			String category = cmd + ":" + pid;
                			String subcategory = event + "[" + cpu + "]";
                			series().getOrAddSeries(category, subcategory , "runtime").addSampleIfNeeded(getCurrentTimeStamp().getTimeStamp() + usNow, runtime);
                			series().getOrAddSeries(category, subcategory, "vruntime").addSampleIfNeeded(getCurrentTimeStamp().getTimeStamp() + usNow, vruntime);
                		}
                	}
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
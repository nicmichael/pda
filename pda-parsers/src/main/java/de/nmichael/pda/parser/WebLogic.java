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

public class WebLogic extends Parser {
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"wls.log");
    }
    
    public WebLogic() {
        super("wls");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_WEBLOGIC,
                null,
                "WebLogic server logfile",
                new String[][] { { "-Dweblogic.debug.DebugIncrAdvisor=true (for thread statistics)" } },
                "Selective events from WebLogic server logfile",
                null));
    }
    
    // @Override
    public void createAllSeries() {
        series().addSeries("thr", "", "all");
        series().addSeries("thr", "", "healthy");
        series().addSeries("thr", "", "active");
        series().addSeries("thr", "", "idle");
        series().addSeries("thr", "", "hogs");
        series().addSeries("thr", "", "standby");
        series().addSeries("dbconn", "", "maxreached").setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries("dbconn", "", "noconn");
    }
    
    public void parse() {
        try {
            String s;
            Pattern pThreadPool = Pattern.compile(".*<IncrAdvisor>.*all threads: ([0-9]+), healthy threads: ([0-9]+), hogs: ([0-9]+), idle threads: ([0-9]+)>.*");
            Pattern pDbPoolMaxCapReached = Pattern.compile(".*<Reached maximum capacity of pool.*");
            Pattern pCouldNotGetConnection = Pattern.compile(".*Could not get connection for datasource.*");
            long tLast = -1;
            int eventCnt = 0;
            while( (s = readLine()) != null) {
                Matcher m = pThreadPool.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    int all = Integer.parseInt(m.group(1));
                    int healthy = Integer.parseInt(m.group(2));
                    int hogs = Integer.parseInt(m.group(3));
                    int idle = Integer.parseInt(m.group(4));
                    int standby = all - healthy;
                    int active = healthy - idle;
                    series().addSampleIfNeeded("thr", "", "all", t, all);
                    series().addSampleIfNeeded("thr", "", "healthy", t, healthy);
                    series().addSampleIfNeeded("thr", "", "active", t, active);
                    series().addSampleIfNeeded("thr", "", "idle", t, idle);
                    series().addSampleIfNeeded("thr", "", "hogs", t, hogs);
                    series().addSampleIfNeeded("thr", "", "standby", t, standby);
                }
                m = pDbPoolMaxCapReached.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSampleIfNeeded("dbconn", "", "maxreached", t, 1);
                }
                m = pCouldNotGetConnection.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSampleIfNeeded("dbconn", "", "noconn", t, 1);
                }
            }
            series().setPreferredScaleSame(true, false, false);
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
}

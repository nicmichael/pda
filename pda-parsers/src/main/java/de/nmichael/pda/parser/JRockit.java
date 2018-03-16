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

public class JRockit extends Parser {
    
    private static final String GC            = "gc";
    private static final String GC_CONCURRENT = "concurrent";
    private static final String GC_STWTOTAL   = "stw-total";
    private static final String GC_STWMAX     = "stw-max";
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"jrockit");
    }

    public JRockit() {
        super("jrockit");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_JAVA,
                "JRockit",
                "GC Logfile",
                new String[][] { { "" } },
                "Java Garbage Collection Logs",
                null));
        getCurrentTimeStamp().deleteAllTimeStampPatterns();
        getCurrentTimeStamp().addTimeStampPattern("MON DD, YYYY hh:mm:ss (AP)",
                            Pattern.compile(".*(\\w\\w\\w) +(\\d\\d?), (\\d\\d\\d\\d) (\\d\\d?):(\\d\\d):(\\d\\d) ?([AP]?M?) .*"),
                            new TimeStamp.Fields[] { TimeStamp.Fields.nameOfMonth, TimeStamp.Fields.day, TimeStamp.Fields.year, 
                                                     TimeStamp.Fields.hour, TimeStamp.Fields.minute, TimeStamp.Fields.second, TimeStamp.Fields.ampm } );
    }
   
    // @Override
    public void createAllSeries() {
        series().addSeries(GC, "", GC_CONCURRENT);
        series().addSeries(GC, "", GC_STWTOTAL);
        series().addSeries(GC, "", GC_STWMAX);
    }
    
    public void parse() {
        Pattern p = Pattern.compile("\\[INFO \\]\\[memory \\] \\[[^\\]]+\\] ([0-9]+)\\.([0-9]+)-.*, ([0-9\\.]+) s, sum of pauses ([0-9\\.]+) ms, longest pause ([0-9\\.]+) ms.*");
        getCurrentTimeStamp().set(0);
        try {
            long t = -1;
            String s;
            while( (s = readLine()) != null) {
                Matcher m = p.matcher(s);
                if (t == -1 && getCurrentTimeStamp().getTimeStamp() > 0) {
                    t = getCurrentTimeStamp().getTimeStamp();
                }
                if (m.matches()) {
                    long ts = Long.parseLong(m.group(1))*1000 + Long.parseLong(m.group(2));
                    series().addSampleIfNeeded(GC, "", GC_CONCURRENT, ts, Float.parseFloat(m.group(3)));
                    series().addSampleIfNeeded(GC, "", GC_STWTOTAL, ts, Float.parseFloat(m.group(4)));
                    series().addSampleIfNeeded(GC, "", GC_STWMAX, ts, Float.parseFloat(m.group(5)));
                }
            }
        } catch(Exception e) {
            logError(e.toString());
        }
        series().setPreferredScaleSame(true, false, false);
        series().setPreferredStyleAll(DataSeriesProperties.STYLE_IMPULSES);
    }
    
}

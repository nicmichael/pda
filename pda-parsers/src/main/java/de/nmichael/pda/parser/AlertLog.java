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

public class AlertLog extends Parser {
    
    private static final String REDO                = "log";
    private static final String REDO_SWITCH         = "switch";
    private static final String REDO_CANNOTALLOCATE = "cannotallocate";
    
    private static final String CHECKPOINT          = "ckpt";
    private static final String CHECKPOINT_BEGIN    = "begin";
    private static final String CHECKPOINT_END      = "end";
    private static final String CHECKPOINT_CYCLE    = "cycle";
    private static final String CHECKPOINT_INCR     = "incr";
    
    private static final String ARCHIVER            = "arc";
    private static final String ARCHIVER_BEGIN      = "begin";
    private static final String ARCHIVER_END        = "end";
    private static final String ARCHIVER_CYCLE      = "cycle";
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename, "alert");
    }
    
    public AlertLog() {
        super("AlertLog");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_ORACLE,
                null,
                "alert.log",
                null,
                "Selective events from alert.log",
                null));
    }
    
    // @Override
    public void createAllSeries() {
        parse();
    }
    
    // @Override
    public void parse() {
        Pattern pLogSwtch  = Pattern.compile("Thread ([0-9]+) advanced to log sequence ([0-9]+) .*");
        Pattern pLogCAlloc = Pattern.compile("Thread ([0-9]+) cannot allocate new log.*");
        Pattern pCkptBegin = Pattern.compile("Beginning log switch checkpoint up to RBA \\[(.+)\\].*");
        Pattern pCkptEnd   = Pattern.compile("Completed checkpoint up to RBA \\[(.+)\\].*");
        Pattern pCkptIncr  = Pattern.compile("Incremental checkpoint up to RBA.*");
        
        Pattern pArcBegin  = Pattern.compile(".*ARC([0-9]+): Evaluating archive.+log .+ thread . sequence ([0-9]+).*");
        Pattern pArcEnd    = Pattern.compile(".*ARC([0-9]+): Completed archiving thread . sequence ([0-9]+).*");
        try {
            String s;
            while( (s = readLine()) != null) {
                Matcher m;
                
                m = pLogSwtch.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSeries(REDO, "", REDO_SWITCH);
                    series().addSample(REDO, "", REDO_SWITCH, t, 1);
                }
                
                m = pLogCAlloc.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSeries(REDO, "", REDO_CANNOTALLOCATE);
                    series().addSample(REDO, "", REDO_CANNOTALLOCATE, t, 1);
                }
                
                m= pCkptBegin.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSeries(CHECKPOINT, "", CHECKPOINT_BEGIN);
                    series().addSeries(CHECKPOINT, "", CHECKPOINT_CYCLE);
                    series().addSample(CHECKPOINT, "", CHECKPOINT_BEGIN, t, 1);
                    series().addSample(CHECKPOINT, "", CHECKPOINT_CYCLE, t, 0);
                    series().addSample(CHECKPOINT, "", CHECKPOINT_CYCLE, t, 1);
                }
                
                m = pCkptEnd.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSeries(CHECKPOINT, "", CHECKPOINT_END);
                    series().addSeries(CHECKPOINT, "", CHECKPOINT_CYCLE);
                    series().addSample(CHECKPOINT, "", CHECKPOINT_END, t, 1);
                    series().addSample(CHECKPOINT, "", CHECKPOINT_CYCLE, t, 1);
                    series().addSample(CHECKPOINT, "", CHECKPOINT_CYCLE, t, 0);
                }
                
                m = pCkptIncr.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSeries(CHECKPOINT, "", CHECKPOINT_INCR);
                    series().addSample(CHECKPOINT, "", CHECKPOINT_INCR, t, 1);
                }
                
                m = pArcBegin.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    int arc = Integer.parseInt(m.group(1));
                    series().addSeries(ARCHIVER, Integer.toString(arc), ARCHIVER_BEGIN);
                    series().addSeries(ARCHIVER, Integer.toString(arc), ARCHIVER_CYCLE);
                    series().addSample(ARCHIVER, Integer.toString(arc), ARCHIVER_BEGIN, t, arc);
                    series().addSample(ARCHIVER, Integer.toString(arc), ARCHIVER_CYCLE, t, 0);
                    series().addSample(ARCHIVER, Integer.toString(arc), ARCHIVER_CYCLE, t, arc);
                }
                
                m = pArcEnd.matcher(s);
                if (m.matches()) {
                    int arc = Integer.parseInt(m.group(1));
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSeries(ARCHIVER, Integer.toString(arc), ARCHIVER_END);
                    series().addSeries(ARCHIVER, Integer.toString(arc), ARCHIVER_CYCLE);
                    series().addSample(ARCHIVER, Integer.toString(arc), ARCHIVER_END, t, arc);
                    series().addSample(ARCHIVER, Integer.toString(arc), ARCHIVER_CYCLE, t, arc);
                    series().addSample(ARCHIVER, Integer.toString(arc), ARCHIVER_CYCLE, t, 0);
                }
            }
            
            series().setPreferredScaleSame(true, false, true);
            series().setPreferredStyleSeries(REDO_SWITCH, DataSeriesProperties.STYLE_IMPULSES);
            series().setPreferredStyleSeries(REDO_CANNOTALLOCATE, DataSeriesProperties.STYLE_IMPULSES);
            series().setPreferredStyleSeries(CHECKPOINT_BEGIN, DataSeriesProperties.STYLE_IMPULSES);
            series().setPreferredStyleSeries(CHECKPOINT_END, DataSeriesProperties.STYLE_IMPULSES);
            series().setPreferredStyleSeries(CHECKPOINT_INCR, DataSeriesProperties.STYLE_IMPULSES);
            series().setPreferredStyleSeries(ARCHIVER_BEGIN, DataSeriesProperties.STYLE_IMPULSES);
            series().setPreferredStyleSeries(ARCHIVER_END, DataSeriesProperties.STYLE_IMPULSES);
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
}

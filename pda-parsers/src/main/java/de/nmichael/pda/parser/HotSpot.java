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

import de.nmichael.pda.Logger;
import de.nmichael.pda.data.*;
import de.nmichael.pda.util.Util;
import java.util.regex.*;
import java.util.*;
import java.io.*;

public class HotSpot extends Parser {
    
    private static final String C_HEAP             = "heap";
    
    private static final String SC_HEAP_EDEN       = "eden";
    private static final String SC_HEAP_SURVIVOR   = "survivor";
    private static final String SC_HEAP_YOUNG      = "young";
    private static final String SC_HEAP_OLD        = "old";
    private static final String SC_HEAP_TOTAL      = "total";
    private static final String SC_HEAP_PERM       = "perm";
    
    private static final String S_HEAP_USED        = "used";
    private static final String S_HEAP_CAPACITY    = "capacity";
    private static final String S_HEAP_BEFORE      = "before";
    private static final String S_HEAP_AFTER       = "after";
    
    private static final String C_GC               = "gc";

    private static final String SC_OBJECTS          = "objects";
    private static final String S_ALLOC_RATE       = "alloc-rate";

    private static final String SC_GC_PARNEW       = "parnew";
    private static final String SC_GC_PAROLD       = "parold";
    private static final String SC_GC_CMS          = "cms";
    private static final String SC_GC_G1           = "g1";

    private static final String S_PARNEW_PAUSE     = "pause";
    private static final String S_PAROLD_PAUSE     = "pause";
    private static final String S_CMS_INITIALMARK  = "initial-mark";
    private static final String S_CMS_REMARK       = "remark";
    private static final String S_CMS_CONCMARK     = "concurrent-mark";
    private static final String S_CMS_CONCPRECLEAN = "concurrent-preclean";
    private static final String S_CMS_CONCABPRCLN  = "concurrent-abortable-preclean";
    private static final String S_CMS_CONCSWEEP    = "concurrent-sweep";
    private static final String S_CMS_CONCRESET    = "concurrent-reset";
    private static final String S_CMS_STWTIME      = "stw-time";
    private static final String S_CMS_CONCTIME     = "concurrent-time";
    private static final String S_CMS_CYCLE        = "cycle";
    private static final String S_CMS_FULLGCTIME   = "full-gc-time";
    private static final String S_G1_PSYOUNG       = "pause(young)";
    private static final String S_G1_PSEXTROOTSCAN = "pause-ext-root-scan-avg";
    private static final String S_G1_PSUPDRS       = "pause-update-rs-avg";
    private static final String S_G1_PSUPDRSBUFS   = "pause-update-rs-processed-buffers-sum";
    private static final String S_G1_PSSCANRS      = "pause-scan-rs-avg";
    private static final String S_G1_PSOBJECTCOPY  = "pause-object-copy-avg";
    private static final String S_G1_PSTERMINATION = "pause-termination-avg";
    private static final String S_G1_PSGCWORKER    = "pause-gc-worker-avg";
    private static final String S_G1_PSGCWORKEROTR = "pause-gc-worker-other-avg";
    private static final String S_G1_PSFULL        = "pause-full";
    private static final String S_G1_CONCMARK      = "concurrent-mark";
    private static final String S_G1_REMARK        = "remark";
    private static final String S_G1_CONCCOUNT     = "concurrent-count";
    private static final String S_G1_CLEANUP       = "cleanup";
    private static final String S_G1_CONCCLEANUP   = "concurrent-cleanup";
    
    private static final String C_TIME             = "time";
    
    private static final String SC_APP             = "app";

    private static final String S_TIME_STOPPED     = "stopped";
    private static final String S_TIME_CONCURRENT  = "concurrent";

    private static Pattern timepat = Pattern.compile("[^0-9]*([0-9]+)\\.([0-9]+): .*");

    // Young Collections for +UseConcMarkSweepGC and +UseSerialGC
    private static Pattern parnewgc = Pattern.compile(".*: \\[GC [^0-9]*([0-9]+).([0-9]+): \\[...New: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), ([0-9]+.[0-9]+) secs\\] ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern parnewgcPrintTenuringDistribution1 = Pattern.compile(".*\\[...New");
    private static Pattern parnewgcPrintTenuringDistribution2 = Pattern.compile(": ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), ([0-9]+.[0-9]+) secs\\] ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\).*");
    private static Pattern parnewgcPrintTenuringDistribution3a = Pattern.compile(".*, ([0-9]+.[0-9]+) secs\\]");
    private static Pattern parnewgcPrintTenuringDistribution3b = Pattern.compile(".*, ([0-9]+.[0-9]+) secs\\] \\[Times.*");

    // Young Collections for +UseParallelOldGC
    private static Pattern psnewgen = Pattern.compile(".*\\[PSYoungGen: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\)\\] ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), ([0-9]+.[0-9]+) secs\\].*");

    // Old Collections for  +UseParallelOldGC and +UseSerialGC
    private static Pattern fullgc1 = Pattern.compile(".*\\[Full GC.*\\[PSYoungGen: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\)\\] \\[ParOldGen: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\)\\] ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\) \\[[^ ]+: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\)\\], ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern fullgc2 = Pattern.compile(".*\\[GC [0-9]+.[0-9]+: \\[DefNew: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), [0-9]+.[0-9]+ secs\\][0-9]+.[0-9]+: \\[Tenured: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), [0-9]+.[0-9]+ secs\\] ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), \\[Perm +: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\)\\], ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern fullgc3 = Pattern.compile(".*\\[Full GC.*[^0-9]*[0-9]+.[0-9]+: ()()()\\[Tenured: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), [0-9]+.[0-9]+ secs\\] ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), \\[Perm +: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\)\\], ([0-9]+.[0-9]+) secs\\].*");

    // +UseConcMarkSweepGC
    private static Pattern cms      = Pattern.compile(".*CMS.*");
    private static Pattern cms_initial_mark = Pattern.compile(".*: \\[.*CMS.[iI]nitial.[Mm]ark: .* ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern cms_remark = Pattern.compile(".*: .*\\[.*CMS-remark: .* ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern cms_concurrent_start = Pattern.compile(".*: \\[CMS-concurrent-.*-start\\].*");
    private static Pattern cms_concurrent_end = Pattern.compile(".*: \\[CMS-concurrent-(.*): ([0-9]+.[0-9]+)/.* secs\\].*");
    private static Pattern cms_fullgc = Pattern.compile(".*: .*\\[Full GC [^0-9]*[0-9]+.[0-9]+: \\[CMS: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), [0-9]+.[0-9]+ secs\\] ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), \\[Metaspace: ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\)\\], ([0-9]+.[0-9]+) secs\\].*");
            
    // +UseG1GC
    private static Pattern g1_pause_young = Pattern.compile(".*: \\[GC pause .*\\(young\\).*, ([0-9]+.[0-9]+) secs\\].*");
    private static String g1_pause_ExtRootScanning = "[Ext Root Scanning (ms):";
    private static String g1_pause_UpdateRS = "[Update RS (ms):";
    private static String g1_pause_UpdateRSProcessedBuffers = "[Processed Buffers :";
    private static String g1_pause_ScanRS = "[Scan RS (ms):";
    private static String g1_pause_ObjectCopy = "[Object Copy (ms):";
    private static String g1_pause_Termination = "[Termination (ms):";
    private static String g1_pause_GcWorkerTime = "[GC Worker Total (ms):";
    private static String g1_pause_GcWorkerOther = "[GC Worker Other (ms):";
    private static Pattern g1_pause_heap = Pattern.compile(".*\\[Eden: ([0-9\\.]+[KMG])\\(([0-9\\.]+[KMG])\\)->0\\.?0?B\\(([0-9\\.]+[KMG])\\) Survivors: ([0-9\\.]+[KMG])->([0-9\\.]+[KMG]) Heap: ([0-9\\.]+[KMG])\\(([0-9\\.]+[KMG])\\)->([0-9\\.]+[KMG])\\(([0-9\\.]+[KMG])\\)\\].*");
    private static Pattern g1_full_gc = Pattern.compile(".*: \\[Full GC ([0-9]+)K->([0-9]+)K\\(([0-9]+)K\\), ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern g1_concurrent_mark = Pattern.compile(".*: \\[GC concurrent-mark-end, ([0-9]+.[0-9]+) sec\\].*");
    private static Pattern g1_remark = Pattern.compile(".*: \\[GC remark, ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern g1_concurrent_count = Pattern.compile(".*: \\[GC concurrent-count-end, ([0-9]+.[0-9]+)\\].*");
    private static Pattern g1_cleanup = Pattern.compile(".*: \\[GC cleanup.*, ([0-9]+.[0-9]+) secs\\].*");
    private static Pattern g1_concurrent_cleanup = Pattern.compile(".*: \\[GC concurrent-cleanup-end, ([0-9]+.[0-9]+)\\].*");            
    private static Pattern g1_pause_detailsAvgMinMaxDiff = Pattern.compile(".* Avg: *([0-9]+.[0-9]+), Min: *([0-9]+.[0-9]+), Max: *([0-9]+.[0-9]+), Diff: *([0-9]+.[0-9]+)\\].*");
    private static Pattern g1_pause_detailsSumAvgMinMaxDiff = Pattern.compile(".* Sum: *([0-9]+.[0-9]+), Avg: *([0-9]+.[0-9]+), Min: *([0-9]+.[0-9]+), Max: *([0-9]+.[0-9]+), Diff: *([0-9]+.[0-9]+)\\].*");
    private static Pattern g1_pause_detailsMinAvgMaxDiffSum = Pattern.compile(".* Min: *([0-9]+.[0-9]+), Avg: *([0-9]+.[0-9]+), Max: *([0-9]+.[0-9]+), Diff: *([0-9]+.[0-9]+), Sum: *([0-9]+.[0-9]+)\\].*");
            
    private static Pattern timesStopped = Pattern.compile("Total time for which application threads were stopped: (\\d+\\.\\d+) seconds.*");
    private static Pattern timesRunning = Pattern.compile("Application time: (\\d+\\.\\d+) seconds");
    
    private boolean isUnifiedLogging = false;
    private static Pattern unifiedLogging_reltime = Pattern.compile("\\[([0-9\\.]+)s\\](.+)");
    private static Pattern unifiedLogging_abstime = Pattern.compile("\\[([0-9\\-T:\\.]+)\\+[0-9]+\\](.+)");
    
    private long starttime;
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"gc");
    }
    
    public HotSpot() {
        super("gc");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_JAVA,
                "HotSpot",
                "GC Logfile",
                new String[][] {
                    { "-XX:+UseSerialGC", "-XX:+UseParallelOldGC", "-XX:+UseConcMarkSweepGC", "-XX:+UseG1GC" },
                    { "-XX:+PrintGCDetails" },
                    { "-XX:+PrintGCTimeStamps", "-XX:+PrintGCDateStamps" },
                    { "optional:", "-XX:+PrintGCApplicationConcurrentTime", "-XX:+PrintGCApplicationStoppedTime" }
                },
                "Java Garbage Collection Logs",
                null));
        getCurrentTimeStamp().deleteAllTimeStampPatterns();
        getCurrentTimeStamp().addTimeStampPattern("YYYY[.-/]MM[.-/]DD[- T]hh[:-.]mm[:-.]ss[:-.,]mss",
                            Pattern.compile(".*(\\d\\d\\d\\d)[\\.\\-/](\\d\\d)[\\.\\-/](\\d\\d)[\\- T](\\d\\d)[:\\-\\.](\\d\\d)[:\\-\\.](\\d\\d)[:\\-\\.,](\\d\\d\\d).*"),
                            new TimeStamp.Fields[] { TimeStamp.Fields.year, TimeStamp.Fields.month, TimeStamp.Fields.day, 
                                TimeStamp.Fields.hour, TimeStamp.Fields.minute, TimeStamp.Fields.second, TimeStamp.Fields.ms } );

    }

    private void createSeriesHeap(String subcategory) {
        series().addSeries(C_HEAP, subcategory, S_HEAP_USED);
        series().addSeries(C_HEAP, subcategory, S_HEAP_CAPACITY);
        series().addSeries(C_HEAP, subcategory, S_HEAP_BEFORE);
        series().addSeries(C_HEAP, subcategory, S_HEAP_AFTER);
    }
    
    private void createSeriesGC() {
        series().addSeries(C_GC, SC_OBJECTS, S_ALLOC_RATE);
        series().addSeries(C_GC, SC_GC_PARNEW, S_PARNEW_PAUSE).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_PAROLD, S_PAROLD_PAUSE).setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_INITIALMARK).setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_REMARK).setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_CONCMARK).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_CONCPRECLEAN).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_CONCABPRCLN).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_CONCSWEEP).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_CONCRESET).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_STWTIME).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_CONCTIME).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_FULLGCTIME).setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries(C_GC, SC_GC_CMS, S_CMS_CYCLE);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSYOUNG).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSEXTROOTSCAN).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSUPDRS).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSUPDRSBUFS).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSSCANRS).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSOBJECTCOPY).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSTERMINATION).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSGCWORKER).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSGCWORKEROTR).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_PSFULL).setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries(C_GC, SC_GC_G1, S_G1_CONCMARK).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_REMARK).setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries(C_GC, SC_GC_G1, S_G1_CONCCOUNT).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        series().addSeries(C_GC, SC_GC_G1, S_G1_CLEANUP).setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        series().addSeries(C_GC, SC_GC_G1, S_G1_CONCCLEANUP).setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
    }
    
    private void createSeriesTime() {
        series().addSeries(C_TIME, SC_APP, S_TIME_STOPPED);
        series().addSeries(C_TIME, SC_APP, S_TIME_CONCURRENT);
    }

    // @Override
    public void createAllSeries() {
        // probe file whether this is unified logging format (JDK9+)
        try {
            fmark(1024*1024);
            String s;
            int i = 0;
            while ((s = readLine()) != null && i++ < 100) {
                if (unifiedLogging_abstime.matcher(s).matches() || unifiedLogging_reltime.matcher(s).matches()) {
                    Logger.log(Logger.LogType.debug, "Hotspot Parser - detected unified logging format");
                    isUnifiedLogging = true;
                    break;
                }
            }
            freset();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        if (isUnifiedLogging) {
            registerSeriesPattern("safepoint", "application", "time-running", Pattern.compile("Application time: ([0-9\\.]+) seconds"), false);
            registerSeriesPattern("safepoint", "application", "time-stopped", Pattern.compile("Total time for which application threads were stopped: ([0-9\\\\.]+) seconds.*"), false);
            registerSeriesPattern("gc", "pause-young", "time",    Pattern.compile("GC\\([0-9]+\\) Pause Young.* ([0-9\\\\.]+)ms"), false);
            registerSeriesPattern("gc", "pause-young", "cpu-usr", Pattern.compile("GC\\([0-9]+\\) User=([0-9\\.]+)s Sys=[0-9\\\\.]+s Real=[0-9\\\\.]+s"), 1000);
            registerSeriesPattern("gc", "pause-young", "cpu-sys", Pattern.compile("GC\\([0-9]+\\) User=[0-9\\.]+s Sys=([0-9\\\\.]+)s Real=[0-9\\\\.]+s"), 1000);
            registerSeriesPattern("gc", "heap", "heap-before",         Pattern.compile("GC\\([0-9]+\\) Pause Young.* ([0-9KMGT]+)->[0-9KMGT]+\\([0-9KMGT]+\\) [0-9\\\\.]+ms"), true);
            registerSeriesPattern("gc", "heap", "heap-after",          Pattern.compile("GC\\([0-9]+\\) Pause Young.* [0-9KMGT]+->([0-9KMGT]+)\\([0-9KMGT]+\\) [0-9\\\\.]+ms"), true);
            registerSeriesPattern("gc", "heap", "heap-capacity",       Pattern.compile("GC\\([0-9]+\\) Pause Young.* [0-9KMGT]+->[0-9KMGT]+\\(([0-9KMGT]+)\\) [0-9\\\\.]+ms"), true);
            registerSeriesPattern("gc", "heap", "heap-used",           Pattern.compile("GC\\([0-9]+\\) Pause Young.* ([0-9KMGT]+)->([0-9KMGT]+)\\([0-9KMGT]+\\) [0-9\\\\.]+ms"), true);
            parse();
        } else {
            // pre JDK9
            createSeriesHeap(SC_HEAP_EDEN);
            createSeriesHeap(SC_HEAP_SURVIVOR);
            createSeriesHeap(SC_HEAP_YOUNG);
            createSeriesHeap(SC_HEAP_OLD);
            createSeriesHeap(SC_HEAP_TOTAL);
            createSeriesHeap(SC_HEAP_PERM);
            createSeriesGC();
            createSeriesTime();
        }
    }
    
    
    // @Override
    public void parse() {
        if (!this.isUnifiedLogging) {
            parseOld();
            return;
        }

        // New unified logging (JDK9+);
        starttime = getCurrentTimeStamp().getTimeStamp();
        String s;
        while ((s = readLine()) != null) {
            long t = 0;
            Matcher m = unifiedLogging_abstime.matcher(s);
            if (m.matches()) {
                getCurrentTimeStamp().getTimeStampFromLine(m.group(1), null, 0, false);
                t = getCurrentTimeStamp().getTimeStamp();
            } else {
                m = unifiedLogging_reltime.matcher(s);
                if (m.matches()) {
                    String ts = m.group(1);
                    int p = ts.indexOf('.');
                    if (p > 0 && p + 1 < ts.length()) {
                        getCurrentTimeStamp().set(Long.parseLong(ts.substring(0, p)) * 1000
                                + Long.parseLong(ts.substring(p + 1)) + starttime);
                        t = getCurrentTimeStamp().getTimeStamp();
                    }
                }
            }
            if (t > 0) {
                s = m.group(2);
                int p = s.lastIndexOf(']');
                if (p >= 0 && p + 1 < s.length()) {
                    s = s.substring(p + 1);
                }
                s = s.trim();
                //Logger.log(Logger.LogType.debug, "Hotspot Parser - at " + t + ": " + s);
                addAllSamplesForRegisteredPatterns(t, s, false);
            } else {
                //Logger.log(Logger.LogType.debug, "Hotspot Parser - not matching: " + s);
            }
        }
        
        DataSeries ds;
        if (( ds = series().getSeries("gc", "pause-young", "time")) != null) {
            ds.setPreferredStyle(DataSeriesProperties.STYLE_POINTS);
        }
        if (( ds = series().getSeries("gc", "heap", "heap-used")) != null && ds.getNumberOfSamples() > 0) {
            // calculate object allocation rate
            DataSeries garbageRate = series().getOrAddSeries("gc", "objects", "garbage-rate");
            if (garbageRate.getNumberOfSamples() == 0) {
                long lastgc = 0;
                double bytesLast = 0;
                double bytesAfterGC = 0;
                for (int i = 0; i < ds.getNumberOfSamples(); i++) {
                    Sample sample = ds.getSample(i);
                    if (sample.getValue() < bytesLast) {
                        // bytes after GC
                        lastgc = sample.getTimeStamp();
                        bytesAfterGC = sample.getValue();
                    } else {
                        // bytes at GC
                        if (lastgc > 0) {
                            long timediff = sample.getTimeStamp() - lastgc;
                            if (timediff > 0) {
                                garbageRate.addSample(sample.getTimeStamp(), (sample.getValue()-bytesAfterGC) / (((double)timediff) / 1000));
                            }
                        }
                    }
                    bytesLast = sample.getValue();
                }
            }
        }
        
        series().setPreferredScaleMaxSame(true, true, false);
    }

    public void parseOld() {
        try {
            starttime = getCurrentTimeStamp().getTimeStamp();
            
            String str, laststr = null;
            long cms_start_time = 0;
            long cms_concurrent_start_time = 0;
            long t = 0;
            long lastYoungGcTime = 0;
            float cms_stw_total = 0.0f;
            float cms_concurrent_total = 0.0f;
            while( (str = readLine()) != null) {

                // strip input of survivor space distribution
                if (str.startsWith("Desired survivor size")) {
                    while ((str = readLine()) != null && !str.startsWith(","));
                    if (str != null && str.startsWith(",")) {
                        str = laststr + str;
                    }
                }
                laststr = str;

                Matcher m = timepat.matcher(str);
                if (m.matches()) {
                    getCurrentTimeStamp().set(Long.parseLong(m.group(1))*1000 + Long.parseLong(m.group(2)) + starttime);
                }
                t = getCurrentTimeStamp().getTimeStamp();
                
                m = parnewgc.matcher(str);
                if (m.matches()) {
                    float yh_before     = Float.parseFloat(m.group(3));
                    float yh_after      = Float.parseFloat(m.group(4));
                    float yh_capacity   = Float.parseFloat(m.group(5));
                    float h_before     = Float.parseFloat(m.group(7));
                    float h_after      = Float.parseFloat(m.group(8));
                    float h_capacity   = Float.parseFloat(m.group(9));
                    float gc_time = Float.parseFloat(m.group(10)) * 1000;
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_BEFORE, t, yh_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_AFTER, t, yh_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_CAPACITY, t, yh_capacity);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_BEFORE, t, h_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_AFTER, t, h_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, h_capacity);
                    series().addSampleIfNeeded(C_GC, SC_GC_PARNEW, S_PARNEW_PAUSE, t, gc_time);
                    if (t - lastYoungGcTime > 0 && yh_before > 0) {
                        series().addSampleIfNeeded(C_GC, SC_OBJECTS, S_ALLOC_RATE, t,  yh_before*1000.0 / (t - lastYoungGcTime));
                    }
                    lastYoungGcTime = t;
                }
                
                m = parnewgcPrintTenuringDistribution1.matcher(str);
                if (m.matches()) {
                    while (str != null && !parnewgcPrintTenuringDistribution2.matcher(str).matches()) {
                        str = readLine();
                    }
                    if (str != null) {
                        m = parnewgcPrintTenuringDistribution2.matcher(str);
                        if (m.matches()) {
                            float yh_before     = Float.parseFloat(m.group(1));
                            float yh_after      = Float.parseFloat(m.group(2));
                            float yh_capacity   = Float.parseFloat(m.group(3));
                            float h_before     = Float.parseFloat(m.group(5));
                            float h_after      = Float.parseFloat(m.group(6));
                            float h_capacity   = Float.parseFloat(m.group(7));
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_before);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_after);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_BEFORE, t, yh_before);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_AFTER, t, yh_after);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_CAPACITY, t, yh_capacity);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_before);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_after);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_BEFORE, t, h_before);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_AFTER, t, h_after);
                            series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, h_capacity);
                            if (t - lastYoungGcTime > 0 && yh_before > 0) {
                                series().addSampleIfNeeded(C_GC, SC_OBJECTS, S_ALLOC_RATE, t,  yh_before*1000.0 / (t - lastYoungGcTime));
                            }
                            lastYoungGcTime = t;
                        }
                    }
                    while (str != null && !parnewgcPrintTenuringDistribution3a.matcher(str).matches() &&
                                          !parnewgcPrintTenuringDistribution3b.matcher(str).matches()) {
                        str = readLine();
                    }
                    if (str != null) {
                        m = parnewgcPrintTenuringDistribution3a.matcher(str);
                        if (m.matches()) {
                            float gc_time = Float.parseFloat(m.group(1)) * 1000;
                            series().addSampleIfNeeded(C_GC, SC_GC_PARNEW, S_PARNEW_PAUSE, t, gc_time);
                        } else {
                            m = parnewgcPrintTenuringDistribution3b.matcher(str);
                            if (m.matches()) {
                                float gc_time = Float.parseFloat(m.group(1)) * 1000;
                                series().addSampleIfNeeded(C_GC, SC_GC_PARNEW, S_PARNEW_PAUSE, t, gc_time);
                            }
                        }
                    }
                }

                m = psnewgen.matcher(str);
                if (m.matches()) {
                    float yh_before = Float.parseFloat(m.group(1));
                    float yh_after = Float.parseFloat(m.group(2));
                    float yh_capacity = Float.parseFloat(m.group(3));
                    float h_before = Float.parseFloat(m.group(4));
                    float h_after = Float.parseFloat(m.group(5));
                    float h_capacity = Float.parseFloat(m.group(6));
                    float gc_time = Float.parseFloat(m.group(7)) * 1000;

                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_BEFORE, t, yh_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_AFTER, t, yh_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_CAPACITY, t, yh_capacity);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_BEFORE, t, h_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_AFTER, t, h_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, h_capacity);
                    series().addSampleIfNeeded(C_GC, SC_GC_PARNEW, S_PARNEW_PAUSE, t, gc_time);
                    if (t - lastYoungGcTime > 0 && yh_before > 0) {
                        series().addSampleIfNeeded(C_GC, SC_OBJECTS, S_ALLOC_RATE, t,  yh_before*1000.0 / (t - lastYoungGcTime));
                    }
                    lastYoungGcTime = t;
                }
                int fullgctype = 1;
                m = fullgc1.matcher(str);
                if (!m.matches()) {
                    m = fullgc2.matcher(str);
                    fullgctype = 2;
                }
                if (!m.matches()) {
                    m = fullgc3.matcher(str);
                    fullgctype = 3;
                }
                if (m.matches()) {
                    float yh_before = 0;
                    float yh_after = 0;
                    float yh_capacity = 0;
                    if (fullgctype == 1 || fullgctype == 2) {
                        yh_before = Float.parseFloat(m.group(1));
                        yh_after = Float.parseFloat(m.group(2));
                        yh_capacity = Float.parseFloat(m.group(3));
                    }
                    float oh_before = Float.parseFloat(m.group(4));
                    float oh_after = Float.parseFloat(m.group(5));
                    float oh_capacity = Float.parseFloat(m.group(6));
                    float h_before = Float.parseFloat(m.group(7));
                    float h_after = Float.parseFloat(m.group(8));
                    float h_capacity = Float.parseFloat(m.group(9));
                    float ph_before = Float.parseFloat(m.group(10));
                    float ph_after = Float.parseFloat(m.group(11));
                    float ph_capacity = Float.parseFloat(m.group(12));
                    float gc_time = Float.parseFloat(m.group(13)) * 1000;

                    if (fullgctype == 1 || fullgctype == 2) {
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_before);
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_USED, t, yh_after);
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_BEFORE, t, yh_before);
                    }
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_AFTER, t, yh_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_YOUNG, S_HEAP_CAPACITY, t, yh_capacity);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_BEFORE, t, h_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_AFTER, t, h_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, h_capacity);

                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_USED, t, oh_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_USED, t, oh_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_BEFORE, t, oh_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_AFTER, t, oh_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_CAPACITY, t, oh_capacity);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_PERM, S_HEAP_USED, t, ph_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_PERM, S_HEAP_USED, t, ph_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_PERM, S_HEAP_BEFORE, t, ph_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_PERM, S_HEAP_AFTER, t, ph_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_PERM, S_HEAP_CAPACITY, t, ph_capacity);

                    series().addSampleIfNeeded(C_GC, SC_GC_PAROLD, S_PAROLD_PAUSE, t, gc_time);

                    if (t - lastYoungGcTime > 0 && yh_before > 0) {
                        series().addSampleIfNeeded(C_GC, SC_OBJECTS, S_ALLOC_RATE, t,  yh_before*1000.0 / (t - lastYoungGcTime));
                    }
                    lastYoungGcTime = t;
                }
                
                m = cms.matcher(str);
                if (m.matches()) {
                    m = cms_initial_mark.matcher(str);
                    if (m.matches()) {
                        cms_stw_total = 0.0f;
                        cms_concurrent_total = 0.0f;
                        float duration      = Float.parseFloat(m.group(1)) * 1000;
                        series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_INITIALMARK, t, duration);
                        cms_start_time = t;
                        cms_stw_total += duration;
                    }
                    m = cms_remark.matcher(str);
                    if (m.matches()) {
                        float duration      = Float.parseFloat(m.group(1)) * 1000;
                        series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_REMARK, t, duration);
                        cms_stw_total += duration;
                        
                        if (cms_start_time != 0) {
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_STWTIME, cms_start_time, cms_stw_total);
                        }
                    }
                    m = cms_concurrent_start.matcher(str);
                    if (m.matches()) {
                        cms_concurrent_start_time = t;
                    }
                    m = cms_concurrent_end.matcher(str);
                    if (m.matches()) {
                        String operation     = m.group(1);
                        float duration      = Float.parseFloat(m.group(2)) * 1000;
                        if (operation.equals("mark")) {
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CONCMARK, cms_concurrent_start_time, duration);
                        }
                        if (operation.equals("preclean")) {
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CONCPRECLEAN, cms_concurrent_start_time, duration);
                        }
                        if (operation.equals("abortable-preclean")) {
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CONCABPRCLN, cms_concurrent_start_time, duration);
                        }
                        if (operation.equals("sweep")) {
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CONCSWEEP, cms_concurrent_start_time, duration);
                        }
                        if (operation.equals("reset")) {
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CONCRESET, cms_concurrent_start_time, duration);
                        }
                        cms_concurrent_total += duration;
                        
                        if (operation.equals("reset")) {
                            if (cms_start_time != 0) {
                                series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CYCLE, cms_start_time, 0.0f);
                                series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CYCLE, cms_start_time, 1.0f);
                            }
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CYCLE, t, 1.0f);
                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CYCLE, t, 0.0f);

                            series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_CONCTIME, cms_start_time, cms_concurrent_total);
                        }
                    }
                    m = cms_fullgc.matcher(str);
                    if (m.matches()) {
                        float h_before = Float.parseFloat(m.group(4));
                        float h_after = Float.parseFloat(m.group(5));
                        float h_capacity = Float.parseFloat(m.group(6));
                        float gc_time = Float.parseFloat(m.group(10)) * 1000;
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_before);
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, h_after);
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_BEFORE, t, h_before);
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_AFTER, t, h_after);
                        series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, h_capacity);
                        series().addSampleIfNeeded(C_GC, SC_GC_CMS, S_CMS_FULLGCTIME, t, gc_time);
                        
                    }
                }

                m = g1_pause_young.matcher(str);
                if (m.matches() && t>0) {
                    series().addSampleIfNeeded(C_GC, SC_GC_G1, S_G1_PSYOUNG, t, Float.parseFloat(m.group(1)) * 1000);
                }
                if (str.indexOf(g1_pause_ExtRootScanning) >= 0) {
                    findG1PauseDetails(str, S_G1_PSEXTROOTSCAN, t);
                }
                if (str.indexOf(g1_pause_UpdateRS) >= 0) {
                    findG1PauseDetails(str, S_G1_PSUPDRS, t);
                }
                if (str.indexOf(g1_pause_UpdateRSProcessedBuffers) >= 0) {
                    findG1PauseDetails(str, S_G1_PSUPDRSBUFS, t);
                }
                if (str.indexOf(g1_pause_ScanRS) >= 0) {
                    findG1PauseDetails(str, S_G1_PSSCANRS, t);
                }
                if (str.indexOf(g1_pause_ObjectCopy) >= 0) {
                    findG1PauseDetails(str, S_G1_PSOBJECTCOPY, t);
                }
                if (str.indexOf(g1_pause_Termination) >= 0) {
                    findG1PauseDetails(str, S_G1_PSTERMINATION, t);
                }
                if (str.indexOf(g1_pause_GcWorkerTime) >= 0) {
                    findG1PauseDetails(str, S_G1_PSGCWORKER, t);
                }
                if (str.indexOf(g1_pause_GcWorkerOther) >= 0) {
                    findG1PauseDetails(str, S_G1_PSGCWORKEROTR, t);
                }
                
                m = g1_pause_heap.matcher(str);
                if (m.matches() && t>0) {
                    double eden_before = Util.stringKMG2double(m.group(1));
                    double eden_cap_before = Util.stringKMG2double(m.group(2));
                    double eden_cap_after = Util.stringKMG2double(m.group(3));
                    double survivors_before = Util.stringKMG2double(m.group(4));
                    double survivors_after = Util.stringKMG2double(m.group(5));
                    double heap_before = Util.stringKMG2double(m.group(6));
                    double heap_cap_before = Util.stringKMG2double(m.group(7));
                    double heap_after = Util.stringKMG2double(m.group(8));
                    double heap_cap_after = Util.stringKMG2double(m.group(9));
                    double before = heap_before - survivors_before - eden_before;
                    double cap_before = heap_cap_before - survivors_before - eden_cap_before;
                    double after = heap_after - survivors_after;
                    double cap_after = heap_cap_after - survivors_after - eden_cap_after;
                    
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_EDEN, S_HEAP_USED, t, eden_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_EDEN, S_HEAP_USED, t, 0);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_EDEN, S_HEAP_BEFORE, t, eden_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_EDEN, S_HEAP_AFTER, t, 0);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_EDEN, S_HEAP_CAPACITY, t, eden_cap_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_EDEN, S_HEAP_CAPACITY, t, eden_cap_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_SURVIVOR, S_HEAP_USED, t, survivors_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_SURVIVOR, S_HEAP_USED, t, survivors_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_SURVIVOR, S_HEAP_BEFORE, t, survivors_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_SURVIVOR, S_HEAP_AFTER, t, survivors_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_USED, t, before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_USED, t, after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_BEFORE, t, before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_AFTER, t, after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_CAPACITY, t, cap_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_OLD, S_HEAP_CAPACITY, t, cap_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, heap_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, heap_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_BEFORE, t, heap_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_AFTER, t, heap_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, heap_cap_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, heap_cap_after);

                    if (t - lastYoungGcTime > 0 && eden_before > 0) {
                        series().addSampleIfNeeded(C_GC, SC_OBJECTS, S_ALLOC_RATE, t,  eden_before*1000.0 / (t - lastYoungGcTime));
                    }
                    lastYoungGcTime = t;
                }
                m = g1_full_gc.matcher(str);
                if (m.matches() && t>0) {
                    float heap_before = Float.parseFloat(m.group(1));
                    float heap_after = Float.parseFloat(m.group(2));
                    float heap_cap_after = Float.parseFloat(m.group(3));
                    float full_pause = Float.parseFloat(m.group(4)) * 1000;
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, heap_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_USED, t, heap_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_BEFORE, t, heap_before);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_AFTER, t, heap_after);
                    series().addSampleIfNeeded(C_HEAP, SC_HEAP_TOTAL, S_HEAP_CAPACITY, t, heap_cap_after);
                    series().addSampleIfNeeded(C_GC, SC_GC_G1, S_G1_PSFULL, t, full_pause);
                }
                
                m = g1_concurrent_mark.matcher(str);
                if (m.matches() && t>0) {
                    float elapsed = Float.parseFloat(m.group(1));
                    series().addSampleIfNeeded(C_GC, SC_GC_G1, S_G1_CONCMARK, t - ((long)elapsed*1000), elapsed);
                }
                m = g1_remark.matcher(str);
                if (m.matches() && t>0) {
                    series().addSampleIfNeeded(C_GC, SC_GC_G1, S_G1_REMARK, t, Float.parseFloat(m.group(1)));
                }
                m = g1_concurrent_count.matcher(str);
                if (m.matches() && t>0) {
                    float elapsed = Float.parseFloat(m.group(1));
                    series().addSampleIfNeeded(C_GC, SC_GC_G1, S_G1_CONCCOUNT, t - ((long)elapsed*1000), elapsed);
                }
                m = g1_cleanup.matcher(str);
                if (m.matches() && t>0) {
                    series().addSampleIfNeeded(C_GC, SC_GC_G1, S_G1_CLEANUP, t, Float.parseFloat(m.group(1)));
                }
                m = g1_concurrent_cleanup.matcher(str);
                if (m.matches() && t>0) {
                    float elapsed = Float.parseFloat(m.group(1));
                    series().addSampleIfNeeded(C_GC, SC_GC_G1, S_G1_CONCCLEANUP, t - ((long)elapsed*1000), elapsed);
                }
                
                m = timesStopped.matcher(str);
                if (m.matches() && t > 0) {
                    series().addSampleIfNeeded(C_TIME, SC_APP, S_TIME_STOPPED, t, Float.parseFloat(m.group(1)));
                }

                m = timesRunning.matcher(str);
                if (m.matches() && t > 0) {
                    series().addSampleIfNeeded(C_TIME, SC_APP, S_TIME_CONCURRENT, t, Float.parseFloat(m.group(1)));
                }
            }
            
            series().setPreferredScaleIndividual();
            
            // heap
            series().setPreferredScaleSame(new String[] {
                       S_HEAP_USED, S_HEAP_CAPACITY, S_HEAP_BEFORE, S_HEAP_AFTER });
            
            // minor pauses
            series().setPreferredScaleSame(new String[] {
                       S_PARNEW_PAUSE, S_G1_PSYOUNG });
            
            // major pauses
            series().setPreferredScaleSame(new String[] {
                       S_PAROLD_PAUSE, S_G1_PSFULL });
            
            // cms-stw
            series().setPreferredScaleSame(new String[] {
                       S_CMS_INITIALMARK, S_CMS_REMARK, S_CMS_STWTIME });
            
            // cms-concurrent
            series().setPreferredScaleSame(new String[] {
                       S_CMS_CONCMARK, S_CMS_CONCPRECLEAN, S_CMS_CONCABPRCLN, S_CMS_CONCSWEEP,
                       S_CMS_CONCRESET, S_CMS_CONCTIME });
            
            // cms-cycle
            series().setPreferredScaleIndividual(S_CMS_CYCLE);
            
            // g1
            series().setPreferredScaleSame(new String[] {
                       S_G1_PSEXTROOTSCAN, S_G1_PSUPDRS, S_G1_PSSCANRS, S_G1_PSOBJECTCOPY,
                       S_G1_PSTERMINATION, S_G1_PSGCWORKER, S_G1_PSGCWORKEROTR,
                       S_G1_CONCMARK, S_G1_CONCCOUNT, S_G1_CONCCLEANUP });
            
            // app time
            series().setPreferredScaleSame(new String[] {
                       S_TIME_STOPPED, S_TIME_CONCURRENT });
            
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
    private void findG1PauseDetails(String s, String series, long t) {
        while (s != null) {
            Matcher m;
            m = g1_pause_detailsAvgMinMaxDiff.matcher(s);
            if (m.matches()) {
                series().addSampleIfNeeded(C_GC, SC_GC_G1, series, t, Float.parseFloat(m.group(1)));
                return;
            }
            m = g1_pause_detailsSumAvgMinMaxDiff.matcher(s);
            if (m.matches()) {
                series().addSampleIfNeeded(C_GC, SC_GC_G1, series, t, Float.parseFloat(m.group(2)));
                return;
            }
            m = g1_pause_detailsMinAvgMaxDiffSum.matcher(s);
            if (m.matches()) {
                series().addSampleIfNeeded(C_GC, SC_GC_G1, series, t, Float.parseFloat(m.group(2)));
                return;
            }
            s = readLine();
        }
    }
    
}

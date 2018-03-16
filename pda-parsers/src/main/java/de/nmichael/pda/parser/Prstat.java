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
import de.nmichael.pda.util.Util;
import java.util.regex.*;
import java.util.*;
import java.io.*;

public class Prstat extends Parser {
    
    private static final int MAX_PROCESSES = 8192;
    private static final String PARAM_THRESHOLD = "Selection Threshold usr+sys [0-100]";
    private static final String PRSTAT_USR = "usr";
    private static final String PRSTAT_SYS = "sys";
    private static final String PRSTAT_TRP = "trp";
    private static final String PRSTAT_TFL = "tfl";
    private static final String PRSTAT_DFL = "dfl";
    private static final String PRSTAT_LCK = "lck";
    private static final String PRSTAT_SLP = "slp";
    private static final String PRSTAT_LAT = "lat";
    private static final String PRSTAT_VCX = "vcx";
    private static final String PRSTAT_ICX = "icx";
    private static final String PRSTAT_SCL = "scl";
    private static final String PRSTAT_SIG = "sig";
    private static final String PRSTAT_CPU = "cpu";
    
    private int minThreshold = 0;
    private Hashtable<String,String> pid2args = new Hashtable<String,String>();

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"prstat");
    }
    
    public Prstat() {
        super("prstat");
        setParameter(PARAM_THRESHOLD, "0");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS,
                null,
                "prstat",
                new String[][] { { "-mL" } },
                "Thread Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
    
    private boolean createSeries(String category, String subcategory) {
        if (series().getNumberOfSeries(category, subcategory) > 0) {
            return false; // series already created
        }
        series().addSeries(category, subcategory, PRSTAT_USR);
        series().addSeries(category, subcategory, PRSTAT_SYS);
        series().addSeries(category, subcategory, PRSTAT_TRP);
        series().addSeries(category, subcategory, PRSTAT_TFL);
        series().addSeries(category, subcategory, PRSTAT_DFL);
        series().addSeries(category, subcategory, PRSTAT_LCK);
        series().addSeries(category, subcategory, PRSTAT_SLP);
        series().addSeries(category, subcategory, PRSTAT_LAT);
        series().addSeries(category, subcategory, PRSTAT_VCX);
        series().addSeries(category, subcategory, PRSTAT_ICX);
        series().addSeries(category, subcategory, PRSTAT_SCL);
        series().addSeries(category, subcategory, PRSTAT_SIG);
        series().addSeries(category, subcategory, PRSTAT_CPU);
        return true;
    }
    
    @Override
    public void createAllSeries() {
        getProcessNames();
        Pattern p = Pattern.compile(" *(\\d+) +[^ ]+ +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +([\\d+\\.KMG]+) +([\\d+\\.KMG]+) +([\\d+\\.KMG]+) +([\\d+\\.KMG]+) +(.*)");
        try {
            int count = 0;
            String s;
            while( (s = readLine(true)) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String pid = m.group(1);
                    String name = m.group(14);
                    String args = pid2args.get(pid);
                    String category = getProcessName(name, args);
                    String subcategory = getPidAndTid(name, pid);
                    if (createSeries(category, subcategory) && ++count >= MAX_PROCESSES) {
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
        long lastTs = 0;
        long elapsed = 0;
        
        Pattern p = Pattern.compile(" *(\\d+) +[^ ]+ +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +(\\d+\\.?\\d*) +([\\d+\\.KMG]+) +([\\d+\\.KMG]+) +([\\d+\\.KMG]+) +([\\d+\\.KMG]+) +(.*)");
        setNewSamplesHeader("PID USERNAME");
        boolean first = true;
        boolean ignore = true;
        try {
            String s;
            while( (s = readLine(true)) != null) {
                if (s.indexOf("PID USERNAME") >= 0) {
                    if (first) {
                        first = false;
                        ignore = true;
                    } else {
                        ignore = false;
                    }
                    continue;
                }
                Matcher m = p.matcher(s);
                if (!ignore && m.matches()) {
                    String pid = m.group(1);
                    String name = m.group(14);
                    String args = pid2args.get(pid);
                    String category = getProcessName(name, args);
                    String subcategory = getPidAndTid(name, pid);
                    
                    if (minThreshold == 0 || Float.parseFloat(m.group(2))+Float.parseFloat(m.group(3)) >= minThreshold) {
                        long t = getCurrentTimeStamp().getTimeStamp();
                        if (t != lastTs) {
                            elapsed = Math.abs(t - lastTs) / 1000;
                            lastTs = t;
                            if (elapsed == 0 || elapsed > 1000) {
                                elapsed = 1; // large elapsed in case of initial sample
                            }
                        }
                        if (series().getNumberOfSeries(category, subcategory) == 0) {
                            createSeries(category, subcategory);
                        }
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_USR, t, Float.parseFloat(m.group(2)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_SYS, t, Float.parseFloat(m.group(3)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_TRP, t, Float.parseFloat(m.group(4)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_TFL, t, Float.parseFloat(m.group(5)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_DFL, t, Float.parseFloat(m.group(6)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_LCK, t, Float.parseFloat(m.group(7)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_SLP, t, Float.parseFloat(m.group(8)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_LAT, t, Float.parseFloat(m.group(9)));
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_VCX, t, Util.stringKMG2double(m.group(10)) / elapsed);
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_ICX, t, Util.stringKMG2double(m.group(11)) / elapsed);
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_SCL, t, Util.stringKMG2double(m.group(12)) / elapsed);
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_SIG, t, Util.stringKMG2double(m.group(13)) / elapsed);
                        series().addSampleIfNeeded(category, subcategory, PRSTAT_CPU, t, Float.parseFloat(m.group(2))+Float.parseFloat(m.group(3)));
                    }
                }
            }
            series().setPreferredScaleSame(false, false, true);
            series().setPreferredScaleSeries(PRSTAT_USR, 0, 100);
            series().setPreferredScaleSeries(PRSTAT_SYS, 0, 100);
            series().setPreferredScaleSeries(PRSTAT_TRP, 0, 100);
            series().setPreferredScaleSeries(PRSTAT_TFL, 0, 100);
            series().setPreferredScaleSeries(PRSTAT_DFL, 0, 100);
            series().setPreferredScaleSeries(PRSTAT_LCK, 0, 100);
            series().setPreferredScaleSeries(PRSTAT_SLP, 0, 100);
            series().setPreferredScaleSeries(PRSTAT_LAT, 0, 100);
            series().setPreferredScaleIndividual(PRSTAT_VCX);
            series().setPreferredScaleIndividual(PRSTAT_ICX);
            series().setPreferredScaleIndividual(PRSTAT_SCL);
            series().setPreferredScaleIndividual(PRSTAT_SIG);
            series().setPreferredScaleSeries(PRSTAT_CPU, 0, 100);
            
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
    
    private static String getProcessName(String name, String args) {
        if (args != null) {
            return args;
        }
        int pos = name.lastIndexOf("/");
        if (pos > 0) {
            return name.substring(0, pos);
        } else {
            return name;
        }
    }

    private static String getPidAndTid(String name, String pid) {
        int pos = name.lastIndexOf("/");
        if (pos > 0) {
            String tid = Util.stringOfLength(name.substring(pos+1), 3, '0', false);
            return Util.stringOfLength(pid, 5, '0', false) + "/" + tid;
        } else {
            return Util.stringOfLength(pid, 5, '0', false);
        }
    }
    
    private void getProcessNames() {
        try {
            String fname = getFilename();
            int pos = fname.toLowerCase().indexOf("prstat");
            if (pos >= 0) {
                fname = (pos > 0 ? fname.substring(0, pos) : "") + "ps" +
                        (fname.length() > pos+6 ? fname.substring(pos+6) : "");
                if (!(new File(fname)).exists()) {
                    return;
                }
            } else {
                return;
            }
            BufferedReader f = new BufferedReader(new FileReader(fname));
            String s;
            // standard ps output (same as ps parser)
            Pattern p1 = Pattern.compile(" *(\\d+) +\\d+ +\\d*-?\\d*:?\\d+:\\d+ +\\d+ +\\d+ +\\d+ +[^ ]+ +\\d+ +([^ ]+).*");
            //  0 O   oracle 25987     1   1  39  0        ? 42639776          12:38:10 ?           7:39 ora_lgwr_orcl
            Pattern p2 = Pattern.compile(" *. . +[^ ]+ +(\\d+).*\\d+:\\d+ +([^ ]+).*");
            // ps -ef:   oracle  3043     1   0 20:45:59 ?           0:09 asm_lgwr_+ASM
            Pattern p3 = Pattern.compile(" *[^ ]+ +(\\d+).*\\d+:\\d+ +([^ ]+).*");
            while ( (s = f.readLine()) != null) {
                Matcher m = p1.matcher(s);
                if (!m.matches()) {
                    m = p2.matcher(s);
                }
                if (!m.matches()) {
                    m = p3.matcher(s);
                }
                if (m.matches()) {
                    String pid = m.group(1);
                    String args = m.group(2);
                    pos = args.indexOf(" ");
                    if (pos > 0) {
                        args = args.substring(0, pos);
                    }
                    pid2args.put(pid, args);
                }
            }
            f.close();
        } catch (Exception e) {
        }
    }
    
}
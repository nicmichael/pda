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
import de.nmichael.pda.util.*;
import java.util.regex.*;
import java.util.*;
import java.io.*;

public class Mpstat extends Parser {
    
    private static final String PARAM_IGNORE_AFTER_LINE = "Ignore each Sample after Line";
    private static final String PARAM_CPUS_PER_CORE = "CPUs per Core";
    
    private static final String SOLARIS_MINF  = "minf";
    private static final String SOLARIS_MJF   = "mjf";
    private static final String SOLARIS_XCAL  = "xcal";
    private static final String SOLARIS_INTR  = "intr";
    private static final String SOLARIS_ITHR  = "ithr";
    private static final String SOLARIS_CSW   = "csw";
    private static final String SOLARIS_ICSW  = "icsw";
    private static final String SOLARIS_MIGR  = "migr";
    private static final String SOLARIS_SMTX  = "smtx";
    private static final String SOLARIS_SRW   = "srw";
    private static final String SOLARIS_SYSCL = "syscl";
    private static final String SOLARIS_USR   = "usr";
    private static final String SOLARIS_SYS   = "sys";
    private static final String SOLARIS_IDL   = "idl";
    private static final String SOLARIS_CPU   = "cpu";

    private static final String LINUX_CPU   = "cpu";

    private static final String CATEGORY_TOTAL  = "all";
    private static final String CATEGORY_CPU    = "cpu";
    private static final String CATEGORY_CORE   = "core";
    private static final String CATEGORY_THREAD = "thread";
    
    private boolean isLinux = false; // Solaris or Linux
    private int numberOfCpus = 1;
    private String ignoreSampleAfterLine = "";
    private int cpusPerCore = 8;
    private String[] linuxHeaders;
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"mpstat");
    }
    
    public Mpstat() {
        super("mpstat");
        setParameter(PARAM_IGNORE_AFTER_LINE, "");
        setParameter(PARAM_CPUS_PER_CORE, "8");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + " / " + FileFormatDescription.PRODUCT_LINUX,
                null,
                "mpstat",
                new String[][] { { "[-p] (Solaris)", "-P ALL (Linux)" } },
                "CPU Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
    
    private void createSeries(String category, String subcategory) {
        if (!isLinux) {
            series().addSeries(category, subcategory, SOLARIS_MINF);
            series().addSeries(category, subcategory, SOLARIS_MJF);
            series().addSeries(category, subcategory, SOLARIS_XCAL);
            series().addSeries(category, subcategory, SOLARIS_INTR);
            series().addSeries(category, subcategory, SOLARIS_ITHR);
            series().addSeries(category, subcategory, SOLARIS_CSW);
            series().addSeries(category, subcategory, SOLARIS_ICSW);
            series().addSeries(category, subcategory, SOLARIS_MIGR);
            series().addSeries(category, subcategory, SOLARIS_SMTX);
            series().addSeries(category, subcategory, SOLARIS_SRW);
            series().addSeries(category, subcategory, SOLARIS_SYSCL);
            series().addSeries(category, subcategory, SOLARIS_USR);
            series().addSeries(category, subcategory, SOLARIS_SYS);
            series().addSeries(category, subcategory, SOLARIS_IDL);
            series().addSeries(category, subcategory, SOLARIS_CPU);
        } else {
            for (String s : linuxHeaders) {
                series().addSeries(category, subcategory, s);
            }
            series().addSeries(category, subcategory, LINUX_CPU);
        }
    }
    
    private void addSamples(String category, String subcategory, long ts, 
            double[] values, int aggrCount) {
        if (!isLinux && aggrCount > 1) {
            values[11] /= aggrCount;
            values[12] /= aggrCount;
            values[13] /= aggrCount;
            values[14] /= aggrCount;
        }
        if (!isLinux) {
            series().addSampleIfNeeded(category, subcategory, SOLARIS_MINF, ts, values[0]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_MJF,  ts, values[1]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_XCAL, ts, values[2]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_INTR, ts, values[3]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_ITHR, ts, values[4]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_CSW,  ts, values[5]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_ICSW, ts, values[6]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_MIGR, ts, values[7]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_SMTX, ts, values[8]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_SRW,  ts, values[9]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_SYSCL,ts, values[10]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_USR,  ts, values[11]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_SYS,  ts, values[12]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_IDL,  ts, values[13]);
            series().addSampleIfNeeded(category, subcategory, SOLARIS_CPU,  ts, values[14]);
        } else {
            double idle = 100;
            for (int i=0; i<linuxHeaders.length; i++) {
                series().addSampleIfNeeded(category, subcategory, linuxHeaders[i],   ts, values[i]);
                if (linuxHeaders[i].equalsIgnoreCase("idle")) {
                    idle = values[i];
                }
            }
            series().addSampleIfNeeded(category, subcategory, LINUX_CPU,    ts, 100.0-idle);
        }
        if (aggrCount > 1) {
            Arrays.fill(values, 0);
        }
    }

    @Override
    public void createAllSeries() {
        try {
            String s;
            Pattern pSolaris  = Pattern.compile("^ *([0-9]+) .*");
            Pattern pLinux    = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9] .. +([0-9]+) .*");
            Pattern pLinuxHdr = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9] .. +CPU +(.+)");
            int cnt = 0;
            while( (s = readLine(false)) != null) {
                s = s.trim();
                if (s.startsWith("Linux")) { // e.g.: Linux 2.6.18-194.el5 (hostname)        09/07/2010
                    isLinux = true;
                    cpusPerCore = 0; // we don't really know how to translate CPU IDs to cores on Linux
                }
                Matcher m = pSolaris.matcher(s);
                if (!m.matches()) {
                    m = pLinux.matcher(s);
                }
                if (m.matches()) {
                    int cpuid = Integer.parseInt(m.group(1));
                    createSeries(CATEGORY_CPU, Integer.toString(cpuid));
                    if (cpusPerCore > 1) {
                        createSeries(CATEGORY_CORE, Integer.toString(cpuid / cpusPerCore));
                        createSeries(CATEGORY_THREAD, Integer.toString(cpuid % cpusPerCore));
                    }
                }
                if (isLinux && linuxHeaders == null && (m = pLinuxHdr.matcher(s)).matches()) {
                    linuxHeaders = m.group(1).trim().split(" +");
                    for (int i=0; i<linuxHeaders.length; i++) {
                        if (linuxHeaders[i].startsWith("%")) {
                            linuxHeaders[i] = linuxHeaders[i].substring(1);
                        }
                    }
                }
                if (s.indexOf("CPU ") >= 0 && ++cnt >= 2) {
                    break;
                }
            }
            numberOfCpus = series().getNumberOfSubcategories(CATEGORY_CPU);
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (series().size() > 0) {
            createSeries(CATEGORY_TOTAL ,"");
        }
    }
            
    // @Override
    public void parse() {
        boolean firstHeader = true;
        Pattern p = ( isLinux ?
            Pattern.compile("([0-9][0-9]):([0-9][0-9]):([0-9][0-9]) (..) +([^ ]+) +(.+)")
            : 
            // typically mpstat looks like this:
            // CPU minf mjf xcal  intr ithr  csw icsw migr smtx  srw syscl  usr sys  wt idl
            // with -p option, mpstat has an additional "set" column
            // CPU minf mjf xcal  intr ithr  csw icsw migr smtx  srw syscl  usr sys  wt idl set
            Pattern.compile(" *([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +[0-9]+ +([0-9]+) *[0-9]*") );
        if (!isLinux) {
            setNewSamplesHeader("CPU minf");
        }
        try {
            String s;
            double[] allvalues = new double[ (isLinux ? linuxHeaders.length : 15) ];
            double[] cpuvalues = new double[ (isLinux ? linuxHeaders.length : 15) ];
            double[] corevalues = cpusPerCore > 1 ? new double[ (isLinux ? linuxHeaders.length : 15) ] : null;
            double[][] threadvalues = cpusPerCore > 1 ? new double[cpusPerCore][ (isLinux ? linuxHeaders.length : 15) ] : null;
            long t = 0;
            int lastCpu = 0;
            int lastCore = 0;
            while( (s = readLine(false)) != null) {
                s = s.trim();
                if (ignoreSampleAfterLine != null && ignoreSampleAfterLine.length() > 0 &&
                    s.startsWith(ignoreSampleAfterLine)) {
                    firstHeader = true;
                    s = readLine(true); // read next line
                    if (s != null) {
                        s = s.trim();
                    } else {
                        break;
                    }
                }
                if (!isLinux && s.startsWith("CPU ")) {
                    if (firstHeader) {
                        String ss;
                        while ( (ss = readLine(true)) != null && !ss.trim().startsWith("CPU "));                        
                    }
                    firstHeader = false;
                    t = getCurrentTimeStamp().getTimeStamp();
                }
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    int cpuid = -1;
                    if (isLinux) {
                        // Linux
                        if (m.group(5).equals("CPU")) {
                            continue;
                        } else if (m.group(5).equals("all")) {
                            cpuid = -1;
                        } else {
                            cpuid = Util.string2int(m.group(5), -1);
                        }
                        String[] values = m.group(6).trim().split(" +");
                        for (int i = 0; i < cpuvalues.length; i++) {
                            cpuvalues[i] = Float.parseFloat(values[i]);
                            allvalues[i] += cpuvalues[i];
                            if (corevalues != null) {
                                corevalues[i] += cpuvalues[i];
                            }
                            if (threadvalues != null) {
                                threadvalues[i % cpusPerCore][i] += cpuvalues[i];
                            }
                        }
                    } else {
                        // Solaris
                        cpuid = Util.string2int(m.group(1), -1);
                        for (int i = 0; i < cpuvalues.length; i++) {
                            cpuvalues[i] = (i < cpuvalues.length-1 ? Integer.parseInt(m.group(i + 2)) : 100 - cpuvalues[i-1]);
                            allvalues[i] += cpuvalues[i];
                            if (corevalues != null) {
                                corevalues[i] += cpuvalues[i];
                            }
                            if (threadvalues != null) {
                                threadvalues[cpuid % cpusPerCore][i] += cpuvalues[i];
                            }
                        }
                    }
                    
                    int coreid = cpuid / (cpusPerCore > 0 ? cpusPerCore : 1);
                    if (!isLinux && (cpuid == -1 || cpuid < lastCpu)) {
                        addSamples(CATEGORY_TOTAL, "", getCurrentTimeStamp().getTimeStamp(), allvalues, numberOfCpus);
                    }
                    if (cpuid != -1 && cpusPerCore > 1) {
                        if (coreid != lastCore) {
                            addSamples(CATEGORY_CORE, Integer.toString(coreid), getCurrentTimeStamp().getTimeStamp(), corevalues, cpusPerCore);
                        }
                        if (cpuid < lastCpu) {
                            for (int j=0; j<threadvalues.length; j++) {
                                addSamples(CATEGORY_THREAD, Integer.toString(j), getCurrentTimeStamp().getTimeStamp(), threadvalues[j], numberOfCpus / cpusPerCore);
                            }
                        }
                    }
                    addSamples(cpuid == -1 ? CATEGORY_TOTAL : CATEGORY_CPU, 
                            cpuid == -1 ? "" : Integer.toString(cpuid), t > 0 ? t : getCurrentTimeStamp().getTimeStamp(), cpuvalues, 1);
                    lastCpu = cpuid;
                    lastCore = coreid;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (!isLinux) {
            series().setPreferredScaleSame(false, false, true);
            series().setPreferredScaleSeries(SOLARIS_USR, 0, 100);
            series().setPreferredScaleSeries(SOLARIS_SYS, 0, 100);
            series().setPreferredScaleSeries(SOLARIS_IDL, 0, 100);
            series().setPreferredScaleSeries(SOLARIS_CPU, 0, 100);
        } else {
            series().setPreferredScaleAll(0, 100);
        }
    }
    
     public void setParameter(String name, String value) {
        super.setParameter(name, value);
        if (PARAM_IGNORE_AFTER_LINE.equals(name)) {
            try {
                this.ignoreSampleAfterLine = value;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (PARAM_CPUS_PER_CORE.equals(name)) {
            try {
                this.cpusPerCore = Util.string2int(value, cpusPerCore);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
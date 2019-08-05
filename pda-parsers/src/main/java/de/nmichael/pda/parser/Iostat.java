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

public class Iostat extends Parser {
    
    private static final String IO_RS       = "r/s";
    private static final String IO_WS       = "w/s";
    private static final String IO_KRS      = "kr/s";
    private static final String IO_KWS      = "kw/s";
    private static final String IO_WAIT     = "wait";
    private static final String IO_ACTV     = "actv";
    private static final String IO_WSVCT    = "wsvc_t";
    private static final String IO_ASVCT    = "asvc_t";
    private static final String IO_RAWAIT   = "r_await";
    private static final String IO_WAWAIT   = "w_await";
    private static final String IO_PCTW     = "%w";
    private static final String IO_PCTB     = "%b";
    private static final String IO_KBPR     = "kb/r";
    private static final String IO_KBPW     = "kb/w";
    private static final String IO_ACTASVCT = "act_asvc_t";
    private static final String IO_IOPS     = "iops";
    
    private static final String CATEGORY_TOTAL = "all";
    private static final String CATEGORY_DISK  = "disk";
    private static final String CATEGORY_CPU  = "cpu";

    private static final String PARAM_ALLOW_DUPLICATE_DISKS = "Allow duplicate (identical) Disk Names";

    private Pattern p1 = Pattern.compile("([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),([0-9]+\\.?[0-9]*),(.+)"); // -xnr
    private Pattern p2 = Pattern.compile(" *([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +(.+)"); // -xn
    private Pattern p3 = Pattern.compile("([^ ]+) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) +([0-9]+\\.?[0-9]*) *"); // -x (device first, wsvc_t and asvc_t merged into svc_t)
    private Pattern pL = Pattern.compile("([^ ]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+)");
    private Pattern pL2 = Pattern.compile("([^ ]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\\\.]+) +([0-9\\\\.]+)");
    private Pattern pLcpu = Pattern.compile(" +([0-9\\.]+)  +([0-9\\\\.]+) +([0-9\\\\.]+) +([0-9\\\\.]+) +([0-9\\\\.]+) +([0-9\\\\.]+)");
    private boolean p23_deviceLast = true;
    private boolean isLinux = false;
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"iostat");
    }
    
    public Iostat() {
        super("iostat");
        getCurrentTimeStamp().addTimeStampPattern("UNIXMILLISECONDS", 
                                                  Pattern.compile("(\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d+)"), 
                                                  new TimeStamp.Fields[] { TimeStamp.Fields.unixms });
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + " / " + FileFormatDescription.PRODUCT_LINUX, 
                null,
                "iostat",
                new String[][] { { "-xn", "-xnr", "-x", "-xkt (Linux)" },
                                 { "-T d", "-T u" } },
                "Disk I/O Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
    
    private void createSeries(String category, String subcategory) {
        series().addSeries(category, subcategory, IO_RS);
        series().addSeries(category, subcategory, IO_WS);
        series().addSeries(category, subcategory, IO_KRS);
        series().addSeries(category, subcategory, IO_KWS);
        series().addSeries(category, subcategory, IO_WAIT);
        series().addSeries(category, subcategory, IO_ACTV);
        series().addSeries(category, subcategory, IO_WSVCT);
        series().addSeries(category, subcategory, IO_ASVCT);
        series().addSeries(category, subcategory, IO_PCTW);
        series().addSeries(category, subcategory, IO_PCTB);
        series().addSeries(category, subcategory, IO_KBPR);
        series().addSeries(category, subcategory, IO_KBPW);
        series().addSeries(category, subcategory, IO_ACTASVCT);
        series().addSeries(category, subcategory, IO_IOPS);
    }
    
    private void addSamples(String category, String subcategory, long ts, double[] values) {
        series().addSampleIfNeeded(category, subcategory, IO_RS, ts, values[0]);
        series().addSampleIfNeeded(category, subcategory, IO_WS, ts, values[1]);
        series().addSampleIfNeeded(category, subcategory, IO_KRS, ts, values[2]);
        series().addSampleIfNeeded(category, subcategory, IO_KWS, ts, values[3]);
        series().addSampleIfNeeded(category, subcategory, IO_WAIT, ts, values[4]);
        series().addSampleIfNeeded(category, subcategory, IO_ACTV, ts, values[5]);
        series().addSampleIfNeeded(category, subcategory, IO_WSVCT, ts, values[6]);
        series().addSampleIfNeeded(category, subcategory, IO_ASVCT, ts, values[7]);
        series().addSampleIfNeeded(category, subcategory, IO_PCTW, ts, values[8]);
        series().addSampleIfNeeded(category, subcategory, IO_PCTB, ts, values[9]);
        series().addSampleIfNeeded(category, subcategory, IO_KBPR, ts, values[10]);
        series().addSampleIfNeeded(category, subcategory, IO_KBPW, ts, values[11]);
        series().addSampleIfNeeded(category, subcategory, IO_ACTASVCT, ts, values[12]);
        series().addSampleIfNeeded(category, subcategory, IO_IOPS, ts, values[13]);
    }

    @Override
    public void createAllSeries() {
        createSeries(CATEGORY_TOTAL ,"");
        boolean foundCpu = false;
        try {
            String s;
            int cnt = 0;
            while( (s = readLine(true)) != null) {
                if (s.startsWith("device ")) {
                    p23_deviceLast = false;
                }
                Matcher m = p1.matcher(s);
                if (!m.matches()) {
                    if (p23_deviceLast) {
                        m = p2.matcher(s);
                    } else {
                        m = p3.matcher(s);
                    }
                }
                if (m.matches()) {
                    if (p23_deviceLast) {
                        createSeries(CATEGORY_DISK, m.group(11));
                    } else {
                        createSeries(CATEGORY_DISK, m.group(1));
                    }
                } else {
                    // Linux
                    m = pL.matcher(s);
                    if (m.matches()) {
                        isLinux = true;
                        createSeries(CATEGORY_DISK, m.group(1));
                    } else {
                        m = pL2.matcher(s);
                        if (m.matches()) {
                            isLinux = true;
                            createSeries(CATEGORY_DISK, m.group(1));
                            series().getOrAddSeries(CATEGORY_DISK, m.group(1), IO_RAWAIT);
                            series().getOrAddSeries(CATEGORY_DISK, m.group(1), IO_WAWAIT);
                        }
                    }
                }
                m = pLcpu.matcher(s);
                if (m.matches()) {
                    if (!foundCpu) {
                        series().addSeries(CATEGORY_CPU, "", "usr");
                        series().addSeries(CATEGORY_CPU, "", "sys");
                        series().addSeries(CATEGORY_CPU, "", "idl");
                        series().addSeries(CATEGORY_CPU, "", "cpu");
                    }
                    foundCpu = true;
                }
                
                if (cnt++ == 1000) {
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // @Override
    public void parse() {
        boolean first = true;
        boolean ignore = true;
        if (isLinux) {
            setNewSamplesHeader("Filesystem:");
        } else {
            setNewSamplesHeader("extended device statistics");
        }
        try {
            String s;
            long t = 0;
            int allcnt = 0;
            double[] allvalues = new double[14];
            double[] values = new double[14];
            while( (s = readLine(true)) != null) {
                if (s.indexOf("extended device statistics")>=0 ||
                    s.indexOf("Device:")>=0) {
                    if (allcnt > 0) {
                        allvalues[6] /= (double)allcnt;
                        allvalues[7] /= (double)allcnt;
                        allvalues[8] /= (double)allcnt;
                        allvalues[9] /= (double)allcnt;
                        allvalues[10] = (allvalues[0] > 0 ? allvalues[2] / allvalues[0] : 0); // kb/r
                        allvalues[11] = (allvalues[1] > 0 ? allvalues[3] / allvalues[1] : 0); // kb/w
                        allvalues[12] = (allvalues[1] > 10 ? allvalues[7] : 0);  // asvc_t only for active (write) LUNs
                        allvalues[13] = allvalues[0] + allvalues[1];
                        addSamples(CATEGORY_TOTAL, "", t, allvalues);
                    }
                    allcnt = 0;
                    Arrays.fill(allvalues, 0);
                    if (first) {
                        first = false;
                        ignore = true;
                    } else {
                        ignore = false;
                    }
                    continue;
                }
                
                Matcher m = null;
                if (!isLinux) {
                    m = p1.matcher(s);
                    if (!m.matches()) {
                        if (p23_deviceLast) {
                            m = p2.matcher(s);
                        } else {
                            m = p3.matcher(s);
                        }
                    }
                }
                if (!ignore) {
                    if (!isLinux && m.matches()) {
                        t = getCurrentTimeStamp().getTimeStamp();
                        String device = (p23_deviceLast ? m.group(11) : m.group(1));
                        if (p23_deviceLast) {
                            for (int i = 0; i < 10; i++) {
                                values[i] = Float.parseFloat(m.group(i+1));
                            }
                        } else {
                            for (int i = 0; i < 9; i++) {
                                values[i<=6 ? i : i+1] = Float.parseFloat(m.group(i+2));
                            }
                        }
                        values[10] = (values[0] > 0 ? values[2] / values[0] : 0); // kb/r
                        values[11] = (values[1] > 0 ? values[3] / values[1] : 0); // kb/w
                        values[12] = (values[1] > 10 ? values[7] : 0);  // asvc_t only for active (write) LUNs
                        values[13] = values[0] + values[1];
                        addSamples(CATEGORY_DISK, device, t, values);
                        for (int i=0; i<values.length; i++) {
                            allvalues[i] += values[i];
                        }
                        allcnt++;
                    } else {
                        // Linux
                        m = pL.matcher(s);
                        if (m.matches()) {
                            t = getCurrentTimeStamp().getTimeStamp();
                            String device = m.group(1);
                            values[0] = Float.parseFloat(m.group(4));  // r/s
                            values[1] = Float.parseFloat(m.group(5));  // w/s
                            values[2] = Float.parseFloat(m.group(6));  // rKb/s
                            values[3] = Float.parseFloat(m.group(7));  // wKb/s
                            values[6] = Float.parseFloat(m.group(10)); // IO_WSVCT: await
                            values[7] = Float.parseFloat(m.group(11)); // IO_ASVCT: svctm
                            values[9] = Float.parseFloat(m.group(12)); // IO_PCTB:  %util
                            values[10] = (values[0] > 0 ? values[2] / values[0] : 0); // kb/r
                            values[11] = (values[1] > 0 ? values[3] / values[1] : 0); // kb/w
                            values[12] = (values[1] > 10 ? values[7] : 0);  // asvc_t only for active (write) LUNs
                            values[13] = values[0] + values[1];
                            addSamples(CATEGORY_DISK, m.group(1), t, values);
                            for (int i = 0; i < values.length; i++) {
                                allvalues[i] += values[i];
                            }
                            allcnt++;
                        } else {
                            m = pL2.matcher(s);
                            if (m.matches()) {
                                t = getCurrentTimeStamp().getTimeStamp();
                                String device = m.group(1);
                                values[0] = Float.parseFloat(m.group(4));  // r/s
                                values[1] = Float.parseFloat(m.group(5));  // w/s
                                values[2] = Float.parseFloat(m.group(6));  // rKb/s
                                values[3] = Float.parseFloat(m.group(7));  // wKb/s
                                values[6] = Float.parseFloat(m.group(10)); // IO_WSVCT: await
                                values[7] = Float.parseFloat(m.group(13)); // IO_ASVCT: svctm
                                values[9] = Float.parseFloat(m.group(14)); // IO_PCTB:  %util
                                values[10] = (values[0] > 0 ? values[2] / values[0] : 0); // kb/r
                                values[11] = (values[1] > 0 ? values[3] / values[1] : 0); // kb/w
                                values[12] = (values[1] > 10 ? values[7] : 0);  // asvc_t only for active (write) LUNs
                                values[13] = values[0] + values[1];
                                addSamples(CATEGORY_DISK, m.group(1), t, values);
                                series().getOrAddSeries(CATEGORY_DISK, m.group(1), IO_RAWAIT).addSampleIfNeeded(t, Float.parseFloat(m.group(11)));
                                series().getOrAddSeries(CATEGORY_DISK, m.group(1), IO_WAWAIT).addSampleIfNeeded(t, Float.parseFloat(m.group(12)));
                                for (int i = 0; i < values.length; i++) {
                                    allvalues[i] += values[i];
                                }
                                allcnt++;
                            }
                        }
                        m = pLcpu.matcher(s);
                        if (m.matches()) {
                            t = getCurrentTimeStamp().getTimeStamp();
                            series().addSampleIfNeeded(CATEGORY_CPU, "", "usr", t, Float.parseFloat(m.group(1)));
                            series().addSampleIfNeeded(CATEGORY_CPU, "", "sys", t, Float.parseFloat(m.group(3)));
                            series().addSampleIfNeeded(CATEGORY_CPU, "", "idl", t, Float.parseFloat(m.group(6)));
                            series().addSampleIfNeeded(CATEGORY_CPU, "", "cpu", t, 100 - Float.parseFloat(m.group(6)));
                        }
                    }
                }
            }
            series().setPreferredScaleSame(new String[] { IO_RS, IO_WS });
            series().setPreferredScaleSame(new String[] { IO_KRS, IO_KWS });
            series().setPreferredScaleSame(new String[] { IO_WAIT, IO_ACTV });
            series().setPreferredScaleSame(new String[] { IO_WSVCT, IO_ASVCT, IO_ACTASVCT });
            series().setPreferredScaleSeries(IO_PCTW, 0, 100);
            series().setPreferredScaleSeries(IO_PCTB, 0, 100);
            series().setPreferredScaleSame(new String[] { IO_KBPR, IO_KBPW });
            series().setPreferredScaleCategory(CATEGORY_CPU, 0, 100);
       } catch(Exception e) {
            logError(e.toString());
        }
    }
  
}
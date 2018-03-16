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

public class Ps extends Parser {

    private static final int MAX_PROCESSES = 8192;
    private static final String PS_TIME = "time";
    private static final String PS_VSZ = "vsz";
    private static final String PS_RSS = "rss";
    private static final String PS_PRI = "pri";
    private Hashtable<String, Long> processTime = new Hashtable<String, Long>();
    private Pattern p = Pattern.compile(" *(\\d+) +\\d+ +(\\d*-?\\d*:?\\d+:\\d+) +(\\d+) +(\\d+) +\\d+ +[^ ]+ +(\\d+) +([^ ]+).*");
    private Pattern pef = Pattern.compile(" *[^ ]+ +(\\d+) +\\d+ +\\d+ +[^ ]+ +[^ ]+ +(\\d*-?\\d*:?\\d+:\\d+) +([^ ]+).*");
    private Pattern plinux = Pattern.compile(" *(\\d+) +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +(\\d*-?\\d*:?\\d+\\.\\d+) +([^ ]+).*");
    private Pattern poswsol = Pattern.compile(" *[^ ]+ +[^ ]+ +[^ ]+ +([0-9]+) +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ +[^ ]+ .+ +([0-9]+:[0-9]+) +(.+)");

    public boolean canHandle(String filename) {
        return super.canHandle(filename, "ps");
    }

    public Ps() {
        super("ps");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + " / " + FileFormatDescription.PRODUCT_LINUX,
                null,
                "ps",
                new String[][]{{"-e -o pid -o uid -o time -o vsz -o rss -o nlwp -o nice -o pri -o args (Solaris)",
                    "-ef (Solaris)",    
                    "OSWatcher Output (Linux)"}},
                "Process Statistics (periodically polled)",
                null));
        getCurrentTimeStamp().setOnlyTimeStampsWithDate(true);
        setDefaultInterval(60); // in case data has no timestamps
    }

    private boolean createSeries(String category, String subcategory, boolean vszrsspri) {
        if (series().getNumberOfSeries(category, subcategory) > 0) {
            return false; // series already created
        }
        series().addSeries(category, subcategory, PS_TIME);
        if (vszrsspri) {
            series().addSeries(category, subcategory, PS_VSZ);
            series().addSeries(category, subcategory, PS_RSS);
            series().addSeries(category, subcategory, PS_PRI);
        }
        return true;
    }

    @Override
    public void createAllSeries() {
        try {
            int count = 0;
            String s;
            while ((s = readLine()) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    if (createSeries(m.group(6), m.group(1),true) && ++count >= MAX_PROCESSES) {
                        return;
                    }
                    continue;
                }
                m = pef.matcher(s);
                if (m.matches()) {
                    if (createSeries(m.group(3), m.group(1), false) && ++count >= MAX_PROCESSES) {
                        return;
                    }
                    continue;                    
                }
                m = plinux.matcher(s);
                if (!m.matches()) {
                    m = poswsol.matcher(s);
                }
                if (m.matches()) {
                    if (createSeries(m.group(3), m.group(1), false) && ++count >= MAX_PROCESSES) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Override
    public void parse() {
        long lastTs = 0;
        try {
            String s;
            while ((s = readLine()) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String key = getProcessKey(m.group(6), m.group(1));
                    long curTime = Util.getSecsFromDDHHMMSS(m.group(2));
                    Long time = processTime.get(key);
                    if (time == null) {
                        time = new Long(curTime);
                    }
                    long t = getCurrentTimeStamp().getTimeStamp();
                    if (t < lastTs) {
                        break; // work-around for bug in logfiles
                    }
                    lastTs = t;
                    String category = m.group(6);
                    String subcategory = m.group(1);
                    if (series().getNumberOfSeries(category, subcategory) == 0) {
                        createSeries(category, subcategory, true);
                    }
                    series().addSampleIfNeeded(category, subcategory, PS_TIME, t, curTime - time.longValue());
                    series().addSampleIfNeeded(category, subcategory, PS_VSZ, t, Long.parseLong(m.group(3)));
                    series().addSampleIfNeeded(category, subcategory, PS_RSS, t, Long.parseLong(m.group(4)));
                    series().addSampleIfNeeded(category, subcategory, PS_PRI, t, Long.parseLong(m.group(5)));
                    processTime.put(key, time);
                    continue;
                }
                
                // Solaris ps -ef
                m = pef.matcher(s);
                if (m.matches()) {
                    String key = getProcessKey(m.group(3), m.group(1));
                    long curTime = Util.getSecsFromDDHHMMSS(m.group(2));
                    Long time = processTime.get(key);
                    if (time == null) {
                        time = new Long(curTime);
                    }
                    long t = getCurrentTimeStamp().getTimeStamp();
                    if (t < lastTs) {
                        break; // work-around for bug in logfiles
                    }
                    lastTs = t;
                    String category = m.group(3);
                    String subcategory = m.group(1);
                    if (series().getNumberOfSeries(category, subcategory) == 0) {
                        createSeries(category, subcategory, false);
                    }
                    series().addSampleIfNeeded(category, subcategory, PS_TIME, t, curTime - time.longValue());
                    processTime.put(key, time);
                    continue;
                }
                

                // for Solaris and Linux (OSWatcher Output)
                m = plinux.matcher(s);
                if (!m.matches()) {
                    m = poswsol.matcher(s);
                }
                if (m.matches()) {
                    String key = getProcessKey(m.group(6), m.group(1));
                    long curTime = Util.getSecsFromDDHHMMSS(m.group(2));
                    Long time = processTime.get(key);
                    if (time == null) {
                        time = new Long(curTime);
                    }
                    long t = getCurrentTimeStamp().getTimeStamp();
                    String category = m.group(3);
                    String subcategory = m.group(1);
                    if (series().getNumberOfSeries(category, subcategory) == 0) {
                        createSeries(category, subcategory, false);
                    }
                    series().addSampleIfNeeded(category, subcategory, PS_TIME, t, curTime - time.longValue());
                    processTime.put(key, time);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        series().setPreferredScaleSame(false, false, true);
    }

    private String getProcessKey(String pname, String pid) {
        return pname + " [" + pid + "]";
    }
    
}
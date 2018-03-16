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

public class Sar extends Parser {

    private boolean isLinux = false;
    private Pattern pSolaris = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9] +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+)");
    private Pattern pLinux = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9] .. +([^ ]+) (.+)");
    private Hashtable<String, ArrayList<String>> linuxSeries;

    // @Override
    public boolean canHandle(String filename) {
        return Parser.canHandle(filename, "sar");
    }

    public Sar() {
        super("sar");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + " / " + FileFormatDescription.PRODUCT_LINUX,
                null,
                "sar",
                new String[][]{{"-u", "-n DEV", "-x <PID>"}},
                "System Activity Report",
                null));
    }

    // @Override
    public void createAllSeries() {
        try {
            linuxSeries = new Hashtable<String, ArrayList<String>>();
            String s;
            String header = null;
            String lastCat = null;
            while ((s = readLine()) != null) {
                if (s.startsWith("SunOS ")) {
                    isLinux = false;
                    series().addSeries("cpu", "", "cpu");
                    series().addSeries("cpu", "", "usr");
                    series().addSeries("cpu", "", "sys");
                    series().addSeries("cpu", "", "wio");
                    series().addSeries("cpu", "", "idl");
                    series().setPreferredScaleAll(0, 100);
                    return;
                }
                Matcher m = pLinux.matcher(s);
                if (m.matches()) {
                    String category = m.group(1);
                    String series = m.group(2);
                    if (header == null) {
                        // this is the header with the category names
                        header = category;
                        StringTokenizer tok = new StringTokenizer(series, " ");
                        ArrayList<String> snames = linuxSeries.get(category);
                        if (snames == null) {
                            snames = new ArrayList<String>();
                            linuxSeries.put(category, snames);
                        }
                        while (tok.hasMoreTokens()) {
                            String sname = tok.nextToken();
                            if (sname != null && sname.length() > 0) {
                                snames.add(sname);
                            }
                        }
                    } else {
                        if (category.equals(header)) {
                            break; // we're around once and got all the headers
                        }
                        if (category.equals(lastCat)) {
                            break;
                        }
                        ArrayList<String> snames = linuxSeries.get(header);
                        linuxSeries.put(category, snames);
                        for (String sname : snames) {
                            series().addSeries(category, "", sname);
                        }
                        lastCat = category;
                    }
                }
            }
            isLinux = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // @Override
    public void parse() {
        String s;
        while ((s = readLine()) != null) {
            if (!isLinux) {
                Matcher m = pSolaris.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    series().addSampleIfNeeded("cpu", "", "cpu", t, 100 - Integer.parseInt(m.group(4))); // total
                    series().addSampleIfNeeded("cpu", "", "usr", t, Integer.parseInt(m.group(1))); // usr
                    series().addSampleIfNeeded("cpu", "", "sys", t, Integer.parseInt(m.group(2))); // sys
                    series().addSampleIfNeeded("cpu", "", "wio", t, Integer.parseInt(m.group(3))); // wio
                    series().addSampleIfNeeded("cpu", "", "idl", t, Integer.parseInt(m.group(4))); // idl
                }
            } else {
                Matcher m = pLinux.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    String category = m.group(1);
                    String seriesData = m.group(2);
                    StringTokenizer tok = new StringTokenizer(seriesData, " ");
                    int sId = 0;
                    ArrayList<String> snames = linuxSeries.get(category);
                    if (snames == null) {
                        continue;
                    }
                    try {
                        while (tok.hasMoreTokens()) {
                            String value = tok.nextToken();
                            String series = (sId < snames.size() ? snames.get(sId++) : null);
                            if (value != null && series != null && value.length() > 0) {
                                series().addSampleIfNeeded(category, "", series, t, Float.parseFloat(value));
                            }
                        }
                    } catch (Exception eignore) {
                        // fails for headers
                    }
                }
            }
        }
        if (isLinux) {
            series().setPreferredScaleSame(false, false, false);
        }
    }
}

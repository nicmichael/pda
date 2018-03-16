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

public class CollectD extends Parser {

    // handles a format as:
    //
    // time,counter1,counter2,...
    // 2010-10-15 09:06:40,93.6,0,...

    private ArrayList<String> seriesNames;

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"collectd");
    }

    public CollectD() {
        super("collectd");
        setSupportedFileFormat(new FileFormatDescription(
                "collectd",
                null,
                null,
                null,
                "colletd CSV output ",
                "header like 'time,counter1,counter2,...' and lines like '2010-10-15 09:06:40,93.6,0,...'"));
    }

    // @Override
    public void createAllSeries() {
        seriesNames = new ArrayList<String>();
        String s;
        while ((s = readLine()) != null
                && !s.startsWith("time,"));
        if (s != null && s.startsWith("time,")) {
            StringTokenizer tok = new StringTokenizer(s, ",");
            int i = 0;
            while (tok.hasMoreTokens()) {
                String name = tok.nextToken();
                if (i++ >= 1) {
                    DataSeries ds = series().addSeries("", "", name);
                    seriesNames.add(ds.getLocalName());
                }
            }
        }
    }

    // @Override
    public void parse() {
        try {
            String s;
            while ((s = readLine()) != null) {
                long t = getCurrentTimeStamp().getTimeStamp();
                StringTokenizer tok = new StringTokenizer(s, ",");
                int i = 0;
                while (tok.hasMoreTokens()) {
                    String vv = tok.nextToken();
                    if (i++ >= 1) {
                        try {
                            DataSeries series = series().getSeries(seriesNames.get(i-1));
                            series().addSampleIfNeeded(series.getCategoryName(), series.getSubcategoryName(), 
                                    series.getSeriesName(), t, Double.parseDouble(vv));
                        } catch (Exception ee) {
                            // nothing to do (ignore this sample!)
                        }
                    }
                }
            }
            series().setPreferredScaleSame(false, false, true);
        } catch(Exception e) {
            logError(e.toString());
        }
    }

}

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

public class CollectL extends Parser {
    
    // handles a format as:
    // #Date Time [CPU]User% [CPU]Nice% [CPU]Sys% [CPU]Wait% 
    // 20100310 06:59:42 4 0 1 0
    
    private ArrayList<String> seriesNames;
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"collectl");
    }
    
    public CollectL() {
        super("collectl");
        setSupportedFileFormat(new FileFormatDescription(
                "collectl",
                null,
                null,
                new String[][]  { { "-o D -P -p <infile>" } },
                "colletl CSV output",
                "header like 'Date Time [CPU]User% [CPU]Nice% [CPU]Sys% [CPU]Wait% ' and lines like '20100310 06:59:42 4 0 1 0'"));
        getCurrentTimeStamp().deleteAllTimeStampPatterns();
        getCurrentTimeStamp().addTimeStampPattern("YYYYMMDD hh:mm:ss",
                            Pattern.compile("(\\d\\d\\d\\d)(\\d\\d)(\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d).*"),
                            new TimeStamp.Fields[] { TimeStamp.Fields.year, TimeStamp.Fields.month, TimeStamp.Fields.day, 
                                TimeStamp.Fields.hour, TimeStamp.Fields.minute, TimeStamp.Fields.second } );
    }
    
    // @Override
    public void createAllSeries() {
        String s;
        while ((s = readLine()) != null
                && !s.startsWith("#Date Time "));

        Pattern p = Pattern.compile("\\[([^\\]]+)\\](.+)");
        seriesNames = new ArrayList<String>();
        if (s != null && s.startsWith("#Date Time ")) {
            s = s.substring(1);
            StringTokenizer tok = new StringTokenizer(s, " ");
            while (tok.hasMoreTokens()) {
                String cn = tok.nextToken();
                Matcher m = p.matcher(cn);
                if (m.matches()) {
                    DataSeries ds = series().addSeries(m.group(1), "", m.group(2));
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
                StringTokenizer tok = new StringTokenizer(s, " ");
                int i = 0;
                if (tok.countTokens() != seriesNames.size() + 2) {
                    continue;
                }
                while (tok.hasMoreTokens()) {
                    String vv = tok.nextToken();
                    if (i++ >= 2) {
                        try {
                            DataSeries series = series().getSeries(seriesNames.get(i-2));
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

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

public class Nicstat extends Parser {
    
    private Pattern p = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9] +([^ ]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+) +([0-9\\.]+)");
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"nicstat");
    }

    public Nicstat() {
        super("nicstat");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS,
                null,
                "nictsat",
                null,
                "NIC Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
   
    private void createSeries(String category) {
        series().addSeries(category, "", "rKB/s");
        series().addSeries(category, "", "wKB/s");
        series().addSeries(category, "", "rPk/s");
        series().addSeries(category, "", "wPk/s");
        series().addSeries(category, "", "rAvs");
        series().addSeries(category, "", "wAvs");
        series().addSeries(category, "", "%Util");
        series().addSeries(category, "", "Sat");
        series().addSeries(category, "", "KB/s");
        series().addSeries(category, "", "Pk/s");
    }

    // @Override
    public void createAllSeries() {
        try {
            int i=0;
            String s;
            while ((s = readLine()) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    createSeries(m.group(1));
                }
                if (i++ >= 1000) {
                    break;
                }
            }
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
    // @Override
    public void parse() {
        try {
            String s;
            
            int cnt = 0;
            while( (s = readLine()) != null) {
                if (s.startsWith("    Time")) {
                    if (++cnt == 2) {
                        break;
                    }
                }
            }

            while ((s = readLine()) != null) {
                long t = getCurrentTimeStamp().getTimeStamp();
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String device = m.group(1);
                    float rKBs = Float.parseFloat(m.group(2));
                    float wKBs = Float.parseFloat(m.group(3));
                    float rPks = Float.parseFloat(m.group(4));
                    float wPks = Float.parseFloat(m.group(5));
                    float rAvs = Float.parseFloat(m.group(6));
                    float wAvs = Float.parseFloat(m.group(7));
                    float Util = Float.parseFloat(m.group(8));
                    float Sat = Float.parseFloat(m.group(9));
                    float KBs = rKBs + wKBs;;
                    float Pks = rPks + wPks;
                    series().addSampleIfNeeded(device, "", "rKB/s", t, rKBs);
                    series().addSampleIfNeeded(device, "", "wKB/s", t, wKBs);
                    series().addSampleIfNeeded(device, "", "rPk/s", t, rPks);
                    series().addSampleIfNeeded(device, "", "wPk/s", t, wPks);
                    series().addSampleIfNeeded(device, "", "rAvs",  t, rAvs);
                    series().addSampleIfNeeded(device, "", "wAvs",  t, wAvs);
                    series().addSampleIfNeeded(device, "", "%Util", t, Util);
                    series().addSampleIfNeeded(device, "", "Sat",   t, Sat);
                    series().addSampleIfNeeded(device, "", "KB/s",  t, KBs);
                    series().addSampleIfNeeded(device, "", "Pk/s",  t, Pks);
                }
            }
            
            series().setPreferredScaleSame(new String[] { "rKB/s", "wKB/s", "KB/s" });
            series().setPreferredScaleSame(new String[] { "rPk/s", "wPk/s" });
            series().setPreferredScaleSame(new String[] { "rAvs", "wAvs", "Pk/s" });
            series().setPreferredScaleSeries("%Util", 0, 100);
            series().setPreferredScaleSame(new String[] { "Sat" });
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
}

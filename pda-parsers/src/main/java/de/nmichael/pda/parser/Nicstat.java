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
    
    private String[] seriesNames;
    private Pattern pHeader = Pattern.compile("( +Time.*|[0-9][0-9]:[0-9][0-9]:[0-9][0-9] +[^0-9]+)");
    private Pattern pData = Pattern.compile("[0-9][0-9]:[0-9][0-9]:[0-9][0-9] +([a-zA-Z][^ ]+) +[0-9]+.*");
    private Pattern pDataX = Pattern.compile("([a-zA-Z][^ ]+) +[0-9]+.*");
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"nicstat");
    }

    public Nicstat() {
        super("nicstat");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + "/" + FileFormatDescription.PRODUCT_LINUX,
                null,
                "nictsat",
                null,
                "NIC Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
   
    private void createSeries(String category, int offset) {
        for (int i=offset; i<seriesNames.length; i++) {
            DataSeries ser = series().addSeries(category, "", seriesNames[i]);
        }
    }
    
    private boolean isHeader(String s) {
        return pHeader.matcher(s).matches();
    }

    // @Override
    public void createAllSeries() {
        try {
            int i=0;
            String s;
            while ((s = readLine()) != null) {
                if (seriesNames == null && isHeader(s)) {
                    seriesNames = s.trim().split(" +");
                }
                Matcher m = pData.matcher(s);
                if (m.matches() && seriesNames != null) {
                    createSeries(m.group(1), 2);
                }
                m = pDataX.matcher(s);
                if (m.matches() && seriesNames != null) {
                    createSeries(m.group(1), 1);
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
        if (seriesNames == null) {
            return;
        }
        try {
            String s;
            int cnt = 0;
            while( (s = readLine()) != null) {
                if (isHeader(s)) {
                    if (++cnt == 2) {
                        break;
                    }
                }
            }

            while ((s = readLine()) != null) {
                long t = getCurrentTimeStamp().getTimeStamp();
                int offset = 2;
                Matcher m = pData.matcher(s);
                if (!m.matches()) {
                    m = pDataX.matcher(s);
                    offset = 1;
                }
                if (m.matches()) {
                    String device = m.group(1);
                    String[] values = s.split(" +");
                    for (int i = offset; i < values.length && i - offset < seriesNames.length; i++) {
                        try {
                            series().addSampleIfNeeded(device, "", seriesNames[i], t,
                                    Float.parseFloat(values[i]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            series().setPreferredScaleSame(new String[] { "rKB/s", "wKB/s", "KB/s" });
            series().setPreferredScaleSame(new String[] { "rPk/s", "wPk/s" });
            series().setPreferredScaleSame(new String[] { "rAvs", "wAvs", "Pk/s" });
            series().setPreferredScaleSeries("%Util", 0, 100);
            series().setPreferredScaleSame(new String[] { "Sat" });
            series().setPreferredScaleSame(new String[] { "RdKB", "WrKB" });
            series().setPreferredScaleSame(new String[] { "RdPkt", "WrPkt" });
            series().setPreferredScaleSame(new String[] { "RdMbps", "WrMbps" });
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
}

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

public class CellMetrics extends Parser {
    
    // handles a format as:
    // CD_IO_TM_W_SM_RQ      CD_00_cellname     203 us/request    2012-05-13T08:33:29-07:00
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"cell");
    }
    
    public CellMetrics() {
        super("cell");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_ORACLE,
                null,
                "Storage Cell Metrics",
                new String[][]  { { "list metrichistory", "list metriccurrent" } },
                "CellCli Storage Cell Metrics (periodically polled)",
                null));
    }
    
    // @Override
    public void createAllSeries() {
        parse();
    }
    
    // @Override
    public void parse() {
        Pattern p = Pattern.compile("[ \\t]+([^ \\t]+)[ \\t]+([^ \\t]+)[ \\t]+(.*[^ \\t])[ \\t][ \\t]+([^ \\t]+)");
        Pattern pValueUnit = Pattern.compile("([0-9\\.]+) (.+)");
        
        try {
            String s;
            while( (s = readLine()) != null) {
                long t = getCurrentTimeStamp().getTimeStamp();

                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String name = m.group(1);
                    String object = m.group(2);
                    String value = m.group(3);
                    String vValue = value;
                    m = pValueUnit.matcher(value);
                    if (m.matches()) {
                        vValue = m.group(1);
                    }
                    try {
                        Float f = Float.parseFloat(vValue);
                        if (series().getSeries(object, "", name) == null) {
                            series().addSeries(object, "", name);
                        }
                        series().addSample(object, "", name, t, f);
                    } catch (Exception e) {
                    }
                }
            }
            series().setPreferredScaleSame(false, false, true);
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
}

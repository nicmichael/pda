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

public class Netstat extends Parser {
    
    private static Pattern pSolaris = Pattern.compile("([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) *");
    private static Pattern pLinux= Pattern.compile("[0-9]* ? ([^ ]+) +[0-9]+ +[0-9]+ +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +[^ ]+");
    private boolean isLinux = true;
    private String intf;
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"netstat");
    }
    
    public Netstat() {
        super("netstat");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + " / " + FileFormatDescription.PRODUCT_LINUX,
                null,
                "netstat",
                new String[][] { { "-i interval" },
                                 { "optional: -I interface" } },
                "Network Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
        super.getCurrentTimeStamp().addTimeStampPatternFirst("Getafix timestamp", 
        		Pattern.compile("([0-9]+) [^0-9].*"),
        		new TimeStamp.Fields[] { TimeStamp.Fields.unixsec });
    }
    
    private void createSeries(String category) {
    	if (isLinux) {
			series().addSeries(category, "rx", "ok").setCumulative(true, true, true);
			series().addSeries(category, "rx", "err").setCumulative(true, true, true);
			series().addSeries(category, "rx", "drp").setCumulative(true, true, true);
			series().addSeries(category, "rx", "ovr").setCumulative(true, true, true);
			series().addSeries(category, "tx", "ok").setCumulative(true, true, true);
			series().addSeries(category, "tx", "err").setCumulative(true, true, true);
			series().addSeries(category, "tx", "drp").setCumulative(true, true, true);
			series().addSeries(category, "tx", "ovr").setCumulative(true, true, true);
			series().addSeries(category, "total", "ok").setCumulative(true, true, true);
			series().addSeries(category, "total", "err").setCumulative(true, true, true);
			series().addSeries(category, "total", "drp").setCumulative(true, true, true);
			series().addSeries(category, "total", "ovr").setCumulative(true, true, true);
		} else {
			series().addSeries(category, "in", "pkg");
			series().addSeries(category, "in", "err");
			series().addSeries(category, "out", "pkg");
			series().addSeries(category, "out", "err");
			series().addSeries(category, "in+out", "pkg");
			series().addSeries(category, "in+out", "err");
			series().addSeries(category, "coll", "coll");
		}
    }
    
    // @Override
    public void createAllSeries() {
    	if (getMatchingStringFromFile(pSolaris) != null) {
    		isLinux = false;
    	}
    	
		if (isLinux) {
			String s;
            while( (s = readLine()) != null) {
                Matcher m = pLinux.matcher(s);
                if (m.matches()) {
                	if (series().getNumberOfSubcategories(m.group(1)) > 0) {
                		break;
                	}
                	createSeries(m.group(1));
                }
            }
		} else {
			intf = getMatchingStringFromFile(" +input +([^ ]+) +output.*");
			createSeries("total");
			if (intf != null) {
				createSeries(intf);
			}
		}
    }

    // @Override
    public void parse() {
        setNewSamplesHeaderPattern(pSolaris.toString());
        boolean first = true;
        try {
            String s;
            long lastts = 0;
            while( (s = readLine()) != null) {
            	if (isLinux) {
                    Matcher m = pLinux.matcher(s);
                    if (m.matches()) {
                        long t = getCurrentTimeStamp().getTimeStamp();
                        double[] values = getValuesFromMatcher(m);
						String category = m.group(1);
						series().addSampleIfNeeded(category, "rx", "ok", t, values[1]);
						series().addSampleIfNeeded(category, "rx", "err", t, values[2]);
						series().addSampleIfNeeded(category, "rx", "drp", t, values[3]);
						series().addSampleIfNeeded(category, "rx", "ovr", t, values[4]);
						series().addSampleIfNeeded(category, "tx", "ok", t, values[5]);
						series().addSampleIfNeeded(category, "tx", "err", t, values[6]);
						series().addSampleIfNeeded(category, "tx", "drp", t, values[7]);
						series().addSampleIfNeeded(category, "tx", "ovr", t, values[8]);
						series().addSampleIfNeeded(category, "total", "ok", t, values[1] + values[5]);
						series().addSampleIfNeeded(category, "total", "err", t, values[2] + values[6]);
						series().addSampleIfNeeded(category, "total", "drp", t, values[3] + values[7]);
						series().addSampleIfNeeded(category, "total", "ovr", t, values[4] + values[8]);
                    }
            	} else {
                    Matcher m = pSolaris.matcher(s);
                    if (m.matches()) {
                        long t = getCurrentTimeStamp().getTimeStamp();
                        long elapsed = (t - lastts) / 1000;
                        if (elapsed < 1) {
                            elapsed = 1;
                        }
                        lastts = t;
                        if (first) {
                            first = false;
                            continue;
                        }
                        double[] values = getValuesFromMatcher(m);
                        for (int c=0; c<2; c++) {
                            String category = (c == 0 ? "total" : intf);
                            if (category == null) {
                                break;
                            }
                            series().addSampleIfNeeded(category, "in",     "pkg",  t, values[0 + (c*5)]);
                            series().addSampleIfNeeded(category, "in",     "err",  t, values[1 + (c*5)]);
                            series().addSampleIfNeeded(category, "out",    "pkg",  t, values[2 + (c*5)]);
                            series().addSampleIfNeeded(category, "out",    "err",  t, values[3 + (c*5)]);
                            series().addSampleIfNeeded(category, "in+out", "pkg",  t, values[0 + (c*5)] + values[2 + (c*5)]);
                            series().addSampleIfNeeded(category, "in+out", "err",  t, values[1 + (c*5)] + values[3 + (c*5)]);
                            series().addSampleIfNeeded(category, "coll",   "coll", t, values[4 + (c*5)]);
                        }
                    }
            	}
            }
            series().setPreferredScaleSame(false, false, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}

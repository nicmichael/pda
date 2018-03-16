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

public class Vmstat extends Parser {
    
    private Pattern vmstat  = Pattern.compile(" *([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +[0-9]+ +[0-9]+ +[0-9]+ +[0-9]+ +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+)");
    private Pattern vmstatp = Pattern.compile(" *([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+)");
    private Pattern vmstatL  = Pattern.compile(" *([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+) +([0-9]+).*");
    private boolean modePaging = false;
    private boolean modeSolaris = false;

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"vmstat");
    }
    
    public Vmstat() {
        super("vmstat");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + " / " + FileFormatDescription.PRODUCT_LINUX,
                null,
                "vmstat",
                new String[][] { { "none", "-p" } },
                "Memory Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
    
    // @Override
    public void createAllSeries() {
        try {
            String s;
            while( (s = readLine()) != null) {
                if (s.trim().startsWith("r b w")) {
                    modeSolaris = true;
                    modePaging = false;
                    break;
                }
                if (s.trim().startsWith("swap")) {
                    modeSolaris = true;
                    modePaging = true;
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
		if (modeSolaris) {
			if (modePaging) {
				series().addSeries("mem", "", "swap");
				series().addSeries("mem", "", "free");
				series().addSeries("page", "", "re");
				series().addSeries("page", "", "mf");
				series().addSeries("page", "", "fr");
				series().addSeries("page", "", "de");
				series().addSeries("page", "", "sr");
				series().addSeries("exec", "", "epi");
				series().addSeries("exec", "", "epo");
				series().addSeries("exec", "", "epf");
				series().addSeries("anon", "", "api");
				series().addSeries("anon", "", "apo");
				series().addSeries("anon", "", "apf");
				series().addSeries("file", "", "fpi");
				series().addSeries("file", "", "fpo");
				series().addSeries("file", "", "fpf");
				series().addSeries("page", "", "pi");
				series().addSeries("page", "", "po");
				series().addSeries("page", "", "pf");
			} else {
				series().addSeries("kthr", "", "r");
				series().addSeries("kthr", "", "b");
				series().addSeries("kthr", "", "w");
				series().addSeries("mem", "", "swap");
				series().addSeries("mem", "", "free");
				series().addSeries("page", "", "re");
				series().addSeries("page", "", "mf");
				series().addSeries("page", "", "pi");
				series().addSeries("page", "", "po");
				series().addSeries("page", "", "fr");
				series().addSeries("page", "", "de");
				series().addSeries("page", "", "sr");
				series().addSeries("faults", "", "in");
				series().addSeries("faults", "", "sy");
				series().addSeries("faults", "", "cs");
				series().addSeries("cpu", "", "usr");
				series().addSeries("cpu", "", "sys");
				series().addSeries("cpu", "", "idl");
				series().addSeries("cpu", "", "cpu");
			}
		} else {
        	// Linux
			series().addSeries("kthr", "", "r");
			series().addSeries("kthr", "", "b");
			series().addSeries("mem", "", "swpd");
			series().addSeries("mem", "", "free");
			series().addSeries("mem", "", "buff");
			series().addSeries("mem", "", "cache");
			series().addSeries("page", "", "si");
			series().addSeries("page", "", "so");
			series().addSeries("page", "", "bi");
			series().addSeries("page", "", "bo");
			series().addSeries("faults", "", "in");
			series().addSeries("faults", "", "cs");
			series().addSeries("cpu", "", "usr");
			series().addSeries("cpu", "", "sys");
			series().addSeries("cpu", "", "idl");
			series().addSeries("cpu", "", "wait");
			series().addSeries("cpu", "", "stolen");
			series().addSeries("cpu", "", "cpu");
        }
        series().setPreferredScaleSame(true, false, false);
        series().setPreferredScaleCategory("cpu", 0, 100);
    }
   
    // @Override
    public void parse() {
        boolean firstHeader = true;
        setNewSamplesHeaderPattern(modeSolaris ? (modePaging ? vmstatp.toString() : vmstat.toString()) : vmstatL.toString());
        try {
            String s;
            while( (s = readLine()) != null) {
                Matcher m = (modeSolaris ? (modePaging ? vmstatp.matcher(s) : vmstat.matcher(s)) : vmstatL.matcher(s));
                if (m.matches()) {
                    if (firstHeader) {
                        firstHeader = false;
                        continue;
                    }
                    long ts = getCurrentTimeStamp().getTimeStamp();
                    if (modeSolaris) {
						if (modePaging) {
							series().addSampleIfNeeded("mem", "", "swap", ts, Double.parseDouble(m.group(1)));
							series().addSampleIfNeeded("mem", "", "free", ts, Double.parseDouble(m.group(2)));
							series().addSampleIfNeeded("page", "", "re", ts, Double.parseDouble(m.group(3)));
							series().addSampleIfNeeded("page", "", "mf", ts, Double.parseDouble(m.group(4)));
							series().addSampleIfNeeded("page", "", "fr", ts, Double.parseDouble(m.group(5)));
							series().addSampleIfNeeded("page", "", "de", ts, Double.parseDouble(m.group(6)));
							series().addSampleIfNeeded("page", "", "sr", ts, Double.parseDouble(m.group(7)));
							series().addSampleIfNeeded("exec", "", "epi", ts, Double.parseDouble(m.group(8)));
							series().addSampleIfNeeded("exec", "", "epo", ts, Double.parseDouble(m.group(9)));
							series().addSampleIfNeeded("exec", "", "epf", ts, Double.parseDouble(m.group(10)));
							series().addSampleIfNeeded("anon", "", "api", ts, Double.parseDouble(m.group(11)));
							series().addSampleIfNeeded("anon", "", "apo", ts, Double.parseDouble(m.group(12)));
							series().addSampleIfNeeded("anon", "", "apf", ts, Double.parseDouble(m.group(13)));
							series().addSampleIfNeeded("file", "", "fpi", ts, Double.parseDouble(m.group(14)));
							series().addSampleIfNeeded("file", "", "fpo", ts, Double.parseDouble(m.group(15)));
							series().addSampleIfNeeded("file", "", "fpf", ts, Double.parseDouble(m.group(16)));
							series().addSampleIfNeeded("page", "", "pi", ts,
									Double.parseDouble(m.group(8) + m.group(11) + m.group(14)));
							series().addSampleIfNeeded("page", "", "po", ts,
									Double.parseDouble(m.group(9) + m.group(12) + m.group(15)));
							series().addSampleIfNeeded("page", "", "pf", ts,
									Double.parseDouble(m.group(10) + m.group(13) + m.group(16)));
						} else {
							series().addSampleIfNeeded("kthr", "", "r", ts, Double.parseDouble(m.group(1)));
							series().addSampleIfNeeded("kthr", "", "b", ts, Double.parseDouble(m.group(2)));
							series().addSampleIfNeeded("kthr", "", "w", ts, Double.parseDouble(m.group(3)));
							series().addSampleIfNeeded("mem", "", "swap", ts, Double.parseDouble(m.group(4)));
							series().addSampleIfNeeded("mem", "", "free", ts, Double.parseDouble(m.group(5)));
							series().addSampleIfNeeded("page", "", "re", ts, Double.parseDouble(m.group(6)));
							series().addSampleIfNeeded("page", "", "mf", ts, Double.parseDouble(m.group(7)));
							series().addSampleIfNeeded("page", "", "pi", ts, Double.parseDouble(m.group(8)));
							series().addSampleIfNeeded("page", "", "po", ts, Double.parseDouble(m.group(9)));
							series().addSampleIfNeeded("page", "", "fr", ts, Double.parseDouble(m.group(10)));
							series().addSampleIfNeeded("page", "", "de", ts, Double.parseDouble(m.group(11)));
							series().addSampleIfNeeded("page", "", "sr", ts, Double.parseDouble(m.group(12)));
							series().addSampleIfNeeded("faults", "", "in", ts, Double.parseDouble(m.group(13)));
							series().addSampleIfNeeded("faults", "", "sy", ts, Double.parseDouble(m.group(14)));
							series().addSampleIfNeeded("faults", "", "cs", ts, Double.parseDouble(m.group(15)));
							series().addSampleIfNeeded("cpu", "", "usr", ts, Double.parseDouble(m.group(16)));
							series().addSampleIfNeeded("cpu", "", "sys", ts, Double.parseDouble(m.group(17)));
							series().addSampleIfNeeded("cpu", "", "idl", ts, Double.parseDouble(m.group(18)));
							series().addSampleIfNeeded("cpu", "", "cpu", ts, 100 - Double.parseDouble(m.group(18)));
						}
					} else {
                    	// Linux
						series().addSampleIfNeeded("kthr", "", "r", ts, Double.parseDouble(m.group(1)));
						series().addSampleIfNeeded("kthr", "", "b", ts, Double.parseDouble(m.group(2)));
						series().addSampleIfNeeded("mem", "", "swpd", ts, Double.parseDouble(m.group(3)));
						series().addSampleIfNeeded("mem", "", "free", ts, Double.parseDouble(m.group(4)));
						series().addSampleIfNeeded("mem", "", "buff", ts, Double.parseDouble(m.group(5)));
						series().addSampleIfNeeded("mem", "", "cache", ts, Double.parseDouble(m.group(6)));
						series().addSampleIfNeeded("page", "", "si", ts, Double.parseDouble(m.group(7)));
						series().addSampleIfNeeded("page", "", "so", ts, Double.parseDouble(m.group(8)));
						series().addSampleIfNeeded("page", "", "bi", ts, Double.parseDouble(m.group(9)));
						series().addSampleIfNeeded("page", "", "bo", ts, Double.parseDouble(m.group(10)));
						series().addSampleIfNeeded("faults", "", "in", ts, Double.parseDouble(m.group(11)));
						series().addSampleIfNeeded("faults", "", "cs", ts, Double.parseDouble(m.group(12)));
						series().addSampleIfNeeded("cpu", "", "usr", ts, Double.parseDouble(m.group(13)));
						series().addSampleIfNeeded("cpu", "", "sys", ts, Double.parseDouble(m.group(14)));
						series().addSampleIfNeeded("cpu", "", "idl", ts, Double.parseDouble(m.group(15)));
						series().addSampleIfNeeded("cpu", "", "wait", ts, Double.parseDouble(m.group(16)));
						series().addSampleIfNeeded("cpu", "", "stolen", ts, Double.parseDouble(m.group(17)));
						series().addSampleIfNeeded("cpu", "", "cpu", ts, Double.parseDouble(m.group(13)) + Double.parseDouble(m.group(14)));
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
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

public class Pgstat extends Parser {
    
    private static final String PG_INT = "int";
    private static final String PG_FP  = "fp";
    private static final String PG_HW  = "hw";
    private static final String PG_SW  = "sw";
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"pgstat");
    }
    
    public Pgstat() {
        super("pgstat");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + " / " + FileFormatDescription.PRODUCT_LINUX,
                null,
                "pgstat",
                new String[][] { { "none (Solaris)" } },
                "Processor Group Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
    
    private void createSeries(String category) {
        series().addSeries(category, PG_INT, PG_HW);
        series().addSeries(category, PG_INT, PG_SW);
        series().addSeries(category, PG_FP,  PG_HW);
        series().addSeries(category, PG_FP,  PG_SW);
    }
    
    // @Override
    public void createAllSeries() {
        try {
            String s;
            Pattern pCat = Pattern.compile("^.+ ([0-9\\-]+)");
            int cnt = 0;
            while( (s = readLine()) != null) {
                if (s.startsWith("PG ") && ++cnt >= 2) {
                    break;
                }
                s = s.trim();
                Matcher m = pCat.matcher(s);
                if (m.matches()) {
                    String resource = m.group(1);
                    if (s.indexOf("Floating_Point_Unit") < 0 &&
                        s.indexOf("Integer_Pipeline") < 0) {
                        continue;
                    }
                    createSeries(m.group(1));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        series().setPreferredScaleAll(0, 100);
    }
    
    // @Override
    public void parse() {
        setNewSamplesHeader("PG ");
        Pattern pInt   = Pattern.compile(".*Integer_Pipeline +([0-9\\.\\-\\?]+)% +([0-9\\.\\-\\?]+)% +([0-9\\-]+)");
        Pattern pFloat = Pattern.compile(".*Floating_Point_Unit +([0-9\\.\\-\\?]+)% +([0-9\\.\\-\\?]+)% +([0-9\\-]+)");
        try {
            String s;
            while( (s = readLine()) != null) {
                s = s.trim();
                String subcategory;
                Matcher m;
                m = pInt.matcher(s);
                if (m.matches()) {
                    subcategory = PG_INT;
                } else {
                    subcategory = PG_FP;
                    m = pFloat.matcher(s);
                }
                if (m.matches()) {
                    String category = m.group(3);
                    long t = getCurrentTimeStamp().getTimeStamp();
                    try {
                        series().addSampleIfNeeded(category, subcategory, PG_HW, t, Float.parseFloat(m.group(1)));
                    } catch(Exception e) {}
                    try {
                        series().addSampleIfNeeded(category, subcategory, PG_SW, t, Float.parseFloat(m.group(2)));
                    } catch(Exception e) {}
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
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

public class RegExp extends Parser {
    
    private static final String PARAM_PATTERN = "Pattern";
    private String pattern = "";
    Pattern p;
    
    // @Override
    public boolean canHandle(String filename) {
        return false;
    }
    
    public RegExp() {
        super("regex");
        setParameter(PARAM_PATTERN, "");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_GENERIC,
                null,
                "Generic Regular Expressions",
                null,
                "Regular Expressions: number values in each group will be added as series data",
                "Pattern like: .*[^0-9]([0-9]+)[^0-9][^0-9]--"));
    }
    
    // @Override
    public void createAllSeries() {
        parse();
    }
    
    public void parse() {
        String pattern = this.pattern;
        if (pattern == null || pattern.length() == 0) {
            return;
        }
        try {
            if (!pattern.startsWith("^")) {
                pattern = ".*" + pattern;
            }
            if (!pattern.endsWith("$")) {
                pattern = pattern + ".*";
            }
            p = Pattern.compile(pattern);
            for (int i=1; i<=p.matcher("foobar").groupCount(); i++) {
                series().addSeries("", "",  "group" + i);
            }
            
            String s;
            while ((s = readLine()) != null) {
                s = s.trim();
                if (s.length() > 0) {
                    Matcher m = p.matcher(s);
                    if (m.matches()) {
                        int cnt = m.groupCount();
                        long t = getCurrentTimeStamp().getTimeStamp();
                        for (int i=1; i<=cnt; i++) {
                            try {
                                String str = m.group(i);
                                double v = Double.parseDouble(str);
                                series().addSample("", "", "group" + i, t, v);
                            } catch(Exception eignore) {
                            }
                        }
                    }
                }
            }
            series().setPreferredScaleIndividual();
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
    public void setParameter(String name, String value) {
        super.setParameter(name, value);
        if (PARAM_PATTERN.equals(name)) {
            try {
                this.pattern = value;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

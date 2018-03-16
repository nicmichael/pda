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
import de.nmichael.pda.util.Util;
import java.util.regex.*;
import java.util.*;

public class NameValue extends Parser {
    
    // handles a format as:
    // timestamp;category:subcategory:series=value
    
    private Pattern p = Pattern.compile("([^;]+);([^=]+)=([0-9\\.]+)");
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"nv");
    }
    
    public NameValue() {
        super("nv");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_GENERIC,
                null,
                "Name-Value Pairs",
                null,
                "generic Name-Value format",
                "timestamp;category:subcategory:series=value"));
    }
    
    // @Override
    public void createAllSeries() {
        parse(false);
    }

    // @Override
    public void parse() {
        parse(true);
    }
    
    public void parse(boolean addSamples) {
        try {
            String s;
            while ((s = readLine()) != null) {
                s = s.trim();
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    Vector<String> name = Util.split(m.group(2), ":");
                    String category = name != null && name.size() > 0 ? name.get(0) : "unknown";
                    String subcategory = name != null && name.size() > 1 ? name.get(1) : "";
                    String series = name != null && name.size() > 2 ? name.get(2) : "";
                    Double value = Double.parseDouble(m.group(3));
                    if (addSamples) {
                        series().addSampleIfNeeded(category, subcategory, series, getCurrentTimeStamp().getTimeStamp(), value);
                    } else {
                        series().getOrAddSeries(category, subcategory, series);
                    }
                    //series().getOrAddSeries(category, subcategory, series).addSample(new Sample(getCurrentTimeStamp().getTimeStamp(), value));
                }
            }
            series().setPreferredScaleIndividual();
        } catch(Exception e) {
            logError(e.toString());
        }
    }
        
}

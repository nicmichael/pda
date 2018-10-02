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

public class Df extends Parser {
    
    private Pattern pData = Pattern.compile("([^ ]+) +([0-9\\.KMGT]+) +([0-9\\\\.KMGT]+) +([0-9\\\\.KMGT]+) +([0-9]+)% +([^ ]+)");
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"df");
    }

    public Df() {
        super("df");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_LINUX,
                null,
                "df",
                null,
                "File System Statistics",
                null));
        setDefaultInterval(10); // in case data has no timestamps
    }
   
    // @Override
    public void createAllSeries() {
    	parse();
    }
    
    // @Override
    public void parse() {
        try {
            String s;
            int cnt = 0;
            while ((s = readLine()) != null) {
                Matcher m = pData.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    String fs = m.group(6);
					try {
						series().getOrAddSeries(fs, "", "size").addSampleIfNeeded(t, Util.stringKMG2double(m.group(2)));
						series().getOrAddSeries(fs, "", "used").addSampleIfNeeded(t, Util.stringKMG2double(m.group(3)));
						series().getOrAddSeries(fs, "", "avail").addSampleIfNeeded(t, Util.stringKMG2double(m.group(4)));
						series().getOrAddSeries(fs, "", "use%").addSampleIfNeeded(t, Double.parseDouble(m.group(5)));
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
            }
            series().setPreferredScaleSeries("use%", 0, 100);
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
}

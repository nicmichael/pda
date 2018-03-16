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

public class Swap extends Parser {
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"swap");
    }
    
    public Swap() {
        super("swap");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS,
                null,
                "swap",
                new String[][] { { "-l" } },
                "Swap Statistics",
                null));
        setDefaultInterval(60); // in case data has no timestamps
    }
    
    // @Override
    public void createAllSeries() {
        series().addSeries("swap", "total", "blocks");
        series().addSeries("swap", "total", "mb");
        series().addSeries("swap", "free", "blocks");
        series().addSeries("swap", "free", "mb");
        series().addSeries("swap", "used", "blocks");
        series().addSeries("swap", "used", "mb");
    }

    // @Override
    public void parse() {
        Pattern p = Pattern.compile("/.* +[0-9]+ +([0-9]+) +([0-9]+)");
        setNewSamplesHeader("swapfile");
        try {
            String s;
            long blocks_total = 0;
            long blocks_free = 0;
            boolean lastLineMatched = false;
            while( (s = readLine()) != null) {
                if (s.startsWith("swapfile")) {
                    blocks_total = 0;
                    blocks_free = 0;
                }
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    lastLineMatched = true;
                    blocks_total += Long.parseLong(m.group(1));
                    blocks_free += Long.parseLong(m.group(2));
                } else {
                    if (lastLineMatched) {
                        addNewSample(getCurrentTimeStamp().getTimeStamp(),blocks_total,blocks_free);
                    }
                    lastLineMatched = false;
                }
            }
            series().setPreferredScaleSame(false, false, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addNewSample(long t, long blocks_total, long blocks_free) {
        series().addSampleIfNeeded("swap", "total", "blocks", t, blocks_total);
        series().addSampleIfNeeded("swap", "free",  "blocks", t, blocks_free);
        series().addSampleIfNeeded("swap", "used",  "blocks", t, blocks_total-blocks_free);
        series().addSampleIfNeeded("swap", "total", "mb",     t, ((double)blocks_total*512)/1048576.0);
        series().addSampleIfNeeded("swap", "free",  "mb",     t, ((double)blocks_free*512)/1048576.0);
        series().addSampleIfNeeded("swap", "used",  "mb",     t, ((double)(blocks_total-blocks_free)*512)/1048576.0);
    }
    
}

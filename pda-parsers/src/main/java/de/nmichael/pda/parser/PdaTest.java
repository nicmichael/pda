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

import de.nmichael.pda.Main;
import de.nmichael.pda.data.*;
import java.util.regex.*;
import java.util.*;
import java.io.*;

public class PdaTest extends Parser {
    
    // @Override
    public boolean canHandle(String filename) {
        return false;
    }
    
    public PdaTest() {
        super("PdaTest");
        setSupportedFileFormat(new FileFormatDescription(
                Main.PROGRAMSHORT,
                null,
                Main.PROGRAM,
                null,
                "Parser for Test Purposes",
                null));
    }
    
    // @Override
    public void createAllSeries() {
        parse();
    }
    
    // @Override
    public void parse() {
        long now = System.currentTimeMillis();
        Random rand = new Random();

        DataSeries line = series().getOrAddSeries("", "", "line");
        for (long i=100; i>=0; i--) {
            long v = (Math.abs(rand.nextLong()) % 10) + 1;
            if (v < 10) {
                line.addSample(now-(i*1000), v);
            } else {
                line.addSample(now-(i*1000), v, "peak");
            }
        }

        DataSeries impulses = series().getOrAddSeries("", "", "impulse");
        impulses.setPreferredStyle(DataSeriesProperties.STYLE_IMPULSES);
        for (long i=10; i>=0; i--) {
            long v = (Math.abs(rand.nextLong()) % 10) + 1;
            if (i != 5) {
                impulses.addSample(now-(i*10000), v);
            } else {
                impulses.addSample(now-(i*10000), v, "middle");
            }
        }
        
        series().setPreferredScaleSame(true, false, true);
    }
    
}

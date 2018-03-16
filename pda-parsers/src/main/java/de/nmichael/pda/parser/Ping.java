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

public class Ping extends Parser {
    
    private DataSeries ping;
    private String host;
    
    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"ping") || super.canHandle(filename,"client_");
    }
    
    public Ping() {
        super("ping");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_SOLARIS + FileFormatDescription.PRODUCT_LINUX + " / other",
                null,
                "ping / client_tcp / client_udp",
                null,
                "Network Latency Statistics",
                null));
        setDefaultInterval(1); // in case data has no timestamps
    }
    
    // @Override
    public void createAllSeries() {
        host = getMatchingStringFromFile(".*bytes from ([^ ]+) .*");
        if (host == null || host.length() == 0) {
            host = "network";
        }
        ping = series().addSeries("network", "", "ping");
    }
    
    // @Override
    public void parse() {
        Pattern pping = Pattern.compile(".*bytes from.*time=([0-9\\.]+) ms.*");
        Pattern pclient = Pattern.compile("([0-9]+)\\.([0-9]+): ([0-9]+\\.[0-9]+)ms");
        setNewSamplesHeaderPattern(pping.toString());
        try {
            String s;
            while( (s = readLine()) != null) {
                Matcher m = pping.matcher(s);
                if (m.matches()) {
                    series().addSampleIfNeeded("network", "", "ping",
                            getCurrentTimeStamp().getTimeStamp(), Double.parseDouble(m.group(1)));
                } else {
                    m = pclient.matcher(s);
                    if (m.matches()) {
                        long t = Long.parseLong(m.group(1))*1000 + Long.parseLong(m.group(2)) / 1000;
                        series().addSampleIfNeeded("network", "", "ping", t, Double.parseDouble(m.group(3)));
                    }
                }
            }
            series().setPreferredScaleIndividual();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
 }
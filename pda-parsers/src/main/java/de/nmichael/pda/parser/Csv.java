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

import de.nmichael.pda.Logger;
import de.nmichael.pda.data.*;
import de.nmichael.pda.data.TimeStamp.Fields;
import de.nmichael.pda.util.Util;
import sun.rmi.runtime.Log;

import java.util.regex.*;
import java.util.*;
import java.io.*;

public class Csv extends Parser {

    // handles a format as:
    // header;header;header;header;header;...
    // 2010-03-10 06:59:42;data;data;data;data;...

    private static final String PARAM_DELIMITER = "CSV Delimiter";
    private String delimiter = ";";
    private String myDelimiter = ";";
    private ArrayList<DataSeries> series;

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename, "csv");
    }

    public Csv() {
        super("csv");
        setParameter(PARAM_DELIMITER, delimiter);
        setSupportedFileFormat(new FileFormatDescription(FileFormatDescription.PRODUCT_GENERIC, null, "Generic CSV",
                null, "generic CSV format",
                "header 'header;header;header;...' and lines '2010-03-10 06:59:42;data;data;...'"));
        getCurrentTimeStamp().addTimeStampPattern("YYYYMMDDhhmmss.us", Pattern.compile("(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)\\.(\\d\\d\\d).*"),
                new Fields[] { Fields.year, Fields.month, Fields.day, Fields.hour, Fields.minute, Fields.second, Fields.ms });
        getCurrentTimeStamp().addTimeStampPattern("Unix TS", Pattern.compile("(\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d+)"), // 13+ digits
                new Fields[] { Fields.unixms });
        getCurrentTimeStamp().addTimeStampPattern("Unix TS", Pattern.compile("(\\d+)"),
                new Fields[] { Fields.unixsec });
        getCurrentTimeStamp().addTimeStampPattern("Seconds since start", Pattern.compile("(\\d+\\.?\\d*)"),
                new Fields[] { Fields.secSinceStart });
    }

    private static String getDelimiter(String s, String[] delims) {
        for (String d : delims) {
            if (s.indexOf(d) >= 0) {
                return d;
            }
        }
        return delims[0];
    }
    
    // @Override
    public void createAllSeries() {
        String name = Util.getNameOfFile(getFilename());
        if (name.endsWith(".csv")) {
            name = name.substring(0, name.length() - 4);
        }
        
        series = new ArrayList<DataSeries>();
        String s = readLine();
        while (s != null && s.trim().length() == 0) {
            s = readLine();
        }
        if (s != null) {
            s = s.trim();
            myDelimiter = getDelimiter(s, new String[] { delimiter, ";", ",", "|" });
            if (s.indexOf("\"") >= 0) {
                Pattern p = Pattern.compile("\"([^\"]+)\"");
                Matcher m;
                while ((m = p.matcher(s)).find()) {
                    String sub = m.group(1);
                    sub = sub.replaceAll(";", "_");
                    sub = sub.replaceAll(",", "_");
                    sub = sub.replaceAll("\\{.*\\}", "");
                    s = s.substring(0,  m.start()) + sub + (m.end() < s.length() ? s.substring(m.end()) : "");
                }
            }
            StringTokenizer tok = new StringTokenizer(s, myDelimiter);
            int i = 0;
            while (tok.hasMoreTokens()) {
                String cn = tok.nextToken();
                if (i >= 1) {
                    cn = cn.replaceAll("\"", "");
                    String[] parts = splitStringToSeriesNameParts(name + ":" + cn);
                    DataSeries ds = series().addSeries(parts[0], parts[1], parts[2]);
                    if (ds != null) {
                        logDebug("Added Series: " + ds.getName());
                    } else {
                        logWarning("Duplicate Series: " + parts[0] + ":" + parts[1] + ":" + parts[2]);
                    }
                    series.add(ds);
                }
                i++;
            }
        }
    }

    // @Override
    public void parse() {
        try {
            Logger.log(Logger.LogType.debug, "Parsing " + getFilename() + " with "
                    + (series != null ? series.size() : 0) + " series using delimiter: " + myDelimiter);
            String s;
            while ((s = readLineNoTime()) != null) {
                s = s.trim();
                if (s.length() > 0) {
                    String[] fields = s.split(myDelimiter, -1);
                    if (fields.length > 0) {
                        getCurrentTimeStamp().getTimeStampFromLine(fields[0], null, 1, true);
                        long t = getCurrentTimeStamp().getTimeStamp();
                        for (int i=0; i<fields.length - 1; i++) {
                            String val = fields[i + 1];
                            if (val.length() > 0) {
                                try {
                                    DataSeries ser = (i < series.size() ? series.get(i) : null);
                                    if (ser != null) {
                                        ser.addSampleIfNeeded(t, Double.parseDouble(val));
                                    }
                                } catch (NumberFormatException ee) {
                                    // nothing to do (ignore this sample!
                                }
                            }
                        }
                    }
                }
            }
            series().setPreferredScaleIndividual();
        } catch (Exception e) {
            logError("Caught Exception", e);
        }
    }

    // @Override
    public void setParameter(String name, String value) {
        super.setParameter(name, value);
        if (PARAM_DELIMITER.equals(name)) {
            try {
                delimiter = (value != null && value.length() > 0 ? value : ";");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    }

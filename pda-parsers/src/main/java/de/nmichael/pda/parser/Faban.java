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

public class Faban extends Parser {

    private static final String FABAN_CTHRU   = "CThru";
    private static final String FABAN_OTHRU   = "OThru";
    private static final String FABAN_CERR    = "CErr";
    private static final String FABAN_CRESP   = "CResp";
    private static final String FABAN_ORESP   = "OResp";
    private static final String FABAN_CSD     = "CSD";
    private static final String FABAN_OSD     = "OSD";
    private static final String FABAN_C90RESP = "C90%Resp";
    private static final String FABAN_O90RESP = "O90%Resp";
    
    public boolean canHandle(String filename) {
        return super.canHandle(filename,"driver.log");
    }
    
    public Faban() {
        super("faban");
        setSupportedFileFormat(new FileFormatDescription(
                "Faban",
                null,
                "driver.log",
                null,
                "Faban Driver Statistics Logfile",
                null));
    }
    
    private void createSeries(String category, String subcategory) {
        series().addSeries(category, subcategory, FABAN_CTHRU);
        series().addSeries(category, subcategory, FABAN_OTHRU);
        series().addSeries(category, subcategory, FABAN_CERR);
        series().addSeries(category, subcategory, FABAN_CRESP);
        series().addSeries(category, subcategory, FABAN_ORESP);
        series().addSeries(category, subcategory, FABAN_CSD);
        series().addSeries(category, subcategory, FABAN_OSD);
        series().addSeries(category, subcategory, FABAN_C90RESP);
        series().addSeries(category, subcategory, FABAN_O90RESP);
    }
    
    // @Override
    public void createAllSeries() {
        parse();
    }
    
    private void addSample(String category, String subcategory, String series, long t, String value) {
        if (series().getSeries(category, subcategory, FABAN_CTHRU) == null) {
            createSeries(category, subcategory);
        }
        try { 
            double v = Float.parseFloat(value); 
            series().addSample(category, subcategory, series, t, v);
        } catch(Exception eignore) {
        }
    }
    
    // @Override
    public void parse() {
        try {
            String s;
            Pattern p = Pattern.compile("INFO: [^ ]+ - ([^ ]+): ([^ ]+) CThru=([0-9\\.\\-/]+) OThru=([0-9\\.\\-/]+) CErr=([0-9\\.\\-/]+) CResp=([0-9\\.\\-/]+) OResp=([0-9\\.\\-/]+) CSD=([0-9\\.\\-/]+) OSD=([0-9\\.\\-/]+) C90%Resp=([0-9\\.\\-/]+) O90%Resp=([0-9\\.\\-/]+)");
            while( (s = readLine()) != null) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    long t = getCurrentTimeStamp().getTimeStamp();
                    String driverName = m.group(1);
                    StringTokenizer tNameTok   = new StringTokenizer(m.group(2), "/");
                    StringTokenizer cThruTok   = new StringTokenizer(m.group(3), "/");
                    StringTokenizer oThruTok   = new StringTokenizer(m.group(4), "/");
                    StringTokenizer cErrTok    = new StringTokenizer(m.group(5), "/");
                    StringTokenizer cRespTok   = new StringTokenizer(m.group(6), "/");
                    StringTokenizer oRespTok   = new StringTokenizer(m.group(7), "/");
                    StringTokenizer csdTok     = new StringTokenizer(m.group(8), "/");
                    StringTokenizer osdTok     = new StringTokenizer(m.group(9), "/");
                    StringTokenizer c90RespTok = new StringTokenizer(m.group(10), "/");
                    StringTokenizer o90RespTok = new StringTokenizer(m.group(11), "/");

                    while (tNameTok.hasMoreTokens()) {
                        String tName = tNameTok.nextToken();
                        String cThru = cThruTok.nextToken();
                        String oThru = oThruTok.nextToken();
                        String cErr = cErrTok.nextToken();
                        String cResp = cRespTok.nextToken();
                        String oResp = oRespTok.nextToken();
                        String csd = csdTok.nextToken();
                        String osd = osdTok.nextToken();
                        String c90Resp = c90RespTok.nextToken();
                        String o90Resp = o90RespTok.nextToken();
                        String category = driverName + ":" + tName;
                        addSample(driverName, tName, FABAN_CTHRU, t, cThru);
                        addSample(driverName, tName, FABAN_OTHRU, t, oThru);
                        addSample(driverName, tName, FABAN_CERR, t, cErr);
                        addSample(driverName, tName, FABAN_CRESP, t, cResp);
                        addSample(driverName, tName, FABAN_ORESP, t, oResp);
                        addSample(driverName, tName, FABAN_CSD, t, csd);
                        addSample(driverName, tName, FABAN_OSD, t, osd);
                        addSample(driverName, tName, FABAN_C90RESP, t, c90Resp);
                        addSample(driverName, tName, FABAN_O90RESP, t, o90Resp);
                    }
                }
            }
            series().setPreferredScaleSame(new String[] { FABAN_CTHRU, FABAN_OTHRU });
            series().setPreferredScaleSame(new String[] { FABAN_CERR });
            series().setPreferredScaleSame(new String[] { FABAN_CRESP, FABAN_ORESP,
                                                          FABAN_C90RESP, FABAN_O90RESP});
            series().setPreferredScaleSame(new String[] { FABAN_CSD, FABAN_OSD });
        } catch(Exception e) {
            logError(e.toString());
        }
    }
    
}

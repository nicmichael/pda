package de.nmichael.pda.converter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.nmichael.pda.Logger;
import de.nmichael.pda.data.Converter;
import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.DataSeriesSet;
import de.nmichael.pda.data.Sample;
import de.nmichael.pda.util.Util;

public class CsvConverter implements Converter {
    
    private static String SEP = ";";
    private static long TOLERANCE = 1000;
    
    private void writeHeader(BufferedWriter f, ArrayList<DataSeries> series) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append("Timestamp");
        for (DataSeries ser : series) {
            s.append(SEP + ser.getLocalName());
        }
        s.append("\n");
        f.write(s.toString());
    }
    
    private void writeData(BufferedWriter f, DataSeriesSet series, long ts) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append(ts);
        Sample[] samples = series.getSamplesAtTimestamp(false, ts, TOLERANCE, false);
        for (Sample smp : samples) {
            s.append(SEP + (smp != null ? Double.toString(smp.getValue()) : ""));
        }
        s.append("\n");
        f.write(s.toString());
    }
    
    @Override
    public boolean convert(String filename, String parser, DataSeriesSet series) {
        if (System.getProperty("csv.sep") != null && System.getProperty("csv.sep").length() > 0) {
            SEP = System.getProperty("csv.sep");
        }
        if (System.getProperty("csv.tolerance") != null && System.getProperty("csv.tolerance").length() > 0) {
            TOLERANCE = Util.string2long(System.getProperty("csv.tolerance"), TOLERANCE);
        }
        Logger.info("CsvConverter using separator '" + SEP + "' (-Dcsv.sep) and sampling interval tolerance " + TOLERANCE + "ms (-Dcsv.tolerance)");

        String csvFile = filename + ".csv";
        Logger.info("Creating " + csvFile + " with " + series.size() + " series ...");
        try {
            BufferedWriter f = new BufferedWriter(new FileWriter(csvFile));
            ArrayList<DataSeries> allSeries = series.getAllSeries();
            writeHeader(f, allSeries);
            long t = series.getNextTimestamp(false,  0, TOLERANCE);
            long tLast = series.getLastTimestamp(false);
            while (t <= tLast) {
                writeData(f, series, t);
                t = series.getNextTimestamp(false,  t, TOLERANCE);
            }
            f.close();
        } catch(Exception e) {
            Logger.error("Conversion to " + csvFile + " failed: " + e);
            e.printStackTrace();
        }
        return true;
    }
    
    public static void main(String[] args) {
        System.out.println(";;;;".split(";", -1).length);
    }

}

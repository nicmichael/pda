package de.nmichael.pda.parser;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.nmichael.pda.Logger;
import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.FileFormatDescription;
import de.nmichael.pda.data.Parser;
import de.nmichael.pda.data.TimeStamp.Fields;
import de.nmichael.pda.util.Util;

public class KeyValue extends Parser {
    
    private static Pattern p = Pattern.compile("([a-zA-Z0-9_\\-:]+)=([0-9\\.]+)");

    // @Override
    public boolean canHandle(String filename) {
        return super.canHandle(filename, "kv");
    }

    public KeyValue() {
        super("kv");
        setSupportedFileFormat(new FileFormatDescription(FileFormatDescription.PRODUCT_GENERIC, null, "Generic Key-Value",
                null, "generic key-value format",
                ".*key=value.*"));
        getCurrentTimeStamp().addTimeStampPattern("YYYYMMDDhhmmss.us", Pattern.compile("(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)\\.(\\d\\d\\d).*"),
                new Fields[] { Fields.year, Fields.month, Fields.day, Fields.hour, Fields.minute, Fields.second, Fields.ms });
    }

    // @Override
    public void createAllSeries() {
        parse();
    }

    // @Override
    public void parse() {
        try {
            String s;
            while ((s = readLine()) != null) {
                s = s.trim();
                if (s.length() > 0) {
                    Matcher m = p.matcher(s);
                    while (m.find()) {
                        try {
                            // logDebug("Found: " + m.group(1) + "=" + m.group(2));
                            String key = m.group(1);
                            double value = Double.parseDouble(m.group(2));
                            String category = key;
                            String subcategory = "";
                            String series = "";
                            series().getOrAddSeries(category, subcategory, series).addSampleIfNeeded(getCurrentTimeStamp().getTimeStamp(), value);
                        } catch(Exception eignore) {
                            // invalid format or things that aren't really key=value, e.g. key=1.2.3 etc.
                        }
                    }
                }
            }
            series().setPreferredScaleIndividual();
        } catch (Exception e) {
            logError("Caught Exception", e);
        }
    }

}

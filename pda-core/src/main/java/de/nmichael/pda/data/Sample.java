/**
 * Title:        Performance Data Analyzer (PDA)
 * Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
 * Website:      http://pda.nmichael.de/
 * License:      GNU General Public License v2
 *
 * @author Nicolas Michael
 * @version 2
 */
package de.nmichael.pda.data;

import java.util.*;
import java.util.regex.*;
import gov.noaa.pmel.util.GeoDate;
import de.nmichael.pda.util.*;

public class Sample {

    protected long timestamp;
    private double value;

    private static Calendar calendar = new GregorianCalendar();

    public static long createTimeStamp(GeoDate g) {
        double secMsec = g.getGMTSeconds();
        int sec = (int) Math.abs(secMsec);
        int ms = (int) Math.abs((secMsec - sec) * 1000);
        return TimeStamp.createTimeStamp(g.getGMTYear(), g.getGMTMonth(), g.getGMTDay(), g.getGMTHours(), g.getGMTMinutes(), sec, ms);
    }

    public static String createTimeStampString(long t) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(new SimpleTimeZone(0, ""));
        cal.setTimeInMillis(t);
        return Util.digits(cal.get(Calendar.DAY_OF_MONTH), 2) + "."
                + Util.digits(cal.get(Calendar.MONTH) + 1, 2) + "."
                + Util.digits(cal.get(Calendar.YEAR), 2) + " "
                + Util.digits(cal.get(Calendar.HOUR_OF_DAY), 2) + ":"
                + Util.digits(cal.get(Calendar.MINUTE), 2) + ":"
                + Util.digits(cal.get(Calendar.SECOND), 2) + "."
                + Util.digits(cal.get(Calendar.MILLISECOND), 2);
    }

    public static String createTimeStampStringOnlyTime(long t) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(new SimpleTimeZone(0, ""));
        cal.setTimeInMillis(t);
        return Util.digits(cal.get(Calendar.HOUR_OF_DAY), 2) + ":"
                + Util.digits(cal.get(Calendar.MINUTE), 2) + ":"
                + Util.digits(cal.get(Calendar.SECOND), 2) + "."
                + Util.digits(cal.get(Calendar.MILLISECOND), 2);
    }
    
    public static long getGMTTimeStamp(long localTimeStamp) {
        return localTimeStamp + calendar.getTimeZone().getOffset(localTimeStamp);
    }
    
    public Sample(long timestamp, double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public void setTimeStamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void incValue(double value) {
        setValue(getValue() + value);
    }

    public double getValue() {
        return value;
    }
    
    public String getLabel() {
        return null;
    }
    
}

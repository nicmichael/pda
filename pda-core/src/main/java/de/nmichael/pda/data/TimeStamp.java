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

import de.nmichael.pda.Logger;
import java.util.*;
import java.io.*;
import java.util.regex.*;
import de.nmichael.pda.util.*;

public class TimeStamp {
	
	// maximum number of attempts to match a pattern before it is discarded
	private static final int TIMESTAMP_MATCHING_ATTEMPTS = 10;
    
    public enum Fields {
        year, month, day, hour, minute, second, ms,
        nameOfMonth, unixms, unixsec, ampm,
        weekday
    }
    
    // Current Timestamp
    private int YY,MM,DD,hh,mm,ss,ms;
    
    // Have we ever found any timestamps in this file, or is this a timestamp-less file?
    private int numberOfTimestampsFound = 0;
    private int numberOfHeadersFound = 0;
            
    private long offHH = 0;
    private long offMM = 0;
    private long offSS = 0;
    private boolean onlyTsWithDate = false;
    
    private ArrayList<String> pTimestampDescription = new ArrayList<String>();
    private ArrayList<Pattern> pTimestampPattern = new ArrayList<Pattern>();
    private ArrayList<Fields[]> pTimestampGroupOrder = new ArrayList<Fields[]>();
    private ArrayList<Boolean> matchedTimestamps = new ArrayList<Boolean>(); 

    public TimeStamp(long offHH, long offMM, long offSS) {
        initializePatterns();
        reset(offHH, offMM, offSS);
    }
    
    public void reset(long offHH, long offMM, long offSS) {
        setToToday();
        this.offHH = offHH;
        this.offMM = offMM;
        this.offSS = offSS;
        numberOfTimestampsFound = 0;
        numberOfHeadersFound = 0;
    }
    
    private synchronized void initializePatterns() {
        // typical YYYY-MM-DD hh:mm:ss timestamp
        addTimeStampPattern("YYYY[.-/]MM[.-/]DD[- T]hh[:-.]mm[:-.]ss (AP)",
                            Pattern.compile(".*(\\d\\d\\d\\d)[\\.\\-/](\\d\\d)[\\.\\-/](\\d\\d)[\\- T](\\d\\d)[:\\-\\.](\\d\\d)[:\\-\\.](\\d\\d) ?([AP]?M?).*"),
                            new Fields[] { Fields.year, Fields.month, Fields.day, Fields.hour, Fields.minute, Fields.second, Fields.ampm } );

        // typical MM.DD.YYYY hh:mm:ss timestamp
        addTimeStampPattern("MM[.-/]DD[.-/]YYYY[- ]hh[:-.]mm[:-.]ss (AP)",
                            Pattern.compile(".*(\\d\\d)[\\.\\-/](\\d\\d)[\\.\\-/](\\d\\d\\d\\d)[\\- ](\\d\\d)[:\\-\\.](\\d\\d)[:\\-\\.](\\d\\d) ?([AP]?M?).*"),
                            new Fields[] { Fields.month, Fields.day, Fields.year, Fields.hour, Fields.minute, Fields.second, Fields.ampm } );
        
        // typical YYYY-MM-DD hh:mm:ss.mss timestamp
        addTimeStampPattern("YYYY[.-/]MM[.-/]DD[- T]hh[:-.]mm[:-.]ss[:-.,]mss",
                            Pattern.compile(".*(\\d\\d\\d\\d)[\\.\\-/](\\d\\d)[\\.\\-/](\\d\\d)[\\- T](\\d\\d)[:\\-\\.](\\d\\d)[:\\-\\.](\\d\\d)[:\\-\\.,](\\d\\d\\d).*"),
                            new Fields[] { Fields.year, Fields.month, Fields.day, Fields.hour, Fields.minute, Fields.second, Fields.ms } );

        // timestamp as shown by "date" command
        addTimeStampPattern("WKD MON DD hh:mm:ss (TMZ) YYYY",
                            Pattern.compile(".*(\\w\\w\\w) (\\w\\w\\w) +(\\d\\d?) (\\d\\d):(\\d\\d):(\\d\\d) [^ ]* *(\\d\\d\\d\\d).*"),
                            new Fields[] { Fields.weekday, Fields.nameOfMonth, Fields.day, Fields.hour, Fields.minute, Fields.second, Fields.year } );
        
        // I don't know where this pattern came from...
        addTimeStampPattern("YYYY MON DD hh:mm:ss (AP)",
                            Pattern.compile(".*(\\d\\d\\d\\d) (\\w\\w\\w) +(\\d\\d?) (\\d\\d):(\\d\\d):(\\d\\d) ?([AP]?M?).*"),
                            new Fields[] { Fields.year, Fields.nameOfMonth, Fields.day, Fields.hour, Fields.minute, Fields.second, Fields.ampm } );
        
        // WebLogic Log Timestamps
        addTimeStampPattern("MON DD, YYYY hh:mm:ss (AP)",
                            Pattern.compile(".*(\\w\\w\\w) +(\\d\\d?), (\\d\\d\\d\\d) (\\d\\d?):(\\d\\d):(\\d\\d) ?([AP]?M?) .*"),
                            new TimeStamp.Fields[] { TimeStamp.Fields.nameOfMonth, TimeStamp.Fields.day, TimeStamp.Fields.year, 
                                                     TimeStamp.Fields.hour, TimeStamp.Fields.minute, TimeStamp.Fields.second, TimeStamp.Fields.ampm } );

        // some special tooling
        addTimeStampPattern("TIME=YYYYMMDDhhmmss",
                            Pattern.compile(".*TIME=(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d).*"),
                            new Fields[] { Fields.year, Fields.month, Fields.day, Fields.hour, Fields.minute, Fields.second } );

        // typical MM/DD/YYYY timestamp (like in "sar -u")
        addTimeStampPattern("MM/DD/YYYY",
                            Pattern.compile(".*(\\d\\d)/(\\d\\d)/(\\d\\d\\d\\d).*"),
                            new Fields[] { Fields.month, Fields.day, Fields.year } );

        // typical hh:mm:ss timestamp (like in "sar -u")
        addTimeStampPattern("hh:mm:ss (AP)",
                            Pattern.compile(".*(\\d\\d):(\\d\\d):(\\d\\d) ?([AP]?M?).*"),
                            new Fields[] { Fields.hour, Fields.minute, Fields.second, Fields.ampm } );
        
        // TS=unixms
        addTimeStampPattern("TS=unixms",
                            Pattern.compile("TS=(\\d+).*"),
                            new Fields[] { Fields.unixms } );
        
    }
    
    public synchronized void addTimeStampPattern(String description, Pattern pattern, Fields[] groupOrder) {
        pTimestampDescription.add(description);
        pTimestampPattern.add(pattern);
        pTimestampGroupOrder.add(groupOrder);
    }
    
    public synchronized void addTimeStampPatternFirst(String description, Pattern pattern, Fields[] groupOrder) {
        pTimestampDescription.add(0, description);
        pTimestampPattern.add(0, pattern);
        pTimestampGroupOrder.add(0, groupOrder);
    }

    public synchronized void deleteTimeStampPattern(int idx) {
        if (idx >= 0 && idx < size()) {
            pTimestampDescription.remove(idx);
            pTimestampPattern.remove(idx);
            pTimestampGroupOrder.remove(idx);
        }
    }
    
    public void setOnlyTimeStampsWithDate(boolean onlyTsWithDate) {
        this.onlyTsWithDate = onlyTsWithDate;
    }
    
    public synchronized void deleteAllTimeStampPatterns() {
        while (size() > 0) {
            deleteTimeStampPattern(0);
        }
    }
    
    public synchronized int size() {
        return pTimestampDescription.size();
    }
    
    public synchronized String getTimeStampDescription(int idx) {
        return (idx >= 0 && idx < size() ? pTimestampDescription.get(idx) : null);
    }
    
    public synchronized Pattern getTimeStampPattern(int idx) {
        return (idx >= 0 && idx < size() ? pTimestampPattern.get(idx) : null);
    }
    
    public synchronized Fields[] getTimeStampGroupOrder(int idx) {
        return (idx >= 0 && idx < size() ? pTimestampGroupOrder.get(idx) : null);
    }
    
    public synchronized String getTimeStampFromLine(String s, Pattern newSamplesHeader, 
            long interval, boolean trim) {
        for(int i=0; i<size(); i++) {
        	if (numberOfTimestampsFound >= TIMESTAMP_MATCHING_ATTEMPTS && (i >= matchedTimestamps.size() || !matchedTimestamps.get(i))) {
        		continue; // we've found plenty of timestamps, but never this one... so let's not check for it any more
        	}

            Matcher m = getTimeStampPattern(i).matcher(s);
            if (m.matches()) {
                int lastDay = DD;
                int lastHour = hh;
                boolean patternWithDay = false;
                Fields[] fields = getTimeStampGroupOrder(i);
                if (onlyTsWithDate) {
                    boolean dateInPattern = false;
                    for (int j=0; j<fields.length; j++) {
                        switch(fields[j]) {
                            case year:
                            case month:
                            case nameOfMonth:
                            case day:
                                dateInPattern = true;
                                break;
                        }
                    }
                    if (!dateInPattern) {
                        continue;
                    }
                }

                numberOfTimestampsFound++;
                for (int g=0; g<m.groupCount(); g++) {
                    if (g < fields.length) {
                        switch(fields[g]) {
                            case year:
                                YY = Integer.parseInt(m.group(g+1));
                                break;
                            case month:
                                MM = Integer.parseInt(m.group(g+1));
                                break;
                            case nameOfMonth:
                                String month = m.group(g+1);
                                if (month.equals("Jan")) {
                                    MM = 1;
                                } else if (month.equals("Feb")) {
                                    MM = 2;
                                } else if (month.equals("Mar")) {
                                    MM = 3;
                                } else if (month.equals("Apr")) {
                                    MM = 4;
                                } else if (month.equals("May")) {
                                    MM = 5;
                                } else if (month.equals("Jun")) {
                                    MM = 6;
                                } else if (month.equals("Jul")) {
                                    MM = 7;
                                } else if (month.equals("Aug")) {
                                    MM = 8;
                                } else if (month.equals("Sep")) {
                                    MM = 9;
                                } else if (month.equals("Oct")) {
                                    MM = 10;
                                } else if (month.equals("Nov")) {
                                    MM = 11;
                                } else if (month.equals("Dec")) {
                                    MM = 12;
                                }
                                break;
                            case day:
                                DD = Integer.parseInt(m.group(g+1));
                                patternWithDay = true;
                                break;
                            case hour:
                                hh = Integer.parseInt(m.group(g+1));
                                break;
                            case minute:
                                mm = Integer.parseInt(m.group(g+1));
                                break;
                            case second:
                                ss = Integer.parseInt(m.group(g+1));
                                break;
                            case ms:
                                ms = Integer.parseInt(m.group(g+1));
                                break;
                            case ampm:
                                String aps = m.group(g+1).toLowerCase();
                                if (aps != null && aps.equals("am") && hh == 12) {
                                    hh -= 12;
                                }
                                if (aps != null && aps.equals("pm") && hh != 12) {
                                    hh += 12;
                                }
                                break;
                            case unixms:
                                set(Long.parseLong(m.group(g+1)));
                                patternWithDay = true;
                                break;
                            case unixsec:
                                set(Long.parseLong(m.group(g+1)) * 1000l);
                                patternWithDay = true;
                                break;
                        }
                    }
                }
                hh += offHH;
                mm += offMM;
                ss += offSS;
                if (offHH > 0 || offMM > 0 || offSS > 0) {
                    ensureCorrectTime( (offHH*3600+offMM*60+offSS> 0 ? 1 : -1) );
                }
                
                if (hh < (lastHour-12) && DD == lastDay && !patternWithDay) {
                    // new day (timestamp pattern with only time, no date)
                    // we check if "hh < (lastHour-10)" to make sure that slightly unsorted
                    // time data like "23:59:58 -> 00:00:00 -> 23:59:59" isn't considered as
                    // a change to a new day. By substracting 12, we only consider a date
                    // change if the new hour is 12 or more hours "before" the old one.
                    DD++;
                    ensureCorrectTime(1);
                }
                
                if (trim) {
                    int begin = m.start(1);
                    int end = m.end(m.groupCount());
                    s = (begin > 0 ? s.substring(0, begin - 1) : "")
                            + (end < s.length() ? s.substring(end + 1, s.length()) : "");
                }

                if (numberOfTimestampsFound <= TIMESTAMP_MATCHING_ATTEMPTS) {
            	    setMatched(i);
                }
                
                return s;
            }
        }
        
        if (numberOfTimestampsFound < 2 && newSamplesHeader != null) {
            if (newSamplesHeader.matcher(s).matches() && ++numberOfHeadersFound > 1
                    && interval > 0) {
                ss += interval;
                ensureCorrectTime(1);
                return s;
            }
        }

        return s;
    }
    
    private void setMatched(int i) {
    	while (i >= matchedTimestamps.size()) {
    		matchedTimestamps.add(false);
    	}
    	matchedTimestamps.set(i, true);
    }
    
    public void set(long timestamp) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        DD = cal.get(Calendar.DAY_OF_MONTH);
        MM = cal.get(Calendar.MONTH) + 1;
        YY = cal.get(Calendar.YEAR);
        hh = cal.get(Calendar.HOUR_OF_DAY);
        mm = cal.get(Calendar.MINUTE);
        ss = cal.get(Calendar.SECOND);
        ms = cal.get(Calendar.MILLISECOND);
    }
    
    public void set(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month-1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        DD = cal.get(Calendar.DAY_OF_MONTH);
        MM = cal.get(Calendar.MONTH) + 1;
        YY = cal.get(Calendar.YEAR);
        hh = cal.get(Calendar.HOUR_OF_DAY);
        mm = cal.get(Calendar.MINUTE);
        ss = cal.get(Calendar.SECOND);
        ms = cal.get(Calendar.MILLISECOND);
    }
    
    public void setToNow() {
        GregorianCalendar cal = new GregorianCalendar();
        YY = cal.get(Calendar.YEAR);
        MM = cal.get(Calendar.MONTH) + 1;
        DD = cal.get(Calendar.DAY_OF_MONTH);
        hh = cal.get(Calendar.HOUR_OF_DAY);
        mm = cal.get(Calendar.MINUTE);
        ss = cal.get(Calendar.SECOND);
        ms = cal.get(Calendar.MILLISECOND);
    }

    public void setToToday() {
        GregorianCalendar cal = new GregorianCalendar();
        YY = cal.get(Calendar.YEAR);
        MM = cal.get(Calendar.MONTH) + 1;
        DD = cal.get(Calendar.DAY_OF_MONTH);
        hh = 0;
        mm = 0;
        ss = 0;
        ms = 0;
    }

    public void incTime(int sec) {
        if (sec == 0) {
            return;
        }
        ss += sec;
        ensureCorrectTime((sec > 0 ? 1 : -1));
    }

    public void incTimeMs(int msec) {
        if (ms == 0) {
            return;
        }
        ms += msec;
        ensureCorrectTime((ms > 0 ? 1 : -1));
    }

    public void ensureCorrectTime(int direction) {
        if (direction >= 0) {
            while (ms > 999) {
                ms -= 1000;
                ss++;
            }
            while (ss > 59) {
                ss -= 60;
                mm++;
            }
            while (mm > 59) {
                mm -= 60;
                hh++;
            }
            while (hh > 23) {
                hh -= 24;
                DD++;
            }
            if (DD > 31
                    || (DD > 30 && (MM == 4 || MM == 6 || MM == 9 || MM == 11))
                    || (DD > 29 && MM == 2)
                    || (DD > 28 && MM == 2 && YY % 4 != 0)) {
                DD = 1;
                MM++;
            }
            if (MM > 12) {
                MM = 1;
                YY++;
            }
        }
        if (direction <= 0) {
            while (ms < 0) {
                ms += 1000;
                ss--;
            }
            while (ss < 0) {
                ss += 60;
                mm--;
            }
            while (mm < 0) {
                mm += 60;
                hh--;
            }
            while (hh < 0) {
                hh += 24;
                DD--;
            }
            if (DD < 0) {
                MM--;
                DD = (MM == 4 || MM == 6 || MM == 9 || MM == 11 ? 30
                        : (MM == 2 && YY % 4 == 0 ? 29
                        : (MM == 2 && YY % 4 != 0 ? 28 : 31)));
            }
            if (MM < 0) {
                MM = 12;
                YY--;
            }
        }
    }
    
    public long getTimeStamp() {
        return createTimeStamp(YY, MM, DD, hh, mm, ss, ms);
    }
    
    public String toString() {
        return toString(true);
    }
    
    public String toString(boolean withMs) {
        return Util.digits(YY, 4) + "-" + Util.digits(MM, 2) + "-" + Util.digits(DD, 2) + " " + 
               Util.digits(hh, 2) + ":" + Util.digits(mm, 2) + ":" + Util.digits(ss, 2) + 
               (withMs ? "," + Util.digits(ms, 3) : "");
    }

    public static long createTimeStamp(int YY, int MM, int DD, int hh, int mm, int ss, int ms) {
        GregorianCalendar cal = new GregorianCalendar(YY, MM - 1, DD, hh, mm, ss);
        cal.setTimeZone(new SimpleTimeZone(0, ""));
        // cal.setTimeZone(TimeZone.getDefault());
        return cal.getTimeInMillis() + ms;
    }
}

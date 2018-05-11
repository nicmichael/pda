/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda;

import de.nmichael.pda.gui.BaseDialog;
import de.nmichael.pda.util.Util;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Logger {
    
    private static boolean debugLogging = false;

    public enum LogType {
        info,
        warning,
        error,
        debug
    }
    
    private static String getTypeString(LogType type) {
        switch(type) {
            case info:
                return "INFO   ";
            case warning:
                return "WARNING";
            case error:
                return "ERROR  ";
            case debug:
                return "DEBUG  ";
        }
        return "UNKNOWN";
    }
    
    private static String makeTimeString(int value, int chars) {
        String s = Integer.toString(value);
        while (s.length() < chars) {
            s = "0" + s;
        }
        return s;
    }
    
    private static String getCurrentTimeStamp() {
        Calendar cal = new GregorianCalendar();
        return makeTimeString(cal.get(Calendar.DAY_OF_MONTH), 2) + "."
                + makeTimeString(cal.get(Calendar.MONTH) + 1, 2) + "."
                + makeTimeString(cal.get(Calendar.YEAR), 4) + " "
                + makeTimeString(cal.get(Calendar.HOUR_OF_DAY), 2) + ":"
                + makeTimeString(cal.get(Calendar.MINUTE), 2) + ":"
                + makeTimeString(cal.get(Calendar.SECOND), 2);
    }
    
    public static void log(LogType type, String msg) {
        if (type == LogType.debug && !debugLogging) {
            return;
        }
        System.out.println("[" + getCurrentTimeStamp() + "] - " + getTypeString(type) + " - " + msg);
        if (type == LogType.error && Main.isGUI) {
            BaseDialog.errorDlg(null, Util.breakLine(msg, 80));
        }
    }
    
    public static void info(String s) {
        log(LogType.info, s);
    }
    
    public static void warning(String s) {
        log(LogType.warning, s);
    }
    
    public static void error(String s) {
        log(LogType.error, s);
    }
    
    public static void debug(String s) {
        log(LogType.debug, s);
    }
    
    public static boolean isDebugLoggin() {
        return debugLogging;
    }
    
    static void setDebugLogging(boolean debug) {
        debugLogging = debug;
        log(LogType.debug, "Verbose Mode enabled.");
    }
    
}

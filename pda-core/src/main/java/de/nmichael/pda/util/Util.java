/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.util;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.regex.*;

public class Util {
    
    static final Pattern pNumber = Pattern.compile("([0-9\\.]+)([^0-9\\\\.]+)");
    
    public static Color getColor(String s) {
        int cint;
        try {
            cint = Integer.parseInt(s,16);
        } catch(Exception ee) {
            cint = 204*65536 + 204*256 + 204;
        }
        return new Color(cint);
    }
    
    public static String getColor(Color c) {
        if (c == null) {
            c = Color.black;
        }
        String s = "";
        float[] rgb = c.getRGBColorComponents(null);
        for (int i=0; i<rgb.length; i++)
            s += hexByte((int)(rgb[i]*255));
        return s;
    }
    
    public static char getHexDigit(int i) {
        if (i<10) return (char)(i+'0');
        else return (char)((i-10)+'A');
    }
    
    public static String hexByte(int i) {
        return getHexDigit(i / 16) + "" + getHexDigit(i % 16);
    }
    
    public static String getPathOfFile(String fileName) {
        if (fileName == null) return null;
        String s = new File(fileName).getName();
        int wo;
        if (s != null && (wo =fileName.lastIndexOf(s)) >= 0)
            if (wo != 0) {
              s = fileName.substring(0,wo-1);
            } else return "."+System.getProperty("file.separator");
        else s = "."+System.getProperty("file.separator");
        return s;
    }    
    
    public static String getNameOfFile(String fileName) {
        if (fileName == null) return null;
        return new File(fileName).getName();
    }
    
    public static boolean isRelativePath(String fname) {
        String sep = System.getProperty("file.separator");
        if (fname.startsWith(sep)) return false;
        if (fname.length() >= 3 && fname.charAt(1) == ':' && sep.equals(String.valueOf(fname.charAt(2)))) return false;
        return true;
    }
    
    public static String digits(int v, int dig) {
        String s = Integer.toString(v);
        while (s.length() < dig) s = "0"+s;
        return s;
    }
    
    public static String stringOfLength(String s, int length, char fill, boolean appendright) {
        while (s.length() < length) {
            if (appendright) {
                s = s + fill;
            } else {
                s = fill + s;
            }
        }
        return s;
    }
    
     public static long getSecsFromHHMMSS(String s) {
         Pattern p = Pattern.compile("(\\d+):(\\d+):(\\d+)");
         Matcher m = p.matcher(s);
         if (m.matches()) {
             return Long.parseLong(m.group(1))*3600 + Long.parseLong(m.group(2))*60 + Long.parseLong(m.group(3));
         } else {
             p = Pattern.compile("(\\d+):(\\d+)");
             m = p.matcher(s);
             if (m.matches()) {
                 return Long.parseLong(m.group(1))*60 + Long.parseLong(m.group(2));
             } else {
                 p = Pattern.compile("(\\d+)");
                 m = p.matcher(s);
                 if (m.matches()) {
                     return Long.parseLong(m.group(1));
                 }
             }
         }
         return 0;
     }
     
     public static long getSecsFromDDHHMMSS(String s) {
         Pattern p = Pattern.compile("(\\d+)-(\\d+):(\\d+):(\\d+)");
         Matcher m = p.matcher(s);
         if (m.matches()) {
             return Long.parseLong(m.group(1))*86400 + Long.parseLong(m.group(2))*3600 + Long.parseLong(m.group(3))*60 + Long.parseLong(m.group(4));
         } else {
             p = Pattern.compile("(\\d+):(\\d+):(\\d+)");
             m = p.matcher(s);
             if (m.matches()) {
                 return Long.parseLong(m.group(1))*3600 + Long.parseLong(m.group(2))*60 + Long.parseLong(m.group(3));
             } else {
                 p = Pattern.compile("(\\d+):(\\d+)");
                 m = p.matcher(s);
                 if (m.matches()) {
                     return Long.parseLong(m.group(1))*60 + Long.parseLong(m.group(2));
                 } else {
                     p = Pattern.compile("(\\d+)");
                     m = p.matcher(s);
                     if (m.matches()) {
                         return Long.parseLong(m.group(1));
                     }
                 }
             }
         }
         return 0;
     }
     
     public static double roundUpToScale(double d) {
         if (d == 0.0) return 1;
         if (d == 1.0) return 1;
         double log10 = Math.ceil(Math.log10(d));    // d = 76 -> log10 = 2
         double resolution = ((double)Math.pow(10.0,log10)) / 10.0;  // log10 = 2 -> resolution = 10
         double scale = resolution;
         while (scale < d) {
             scale += resolution;
         }
         return scale; // d = 76 && resolution = 10 -> 80
     }
     
     public static String round(double d) {
       return Double.toString(Math.floor(d*100.0)/100.0);
     }
     
     public static String round(float d) {
         return Float.toString((float)Math.floor(d*100.0f)/100.0f);
     }
     
     public static String double2string(double d, int decimals, boolean kmg) {
         return double2string(d, 1, decimals, kmg);
     }
     
     public static String double2string(double d, int digits, int decimals, boolean kmg) {
         String unit = null;
         if (d == Double.MIN_VALUE) {
             return "MIN";
         }
         if (d == Double.MAX_VALUE) {
             return "MAX";
         }
         if (d == Double.NaN) {
             return "NaN";
         }
         if (kmg) {
             if (Math.abs(d) > 1000000000) {
                 d /= 1000000000;
                 unit = "G";
             }
             if (Math.abs(d) > 1000000) {
                 d /= 1000000;
                 unit = "M";
             }
             if (Math.abs(d) > 1000) {
                 d /= 1000;
                 unit = "K";
             }
         }
         return String.format("% " + digits + "." + decimals + "f", d) + (unit != null ? unit : "");
     }
     
     public static Vector<String> split(String s, String sep) {
         StringTokenizer tok = new StringTokenizer(s,sep);
         Vector v = new Vector();
         while (tok.hasMoreTokens()) {
             v.add(tok.nextToken());
         }
         return v;
     }
     
    public static String replace(String org, String srch, String repl) {
        int pos;
        String s = org;
        if ((pos = org.indexOf(srch)) >= 0) {
            if (pos > 0) {
                if (pos + srch.length() < org.length()) {
                    s = org.substring(0, pos) + repl + org.substring(pos + srch.length(), org.length());
                } else {
                    s = org.substring(0, pos) + repl;
                }
            } else if (pos + srch.length() < org.length()) {
                s = repl + org.substring(pos + srch.length(), org.length());
            } else {
                s = repl;
            }
        }
        return s;
    }

    public static String replace(String org, String srch, String repl, boolean all) {
        if (org == null || srch == null || repl == null) {
            return null;
        }
        if (!all || (repl.length() > 0 && repl.indexOf(srch) >= 0)) {
            return replace(org, srch, repl);
        }
        String s = org;
        while (s.indexOf(srch) >= 0) {
            s = replace(s, srch, repl);
        }
        return s;
    }
    
    public static int countInString(String s, char search) {
        int count = 0;
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i) == search) {
                count++;
            }
        }
        return count;
    }

    public static int string2int(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch(Exception e) {
            return defaultValue;
        }
    }
    
    public static long string2long(String s, long defaultValue) {
        try {
            return Long.parseLong(s.trim());
        } catch(Exception e) {
            return defaultValue;
        }
    }
    
    public static double string2double(String s, double defaultValue) {
        try {
            return Double.parseDouble(s.trim());
        } catch(Exception e) {
            return defaultValue;
        }
    }
    
    public static float string2float(String s, float defaultValue) {
        try {
            return Float.parseFloat(s.trim());
        } catch(Exception e) {
            return defaultValue;
        }
    }
    
    public static double stringKMG2double(String s) {
        try {
            if (s.endsWith("K") || s.endsWith("k")) {
                return Double.parseDouble(s.substring(0, s.length() - 1)) * 1000.0;
            }
            if (s.endsWith("M") || s.endsWith("M")) {
                return Double.parseDouble(s.substring(0, s.length() - 1)) * 1000000.0;
            }
            if (s.endsWith("G") || s.endsWith("g")) {
                return Double.parseDouble(s.substring(0, s.length() - 1)) * 1000000000.0;
            }
            if (s.endsWith("T") || s.endsWith("t")) {
                return Double.parseDouble(s.substring(0, s.length() - 1)) * 1000000000000.0;
            }
            return Double.parseDouble(s);
        } catch(Exception e) {
        }
        return 0;
    }
    
    public static int searchArray(String[] array, String search) {
        return searchArray(array, search, -1);
    }
    
    public static int searchArray(String[] array, String search, int defaultValue) {
        for (int i=0; i<array.length; i++) {
            if (array[i] != null && array[i].equals(search)) {
                return i;
            }
        }
        return defaultValue;
    }
    
    public static String stringMaxLength(String s, int maxLength) {
        if (s.length() <= maxLength) {
            return s;
        } else {
            return s.substring(0, 30);
        }
    }
    
    private static String makeTimeString(int value, int chars) {
        String s = Integer.toString(value);
        while (s.length() < chars) {
            s = "0" + s;
        }
        return s;
    }
    
    public static String getTimeString(long ts) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(ts);
        return makeTimeString(cal.get(Calendar.DAY_OF_MONTH), 2) + "."
                + makeTimeString(cal.get(Calendar.MONTH) + 1, 2) + "."
                + makeTimeString(cal.get(Calendar.YEAR), 4) + " "
                + makeTimeString(cal.get(Calendar.HOUR_OF_DAY), 2) + ":"
                + makeTimeString(cal.get(Calendar.MINUTE), 2) + ":"
                + makeTimeString(cal.get(Calendar.SECOND), 2);
    }
    
    public static String breakLine(String s, int cols) {
        StringBuilder sb = new StringBuilder();
        while (s != null && s.length() > 0) {
            if (s.length() <= cols) {
                sb.append(s);
                s = null;
            } else {
                sb.append(s.substring(0, cols) + "\n");
                s = s.substring(cols);
            }
        }
        return sb.toString();
    }
    
    public static boolean isDirectory(String dir) {
    	return new File(dir).isDirectory();
    }
    
    public static boolean isFile(String fname) {
    	return new File(fname).isFile();
    }
    
    public static double parseDouble(String s, boolean base1024units) {
        try {
            return Double.parseDouble(s);
        } catch(Exception e) {
            try {
                Matcher m = pNumber.matcher(s.trim());
                if (m.matches()) {
                    double d = Double.parseDouble(m.group(1));
                    String unit = m.group(2).trim();
                    if (unit.length() == 0) {
                        return d;
                    }
                    if (unit.equalsIgnoreCase("k")) {
                        return d * (base1024units ? 1024.0: 1000.0);
                    }
                    if (unit.equalsIgnoreCase("m")) {
                        return d * (base1024units ? 1048576.0 : 1000000.0);
                    }
                    if (unit.equalsIgnoreCase("g")) {
                        return d * (base1024units ? 1073741824.0 : 1000000000.0);
                    }
                    if (unit.equalsIgnoreCase("t")) {
                        return d * (base1024units ? 1099511627776.0 : 1000000000000.0);
                    }
                }
                return Double.NaN;
            } catch(Exception e2) {
                return Double.NaN;
            }
        }
    }
    
}

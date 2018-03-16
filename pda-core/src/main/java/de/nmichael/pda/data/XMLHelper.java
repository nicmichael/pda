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

public class XMLHelper {
    
    private static int level = 0;

    public static String escapeXml(String str) {
        str = replaceString(str, "&", "&amp;");
        str = replaceString(str, "<", "&lt;");
        str = replaceString(str, ">", "&gt;");
        str = replaceString(str, "\"", "&quot;");
        str = replaceString(str, "'", "&apos;");
        return str;
    }

    public static String replaceString(String text, String repl, String with) {
        return replaceString(text, repl, with, -1);
    }

    public static String replaceString(String text, String repl, String with, int max) {
        if (text == null) {
            return null;
        }

        StringBuffer buffer = new StringBuffer(text.length());
        int start = 0;
        int end = 0;
        while ((end = text.indexOf(repl, start)) != -1) {
            buffer.append(text.substring(start, end)).append(with);
            start = end + repl.length();

            if (--max == 0) {
                break;
            }
        }
        buffer.append(text.substring(start));

        return buffer.toString();
    }

    public static String getString(String s, int length) {
        if (length == 0) {
            return "";
        }
        while (s.length() < length) {
            s = s + " ";
        }
        return s;
    }

    public static String xmlTagStart(String name) {
        return xmlTagStart(name, true);
    }

    public static String xmlTagStart(String name, boolean linefeed) {
        return getString(" ", 2 * level++) + "<" + name + ">" + (linefeed ? "\n" : "");
    }

    public static String xmlTagStart(String name, String[] paramNames, String[] paramValues) {
        StringBuffer s = new StringBuffer();
        s.append("<" + name);
        for (int i=0; i<paramNames.length; i++) {
            s.append(" " + escapeXml(paramNames[i]) + "=\"" + paramValues[i] + "\"");
        }
        s.append(">");
        return getString(" ", 2 * level++) + s.toString() + "\n";
    }

    public static String xmlTagEnd(String name) {
        return xmlTagEnd(name, true);
    }

    public static String xmlTagEnd(String name, boolean space) {
        --level;
        return (space ? getString(" ", 2 * level) : "") + "</" + name + ">" + "\n";
    }

    public static String xmlTag(String name, String text) {
        return xmlTagStart(name, false) + escapeXml(text) + xmlTagEnd(name, false);
    }
    
    public static void reset() {
        level = 0;
    }

}

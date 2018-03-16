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

import de.nmichael.pda.data.Parser;
import de.nmichael.pda.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Parsers {
    
    private static ArrayList<String> parsers = new ArrayList<String>();
    private static boolean sorted = true;
    
    public static Parser createParser(String className) {
        try {
            Object c = Class.forName(className).newInstance();
            return (c != null && c instanceof Parser ? (Parser)c : null);
        } catch (InstantiationException e) {
        	// ignore - this may be a non-parser class
        } catch (IllegalAccessException e) {
        	// ignore - this may be a non-parser class
        } catch (Exception e) {
            Logger.log(Logger.LogType.warning, "Error registering Parser " + className + ": " + e.toString());
            //e.printStackTrace();
        } catch (java.lang.Error e) {
            Logger.log(Logger.LogType.warning, "Error registering Parser " + className + ": " + e.toString());
        }
        return null;
    }
    
    private static void registerParser(String className) {
        Parser p = createParser(className);
        if (p != null) {
            parsers.add(className);
            sorted = false;
            Logger.log(Logger.LogType.info, "Found Parser: " + className);
        }
    }   
    
    private static void recursiveRead(String dirname, String pkgName) {
        try {
            File dir = new File(dirname);
            File[] files = dir.listFiles();
            for (File f : files) {
                String name = f.getName();
                if (f.isFile() && name.toLowerCase().endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6);
                    registerParser( (pkgName.length() == 0 ? className : pkgName + "." + className) );
                }
                if (f.isDirectory() && !name.equals(".") && !name.equals("..")) {
                    recursiveRead(dirname + Main.FILESEP + name, 
                            (pkgName.length() == 0 ? name : pkgName + "." + name));
                }
            }
        } catch (Exception e) {
            Logger.log(Logger.LogType.error, "Error registering Parsers: " + e.toString());    
            e.printStackTrace();
        }
    }
    
    public static void registerParsers() {
        try {
            String dirname = Main.PARSERDIR;
            if (!Util.isDirectory(dirname)) {
            	dirname = Main.PDADIR;
            }
            
            // first try to scan for .class files
            Logger.log(Logger.LogType.info, "Loading Parsers in " + dirname + " ...");
            File dir = new File(dirname);
            if (!dir.exists()) {
                Logger.log(Logger.LogType.error, "Parser Directory "+dirname+" not found.");
            } else {
                if (!dir.isDirectory()) {
                    Logger.log(Logger.LogType.error, "Parser Directory "+dirname+" is not a directory.");
                } else {
                    recursiveRead(dirname, "");
                }
            }
            
            // now scan for parser jar files
             dirname = Main.PDALIB;
            if (Util.isDirectory(dirname)) {
            	dir = new File(dirname);
                String[] files = dir.list();
                for (String f : files) {
                    if (f.endsWith(".jar") && f.indexOf("parsers") >= 0) {
                        registerJarParsers(dir + Main.FILESEP + f);
                    }
                }
            }
            
        } catch(Exception e) {
            Logger.log(Logger.LogType.error, "Error registering Parsers: " + e.toString());
            e.printStackTrace();
        }
    }
    
    private static void registerJarParsers(String jarname) {
        try {
            Logger.log(Logger.LogType.info, "Loading Parsers in " + jarname + " ...");
            JarFile jar = new JarFile(jarname);
            Enumeration<JarEntry> list = jar.entries();
            while (list.hasMoreElements()) {
                JarEntry entry = list.nextElement();
                if (entry.toString().endsWith(".class")) {
                    String className = entry.toString().substring(0, entry.toString().length() - 6).replaceAll("/", ".");
                    registerParser(className);
                }
            }
        } catch(Exception e) {
            Logger.log(Logger.LogType.error, "Error registering Parsers: " + e.toString());
            e.printStackTrace();
        }
    }
    
       
    public static int getNumberOfParsers() {
        if (!sorted) {
            String[] a = parsers.toArray(new String[0]);
            Arrays.sort(a);
            parsers = new ArrayList<String>();
            for (String s : a) {
                parsers.add(s);
            }
            sorted = true;
        }
        return (parsers == null ? 0 : parsers.size());
    }
    
    public static String getParserName(int i) {
        if (parsers == null || i < 0 || i >= parsers.size()) return null;
        return (String)parsers.get(i);
    } 
    
}

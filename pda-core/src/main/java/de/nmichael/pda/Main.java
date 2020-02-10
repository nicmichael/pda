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

import de.nmichael.pda.data.*;
import de.nmichael.pda.gui.*;
import de.nmichael.pda.util.*;
import java.awt.*;

public class Main {
    
    public static final String VERSIONID = "2.1.1_04";
    
    public static final String PROGRAM = "Performance Data Analyzer";
    public static final String PROGRAMSHORT = "PDA";
    public static final String VERSION = "Version " + VERSIONID;
    public static final String AUTHOR = "Nicolas Michael";
    public static final String HOMEPAGE = "http://pda.nmichael.de";
    public static final String EMAIL = "info@pda.nmichael.de";
    public static final String COPYRIGHT = "Copyright (c) 2006-20 by " + AUTHOR;
    public static final String LICENSE = "GNU General Public License v2";
    public static String FILESEP = System.getProperty("file.separator");
    public static String HOMEDIR = System.getProperty("user.home");
    public static String PDADIR = System.getProperty("user.dir");
    public static String PDALIB = PDADIR + FILESEP + "lib";
    public static String PARSERDIR = PDADIR + FILESEP + "parsers";
    public static ConfigFile config = null;
    public static boolean isGUI = true;
    
    private static String argPrjFile = null;
    private static String argDataFile = null;
    
    private static void startGUI() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        MainFrame frame = new MainFrame();
        frame.pack();
        frame.setSize((int)screenSize.getWidth(),(int)screenSize.getHeight()-30);
        frame.setLocation(0,0);
        frame.setVisible(true);

        if (argPrjFile != null) {
            frame.openProject(argPrjFile);
        }
        if (argDataFile != null) {
            Project prj = frame.createNewProject();
            String path = Util.getPathOfFile(argDataFile);
            prj.setFileName(path + Util.getFileSep() + "project.pda");
            Parser p = Parsers.getParserForFile(argDataFile);
            if (p != null) {
                prj.getProjectItem(0).addParser(p);
            }
        }
    }
    
    
    public static void showHelp() {
        System.out.println(PROGRAM);
        System.out.println(VERSION);
        System.out.println("");
        System.out.println(AUTHOR);
        System.out.println(COPYRIGHT);
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("");
        System.out.println("  Interactive GUI");
        System.out.println("    PDA [-v] [-Dprop=value] [-f file] [prjfile]");
        System.out.println("        -v        verbose");
        System.out.println("        -f file   file to open");
        System.out.println("        prjfile   project file to open");
        System.out.println("");
        System.out.println("  Graph Plotting");
        System.out.println("    PDA [-v] [-Dprop=value] -p -f files... -s series... [-x start] [-y end] [-o name] [-d dim]");
        System.out.println("        -v        verbose");
        System.out.println("        files...  files to plot (use 'file@parser' to specify parser class to use)");
        System.out.println("        series... series to plot (regular expressions matching series names)");
        System.out.println("        start     start timestamp (YYYY-MM-DD_HH:MM:SS)");
        System.out.println("        end       end timestamp (YYYY-MM-DD_HH:MM:SS)");
        System.out.println("        name      output file name");
        System.out.println("        dim       dimension of image (default: 1024x768)");
        System.out.println("");
        System.out.println("  Reporting");
        System.out.println("    PDA [-v] [-Dprop=value] -r -f files... -s series... [-x start] [-y end] [-o name]");
        System.out.println("        -v        verbose");
        System.out.println("        files...  files to plot (use 'file@parser' to specify parser class to use)");
        System.out.println("        series... series to plot (regular expressions matching series names)");
        System.out.println("        start     start timestamp (YYYY-MM-DD_HH:MM:SS)");
        System.out.println("        end       end timestamp (YYYY-MM-DD_HH:MM:SS)");
        System.out.println("        name      output file name");
        System.out.println("");
        System.out.println("  Correlation");
        System.out.println("    PDA [-v] [-Dprop=value] -c -f files... -s series... [-x start] [-y end] [-z] [-i] [-t tolerance] [-o name]");
        System.out.println("        -v        verbose");
        System.out.println("        files...  files to analyze (use 'file@parser' to specify parser class to use)");
        System.out.println("        series... series to correlate (regular expressions matching series names)");
        System.out.println("        start     start timestamp (YYYY-MM-DD_HH:MM:SS)");
        System.out.println("        end       end timestamp (YYYY-MM-DD_HH:MM:SS)");
        System.out.println("        -z        eliminate rows with partially missing samples");
        System.out.println("        -i        interpolate missing samples");
        System.out.println("        tolerance tolerance (in ms) to merge samples at different data points");
        System.out.println("        name      output file name");
        System.out.println("");
        System.out.println("  File Conversion");
        System.out.println("    PDA [-v] [-Dprop=value] -t converterclass -f files...");
        System.out.println("        -v        verbose");
        System.out.println("        files...  files to convert (use 'file@parser' to specify parser class to use)");
        System.out.println("");
        System.out.println("  CSV Merge");
        System.out.println("    PDA [-v] [-Dprop=value] -m files...");
        System.out.println("        -v        verbose");
        System.out.println("        files...  CSV files to merge");
        System.out.println("");
        System.out.println("  CSV Grep");
        System.out.println("    PDA [-v] [-Dprop=value] -g pattern files...");
        System.out.println("        -v        verbose");
        System.out.println("        pattern   Regex of series names to grep for");
        System.out.println("        files...  CSV files to grep from");
    }
    
    private static void getCommandLineArgs(String[] args) {
        for (int i=0; i<args.length; i++) {
            if (args[i].equals("-v")) {
                Logger.setDebugLogging(true);
                continue;
            }
            if (args[i].startsWith("-h")) {
                showHelp();
                System.exit(1);
            }
            if (args[i].startsWith("-D") && args[i].indexOf("=") > 2) {
                String[] kv = args[i].substring(2).split("=");
                if (kv != null && kv.length == 2) {
                    System.setProperty(kv[0], kv[1]);
                } else {
                    showHelp();
                    System.exit(1);
                }
                continue;
            }
            CLI cli = null;
            if (args[i].equals("-p")) {
                cli = new Plot(args, i+1);
            }
            if (args[i].equals("-r")) {
                cli = new Reporter(args, i+1);
            }
            if (args[i].equals("-c")) {
                cli = new Correlation(args, i+1);
            }
            if (cli != null) {
                System.exit(cli.process() ? 0 : 1);
            }
            if (args[i].equals("-t")) {
                System.exit(Convert.convertFiles(args, i+1));
            }
            if (args[i].equals("-m")) {
                System.exit(CsvMerger.merge(args, i+1));
            }
            if (args[i].equals("-g")) {
                System.exit(CsvGrep.grep(args, i+1));
            }
            if (args[i].equals("-f") && i + 1 < args.length) {
                argDataFile = args[++i];
                continue;
            }
            if (i == args.length-1 || argPrjFile != null) {
                if (args[i].startsWith("-")) {
                    showHelp();
                    System.exit(1);
                }
                argPrjFile = args[i];
            }
        }
    }

    private static void iniAppl() {
        System.setErr(new ErrorPrintStream());
        if (!HOMEDIR.endsWith(FILESEP)) {
            HOMEDIR += FILESEP;
        }
        if (!PDADIR.endsWith(FILESEP)) {
            PDADIR += FILESEP;
        }
        if (System.getProperty("pda.home") != null) {
            PDADIR = System.getProperty("pda.home");
        }
        if (System.getProperty("pda.parsers") != null) {
            PARSERDIR = System.getProperty("pda.parsers");
        }
        if (System.getProperty("pda.lib") != null) {
            PDALIB = System.getProperty("pda.lib");
        }
        Logger.log(Logger.LogType.info, PROGRAM+" - "+VERSION);
        Logger.log(Logger.LogType.info, COPYRIGHT);
        Parsers.registerParsers();
    }
    
    public static void exit() {
        try {
            config.writeConfig();
        } catch(Exception ee) {
            ee.printStackTrace();
        }
        System.exit(0);
    }
    
    public static void main(String[] args) {
        try {
            iniAppl();
            config = new ConfigFile(null);
            try {
                config.readConfig();
            } catch (Exception e) {
            }
            
            getCommandLineArgs(args);
            
            startGUI();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Error er) {
            er.printStackTrace();
            System.exit(1);
        }
    }
    
}
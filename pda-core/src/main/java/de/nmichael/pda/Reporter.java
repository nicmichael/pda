package de.nmichael.pda;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;

import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.DataSeriesProperties;
import de.nmichael.pda.data.Parser;
import de.nmichael.pda.data.Project;
import de.nmichael.pda.data.ProjectFile;
import de.nmichael.pda.data.ProjectItem;
import de.nmichael.pda.data.TimeStamp;
import de.nmichael.pda.gui.GraphPanel;
import de.nmichael.pda.util.SeriesStatistics;
import de.nmichael.pda.util.Util;

public class Reporter {
    
    private String[] files;
    private String[] series;
    private long start;
    private long stop;
    private int seriesCount = 0;
    
    public Reporter(String[] files, String[] series, long start, long stop) {
        this.files = files;
        this.series = series;
        this.start = start;
        this.stop = stop;
    }
    
    private int createReport() {
        Project prj = new Project();
        prj.setFileName("report.pda");
        ProjectItem item = new ProjectItem(ProjectItem.Type.graph);
        prj.addProjectItem(item);
        
        for (String file : files) {
            String fname = file;
            String pname = null;
            if (fname.indexOf("@") > 0) {
                int pos = fname.indexOf("@");
                fname = file.substring(0, pos);
                pname = file.substring(pos+1);
            }
            if (!(new File(fname)).exists()) {
                Logger.log(Logger.LogType.error, "File '" + fname + "' not found.");
                continue;
            }
            if (pname == null || pname.length() == 0) {
                for (int pi=0; pi<Parsers.getNumberOfParsers(); pi++) {
                    try {
                        Parser p = (Parser)Class.forName(Parsers.getParserName(pi)).newInstance();
                        if (p.canHandle(fname)) {
                            pname = Parsers.getParserName(pi);
                            break;
                        }
                    } catch(Exception e) {
                    }
                }
            }
            if (pname == null || pname.length() == 0) {
                Logger.log(Logger.LogType.error, "No parser found or configured for file: " + fname);
                continue;
            }
            try {
                Logger.log(Logger.LogType.info, "Using parser '" + pname + "' for file '" + fname + "' ...");
                Parser p = Parsers.createParser(pname);
                if (p == null) {
                    continue;
                }
                p.setFilename(fname);
                p.getAllSeriesNames(true);
                item.addParser(p);
            } catch (Exception e) {
                Logger.log(Logger.LogType.error, "Could not instantiate parser '" + pname +"' for file: " + fname + ": " + e.toString());
            }
        }
        
        for (String sname : series) {
            String pat = sname;
            if (!pat.startsWith("^")) {
                pat = ".*" + pat;
            }
            if (!pat.endsWith("$")) {
                pat = pat + ".*";
            }
            Logger.log(Logger.LogType.info, "-> Adding series for pattern '" + pat + "' ...");
            Pattern spat = Pattern.compile(pat);
            for (int pi=0; pi<item.getParsers().size(); pi++) {
                Parser p = item.getParsers().getParser(pi);
                String[] series = p.series().getSeriesNames(spat);
                for (int si=0; series != null && si < series.length; si++) {
                    DataSeries ds = p.series().getGlobalSeries(series[si]);
                    if (ds != null) {
                        Logger.log(Logger.LogType.info, "   Found series: " + ds.getLocalName());
                        ds.setUsed(true);
                        ds.setSelected(true);
                        DataSeriesProperties prop = new DataSeriesProperties(ds);
                        prop.setColor(item.getColorSelector().getNextColor());
                        ds.setDataProperties(prop);
                        item.getSeriesProperties().addDataProperties(prop);
                        seriesCount++;
                    }
                }
            }
        }
        
        if (item.getParsers().size() == 0) {
            Logger.log(Logger.LogType.error, "No files specified or found.");
            return 1;
        }
        
        if (start > 0) {
            item.setScaleMinX(start);
            Logger.log(Logger.LogType.info, "Using start timestamp: " + Util.getTimeString(start));
        }
        if (stop < Long.MAX_VALUE) {
            item.setScaleMaxX(stop);
            Logger.log(Logger.LogType.info, "Using end timestamp: " + Util.getTimeString(stop));
        }

        if (seriesCount > 0) {
            try {
                Logger.log(Logger.LogType.info, "Parsing all data ...");
                for (int j = 0; j < item.getParsers().size(); j++) {
                    Parser p = item.getParsers().getParser(j);
                    p.parse(false, true);
                }
                
                for (int i=0; i<item.getSeriesProperties().size(); i++) {
                    DataSeriesProperties prop = item.getSeriesProperties().getDataProperties(i);
                    DataSeries s = prop.getSeries();
                    prop.setStyle(s.getPreferredStyle());
                }

                Logger.log(Logger.LogType.info, "Writing report ...");
                ProjectFile pf = new ProjectFile(prj);
                pf.saveToFile();
                SeriesStatistics stats = new SeriesStatistics(item, (DataSeriesProperties)null);
                BufferedWriter f = new BufferedWriter(new FileWriter("report.txt"));
                f.write(stats.getStats());
                f.close();
                Logger.log(Logger.LogType.info, "Done.");
            } catch (Exception e) {
                Logger.log(Logger.LogType.error, "Failed to parse or plot data: " + e);
                e.printStackTrace();
                return 1;
            }
        } else {
            Logger.log(Logger.LogType.error, "No series found.");
            return 1;
        }

        return 0;
    }

    
    private static int usage(String error) {
        if (error != null) {
            System.out.println(error);
        }
        System.out.println("usage: Reporter -f [file1 file2...] -s [series1 series2...] [-x starttime] [-y stoptime]");
        return 1;
    }
    
    public static int report(String[] args, int i) {
        Main.isGUI = false;
        if (args.length <= i) {
            return usage(null);
        }
        ArrayList<String> files = new ArrayList<String>(); 
        ArrayList<String> series = new ArrayList<String>();
        long start = 0;
        long stop = Long.MAX_VALUE;
        final int ARG_FILES = 1;
        final int ARG_SERIES = 2;
        final int ARG_START = 3;
        final int ARG_STOP = 4;
        int argType = 0;
        for (; i<args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (arg.equalsIgnoreCase("-f")) {
                    argType = ARG_FILES;
                } else if (arg.equalsIgnoreCase("-s")) {
                    argType = ARG_SERIES;
                } else if (arg.equalsIgnoreCase("-x")) {
                    argType = ARG_START;
                } else if (arg.equalsIgnoreCase("-y")) {
                    argType = ARG_STOP;
                } else {
                    return usage("Invalid argument: " + arg);
                }
            } else {
                switch(argType) {
                case ARG_FILES:
                    files.add(arg);
                    break;
                case ARG_SERIES:
                    series.add(arg);
                    break;
                case ARG_START:
                    TimeStamp tstart = new TimeStamp(0, 0, 0);
                    tstart.getTimeStampFromLine(arg, null, 0, false);
                    start = tstart.getTimeStamp();
                    Logger.log(Logger.LogType.info, "Start Time: " + Util.getTimeString(start) + " (" + arg + ")");
                    break;
                case ARG_STOP:
                    TimeStamp tstop = new TimeStamp(0, 0, 0);
                    tstop.getTimeStampFromLine(arg, null, 0, false);
                    stop = tstop.getTimeStamp();
                    Logger.log(Logger.LogType.info, "Stop Time: " + Util.getTimeString(stop) + " (" + arg + ")");
                    break;
                default:
                    return usage("Invalid argument: " + arg);
                }
            }
        }
        if (files.size() == 0) {
            return usage("No files specified.");
        }
        if (series.size() == 0) {
            return usage("No series specified.");
        }
        Reporter r = new Reporter(files.toArray(new String[0]), series.toArray(new String[0]), start, stop);
        return r.createReport();
    }
    
}

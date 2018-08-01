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

public abstract class CLI {
    
    private String[] files;
    private String[] series;
    private long start;
    private long stop;
    private String output;
    private int seriesCount = 0;
    protected Project prj;
    protected ProjectItem item;
    
    public CLI(String[] args, int i) {
        Main.isGUI = false;
        if (args.length <= i) {
            System.exit(usage(null));
        }
        ArrayList<String> files = new ArrayList<String>(); 
        ArrayList<String> series = new ArrayList<String>();
        long start = 0;
        long stop = Long.MAX_VALUE;
        String output = null;
        final int ARG_OTHER = 0;
        final int ARG_FILES = 1;
        final int ARG_SERIES = 2;
        final int ARG_START = 3;
        final int ARG_STOP = 4;
        final int ARG_OUTPUT = 5;
        int argType = ARG_OTHER;
        for (; i<args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (arg.equalsIgnoreCase("-f")) {
                    argType = ARG_FILES;
                    args[i] = null;
                } else if (arg.equalsIgnoreCase("-s")) {
                    argType = ARG_SERIES;
                    args[i] = null;
                } else if (arg.equalsIgnoreCase("-x")) {
                    argType = ARG_START;
                    args[i] = null;
                } else if (arg.equalsIgnoreCase("-y")) {
                    argType = ARG_STOP;
                    args[i] = null;
                } else if (arg.equalsIgnoreCase("-o")) {
                    argType = ARG_OUTPUT;
                    args[i] = null;
                } else {
                	argType = ARG_OTHER;
                }
            } else {
                switch(argType) {
                case ARG_FILES:
                    files.add(arg);
                    args[i] = null;
                    break;
                case ARG_SERIES:
                    series.add(arg);
                    args[i] = null;
                    break;
                case ARG_START:
                    TimeStamp tstart = new TimeStamp(0, 0, 0);
                    tstart.getTimeStampFromLine(arg, null, 0, false);
                    start = tstart.getTimeStamp();
                    Logger.log(Logger.LogType.info, "Start Time: " + Util.getTimeString(start) + " (" + arg + ")");
                    args[i] = null;
                    argType = ARG_OTHER;
                    break;
                case ARG_STOP:
                    TimeStamp tstop = new TimeStamp(0, 0, 0);
                    tstop.getTimeStampFromLine(arg, null, 0, false);
                    stop = tstop.getTimeStamp();
                    Logger.log(Logger.LogType.info, "Stop Time: " + Util.getTimeString(stop) + " (" + arg + ")");
                    args[i] = null;
                    argType = ARG_OTHER;
                    break;
                case ARG_OUTPUT:
                    output = arg;
                    args[i] = null;
                    argType = ARG_OTHER;
                    break;
                }
            }
        }
        if (files.size() == 0) {
        	System.exit(usage("No files specified."));
        }
        if (series.size() == 0) {
        	System.exit(usage("No series specified."));
        }
        this.files = files.toArray(new String[0]);
        this.series = series.toArray(new String[0]);
        this.start = start;
        this.stop = stop;
        this.output = output;
    }
    
    protected void checkArgs(String[] args, int i) {
        for (; i<args.length; i++) {
            if (args[i] != null) {
            	System.exit(usage("Unknown argument: " + args[i]));
            }
        }
    }

    public boolean process() {
        prj = new Project();
        prj.setFileName("report.pda");
        item = new ProjectItem(ProjectItem.Type.graph);
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
            return false;
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
                
                run();

            } catch (Exception e) {
                Logger.log(Logger.LogType.error, "Failed to parse or plot data: " + e);
                e.printStackTrace();
                return false;
            }
        } else {
            Logger.log(Logger.LogType.error, "No series found.");
            return false;
        }

        return true;
    }
    
    public abstract boolean run() throws Exception; /*
    {
    	try {
			Logger.log(Logger.LogType.info, "Writing report ...");
			ProjectFile pf = new ProjectFile(prj);
			pf.saveToFile();
			SeriesStatistics stats = new SeriesStatistics(item, (DataSeriesProperties) null);
			BufferedWriter f = new BufferedWriter(new FileWriter("report.txt"));
			f.write(stats.getStats());
			f.close();
			Logger.log(Logger.LogType.info, "Done.");
			return true;
    	} catch(Exception e) {
    		Logger.log(Logger.LogType.error, e.toString());
    		e.printStackTrace();
    		return false;
    	}
    }
    */
    
    protected int getSeriesCount() {
    	return seriesCount;
    }
    
    protected String getOutputName() {
    	return output;
    }

    protected static int usage(String error) {
        if (error != null) {
            System.out.println(error);
        }
        Main.showHelp();
        return 1;
    }
        
}

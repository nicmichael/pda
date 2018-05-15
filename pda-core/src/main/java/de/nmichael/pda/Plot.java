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

import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.DataSeriesProperties;
import de.nmichael.pda.data.Parser;
import de.nmichael.pda.data.Project;
import de.nmichael.pda.data.ProjectFile;
import de.nmichael.pda.data.ProjectItem;
import de.nmichael.pda.gui.GraphPanel;
import de.nmichael.pda.util.Util;
import java.io.File;
import java.util.regex.Pattern;

public class Plot {
    
    public static int plotFiles(String[] args, int i) {
        Main.isGUI = false;
        Project prj = new Project();
        ProjectItem item = new ProjectItem(ProjectItem.Type.graph);
        prj.addProjectItem(item);
        String pngname = null;
        int width = Project.PNG_WIDTH;
        int height = Project.PNG_HEIGHT;
        int mode = 0; // 1 - files; 2 - series
        int seriesCount = 0;
        for (; i<args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-f")) {
                    mode = 1;
                } else if (args[i].equals("-s")) {
                    mode = 2;
                } else {
                    mode = 0;
                }
                continue;
            }
            if (mode == 0) {
                if (pngname == null) {
                    pngname = args[i];
                    String prjname = pngname;
                    if (prjname.endsWith(".png")) {
                        prjname = prjname.substring(0, prjname.length() - 3) + "pda";
                    }
                    prj.setFileName(prjname);
                } else {
                    int pos = args[i].toLowerCase().indexOf("x");
                    if (pos > 0 && pos<args[i].length()-1) {
                        width = Util.string2int(args[i].substring(0, pos), width);
                        height = Util.string2int(args[i].substring(pos+1), height);
                    }
                }
            }
            if (mode == 1) { // files
                String fname = args[i];
                String pname = null;
                if (fname.indexOf("@") > 0) {
                    int pos = fname.indexOf("@");
                    fname = args[i].substring(0, pos);
                    pname = args[i].substring(pos+1);
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
            if (mode == 2) { // series
                String pat = args[i];
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
        }
        
        if (item.getParsers().size() == 0) {
            Logger.log(Logger.LogType.error, "No files specified or found.");
            return 1;
        }
        
        if (seriesCount > 0) {
            try {
                Logger.log(Logger.LogType.info, "Parsing all data ...");
                for (int j = 0; j < item.getParsers().size(); j++) {
                    Parser p = item.getParsers().getParser(j);
                    p.parse(false, true);
                }

                Logger.log(Logger.LogType.info, "Plotting " + seriesCount + " series ...");
                item.setPngFilename(pngname);
                GraphPanel graph = new GraphPanel(null, item, width, height);
                graph.updateGraphPanel();
                graph.doLayout();
                Logger.log(Logger.LogType.info, "Writing image " + pngname + " [" + width + "x" + height + "] ...");
                graph.saveImageToFile(pngname);
                ProjectFile pf = new ProjectFile(prj);
                pf.saveToFile();
            } catch (Exception e) {
                Logger.log(Logger.LogType.error, "Failed to parse or plot data: " + e);
                e.printStackTrace();
                return 1;
            }
        } else {
            Logger.log(Logger.LogType.error, "No series found.");
            ProjectFile pf = new ProjectFile(prj);
            pf.saveToFile();
            return 1;
        }
        return 0;
    }
    
}

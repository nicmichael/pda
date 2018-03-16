/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2015 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda;

import de.nmichael.pda.data.Converter;
import de.nmichael.pda.data.Parser;
import de.nmichael.pda.data.Project;
import de.nmichael.pda.data.ProjectItem;
import java.io.File;

public class Convert {
    
    public static int convertFiles(String[] args, int i) {
        Main.isGUI = false;
        Project prj = new Project();
        ProjectItem item = new ProjectItem(ProjectItem.Type.graph);
        Converter converter = null;
        boolean inFiles = false;
        for (; i<args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-f")) {
                    inFiles = true;
                }
                continue;
            }
            if (!inFiles) { // files
                String converterClass = args[i];
                try {
                    converter = (Converter) Class.forName(converterClass).newInstance();
                } catch (Exception e) {
                    Logger.log(Logger.LogType.error, "Could not instantiate converter class '" + converterClass + "': " + e.toString());
                    return 1;
                }
            } else {
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
        }
        
        if (converter == null) {
            Logger.log(Logger.LogType.error, "No converter class specified.");
            return 1;
        }
        
        if (item.getParsers().size() == 0) {
            Logger.log(Logger.LogType.error, "No files specified or found.");
            return 1;
        }
        
        try {
            Logger.log(Logger.LogType.info, "Parsing all data ...");
            for (int j = 0; j < item.getParsers().size(); j++) {
                Parser p = item.getParsers().getParser(j);
                Logger.log(Logger.LogType.info, "Parsing " + p.getFilename() + " ...");
                p.setAllUsed();
                p.parse(false, false);
                if (p.series() != null && p.series().size() > 0) {
                    Logger.log(Logger.LogType.info, "Converting " + p.getFilename() + " ...");
                    try {
                        if (!converter.convert(p.getFilename(), p.getName(), p.series())) {
                            Logger.log(Logger.LogType.error, "Failed to convert " + p.getFilename() + ".");
                        }
                    } catch (Exception e) {
                        Logger.log(Logger.LogType.error, "Failed to convert " + p.getFilename() + ".");
                        e.printStackTrace();
                    }
                } else {
                    Logger.log(Logger.LogType.warning, "No series found in " + p.getFilename() + ".");
                }
            }
        } catch (Exception e) {
            Logger.log(Logger.LogType.error, "Failed to parse or plot data: " + e);
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
    
}
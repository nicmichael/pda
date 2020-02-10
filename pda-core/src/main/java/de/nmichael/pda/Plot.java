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
import de.nmichael.pda.data.TimeStamp;
import de.nmichael.pda.gui.GraphPanel;
import de.nmichael.pda.util.Util;
import java.io.File;
import java.util.regex.Pattern;

public class Plot extends CLI {

    private int width = Project.PNG_WIDTH;
    private int height = Project.PNG_HEIGHT;


    public Plot(String[] args, int i) {
        super(args, i);
        for (; i<args.length; i++) {
            if (args[i] != null && args[i].equals("-d")) {
                args[i] = null;
                if (i+1 < args.length && args[i+1] != null) {
                    String arg = args[i+1];
                    int pos = arg.toLowerCase().indexOf("x");
                    if (pos > 0 && pos<arg.length()-1 && width == Project.PNG_WIDTH && height == Project.PNG_HEIGHT) {
                        width = Util.string2int(arg.substring(0, pos), width);
                        height = Util.string2int(arg.substring(pos+1), height);
                        args[i] = null;
                    } else {
                        System.exit(usage("Unknown argument: " + args[i]));
                    }
                    args[++i] = null;
                }
            }
        }
        checkArgs(args, i);
    }

    @Override
    public boolean run() throws Exception {
	Logger.log(Logger.LogType.info, "Plotting " + getSeriesCount() + " series ...");
        String pngname = getOutputName() != null ? getOutputName() : "plot.png";
        if (pngname.indexOf(".") < 0) {
            pngname = pngname + ".png";
        }
	item.setPngFilename(pngname);
        setProjectNameFromOutputName(pngname);
        GraphPanel graph = new GraphPanel(null, item, width, height);
	graph.updateGraphPanel();
	graph.doLayout();
	Logger.log(Logger.LogType.info, "Writing image " + pngname + " [" + width + "x" + height + "] ...");
	graph.saveImageToFile(pngname);
	ProjectFile pf = new ProjectFile(prj);
	pf.saveToFile();
	return true;
    }

}

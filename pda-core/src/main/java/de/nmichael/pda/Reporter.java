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

public class Reporter extends CLI {
    
    public Reporter(String[] args, int i) {
        super(args, i);
        checkArgs(args, i);
    }

    @Override
    public boolean run() throws Exception {
        String report = getOutputName() != null ? getOutputName() : "report.txt";
        if (report.indexOf(".") < 0) {
            report = report + ".txt";
        }
        setProjectNameFromOutputName(report);
        Logger.log(Logger.LogType.info, "Writing report " + report + " ...");
        ProjectFile pf = new ProjectFile(prj);
        pf.saveToFile();
        SeriesStatistics stats = new SeriesStatistics(item, (DataSeriesProperties)null);
        BufferedWriter f = new BufferedWriter(new FileWriter(report));
        f.write(stats.getStats());
        f.close();
        Logger.log(Logger.LogType.info, "Done.");
        return true;
    }
    
}

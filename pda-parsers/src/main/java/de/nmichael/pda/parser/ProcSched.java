package de.nmichael.pda.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.FileFormatDescription;
import de.nmichael.pda.data.Parser;
import de.nmichael.pda.data.Sample;

public class ProcSched extends Parser {
	
	Pattern pProcess = Pattern.compile("^([^ ]+) \\(([0-9]+), #threads: .*");
	Pattern pStat = Pattern.compile("^([^ ]+) +: +([0-9\\.]+)");

	@Override
	public boolean canHandle(String filename) {
		return Parser.canHandle(filename, "sched");
	}

    public ProcSched() {
        super("sched");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_LINUX,
                null,
                "/proc/PID/sched",
                null,
                "Linux Process Scheduling Statistics",
                null));
    }
    
	@Override
	public void createAllSeries() {
		parse();
	}

	@Override
	public void parse() {
		//for (DataSeries s : series().getSelectedSeries()) {
		for (DataSeries s : series().getAllSeries()) {
			if (s.isUsed() && s.getSeriesName().equals("avg_runtime")) {
				DataSeries s2;
				if ((s2 = series().getSeries(s.getCategoryName(), s.getSubcategoryName(), "nr_switches")) != null) {
					s2.setUsed(true);
				}
				if ((s2 = series().getSeries(s.getCategoryName(), s.getSubcategoryName(), "se.sum_exec_runtime")) != null) {
					s2.setUsed(true);
				}
			}
		}
		try {
			String s;
			String name = null;
			String pid = null;
            while ((s = readLine()) != null) {
            	Matcher m;
            	if ((m = pStat.matcher(s)).matches()) {
            		if (name != null && pid != null) {
            			series().getOrAddSeries(name, pid, m.group(1)).addSampleIfNeeded(getCurrentTimeStamp().getTimeStamp(), Double.parseDouble(m.group(2)));
            		}
            	} else if ((m = pProcess.matcher(s)).matches()) {
            		name = m.group(1);
            		pid = m.group(2);
            	}
            }
            for (DataSeries series : series().getAllSeries()) {
                if (!series.isUsed()) {
                    continue;
                }
                if (series.isMonotonic()) {
                	series.convertToMonotonic(true);
                }
            }
            for (DataSeries series : series().getAllSeries()) {
            	if (series.getSeriesName().equals("nr_switches")) {
            		DataSeries runTime = series().getSeries(series.getCategoryName(), series.getSubcategoryName(), "se.sum_exec_runtime");
            		if (runTime != null) {
            			DataSeries avgRuntime = series().getOrAddSeries(series.getCategoryName(), series.getSubcategoryName(), "avg_runtime");
            			if (avgRuntime.isUsed() && avgRuntime.getNumberOfSamples() == 0) {
            				for (int i=0; i<series.getNumberOfSamples() && i<runTime.getNumberOfSamples(); i++) {
            					Sample sw = series.getSample(i);
            					Sample rt = runTime.getSample(i);
            					if (sw.getTimeStamp() == rt.getTimeStamp()) {
            					    avgRuntime.addSample(new Sample(sw.getTimeStamp(), sw.getValue() > 0 ? rt.getValue() / sw.getValue() : 0));
            					}
            				}
            			}
            		}
            	}
            }
		} catch(Exception e) {
			super.logError("Error parsing scheduling logfile", e);
		}
	}

}

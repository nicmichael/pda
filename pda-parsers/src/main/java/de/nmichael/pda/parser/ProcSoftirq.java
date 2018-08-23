package de.nmichael.pda.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.FileFormatDescription;
import de.nmichael.pda.data.Parser;
import de.nmichael.pda.data.Sample;
import de.nmichael.pda.util.Util;

public class ProcSoftirq extends Parser {
	
	Pattern pCpus = Pattern.compile(" *CPU.*");
	Pattern pStat = Pattern.compile(" *([^ ]+):(.+)");

	@Override
	public boolean canHandle(String filename) {
		return Parser.canHandle(filename, "softirq") || Parser.canHandle(filename, "irqstats");
	}

    public ProcSoftirq() {
        super("softirq");
        setSupportedFileFormat(new FileFormatDescription(
                FileFormatDescription.PRODUCT_LINUX,
                null,
                "/proc/softirq",
                null,
                "Linux Softirq Statistics",
                null));
    }
    
	@Override
	public void createAllSeries() {
		parse();
	}

	@Override
	public void parse() {
		try {
			String s;
			String[] cpus = null;
            while ((s = readLine()) != null) {
            	Matcher m;
            	if ((m = pStat.matcher(s)).matches()) {
            		String name = m.group(1);
            		String[] values = m.group(2).trim().split(" +");
            		long sum = 0;
            		for (String value : values) {
            			sum += Util.string2long(value, 0);
            		}
          			series().getOrAddSeries("softirq", name, "total").addSampleIfNeeded(getCurrentTimeStamp().getTimeStamp(), sum);
          			if (cpus != null) {
          				for (int i=0; i<cpus.length && i<values.length; i++) {
          					series().getOrAddSeries("softirq", name, cpus[i]).addSampleIfNeeded(getCurrentTimeStamp().getTimeStamp(), Util.string2long(values[i], 0));
          				}
          			}
            	} else if ((m = pCpus.matcher(s)).matches()) {
            		cpus = s.trim().split(" +");
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
		} catch(Exception e) {
			super.logError("Error parsing softirq logfile", e);
		}
	}

}

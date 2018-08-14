package de.nmichael.pda.util;

import java.util.Arrays;
import java.util.Vector;

import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.DataSeriesProperties;
import de.nmichael.pda.data.ProjectItem;
import de.nmichael.pda.data.Sample;
import de.nmichael.pda.data.StatisticsData;

public class SeriesStatistics {

	enum SortOrder {
		unsorted,
		name,
		value
	}

	SortOrder sorting = SortOrder.unsorted;

	class StatSummary implements Comparable {
		String name;
		double value;

		public StatSummary(String name, double value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public int compareTo(Object o) {
			if (sorting == SortOrder.name) {
				return name.compareTo(((StatSummary)o).name);
			}
			if (sorting == SortOrder.value) {
				double v = value - ((StatSummary)o).value;
				return v < 0 ? 1 : v > 0 ? -1 : 0;
			}
			return 0;
		}
	}

    private ProjectItem projectItem;
    private Vector<DataSeriesProperties> vprop;
    private StringBuilder data;
    
    public SeriesStatistics(ProjectItem projectItem, Vector<DataSeriesProperties> vprop) {
        if (vprop == null) {
            vprop = new Vector<DataSeriesProperties>();
            for (int i=0; i<projectItem.getSeriesProperties().size(); i++) {
                vprop.add(projectItem.getSeriesProperties().getDataProperties(i));
            }
        }
        this.projectItem = projectItem;
        this.vprop = vprop;
    }

    public SeriesStatistics(ProjectItem projectItem, DataSeriesProperties prop) {
        Vector<DataSeriesProperties> vprop = new Vector<DataSeriesProperties>();
        if (prop != null) {
            vprop.add(prop);
        } else {
            for (int i=0; i<projectItem.getSeriesProperties().size(); i++) {
                vprop.add(projectItem.getSeriesProperties().getDataProperties(i));
            }
        }
        this.projectItem = projectItem;
        this.vprop = vprop;
    }

    public String getStats() {
        if ("value".equalsIgnoreCase(System.getProperty("stats.sort"))) {
            sorting = SortOrder.value;
        }
        if ("name".equalsIgnoreCase(System.getProperty("stats.sort"))) {
            sorting = SortOrder.name;
        }

        data = new StringBuilder();
        if (vprop.size() == 0) {
            addLine("No Series Data.");
            return data.toString();
        }
        initHeaderData();

        StatSummary[] summary = new StatSummary[vprop.size()];
        for (int i = 0; i < vprop.size(); i++) {
            summary[i] = getOneLineSummary(vprop.get(i));
        }
        Arrays.sort(summary);
        for (int i = 0; i < summary.length; i++) {
            addLine(String.format("%-80s: %s", summary[i].name, Util.double2string(summary[i].value, 19, 3, false)));
        }

        for (int i = 0; i < vprop.size(); i++) {
            initData(vprop.get(i));
        }
        return data.toString();
    }
    
    private void addLine(String s) {
        data.append(s+"\n");
    }

    private void addLine(int level, String s) {
        String space = "";
        while (space.length() < level*5) space += " ";
        data.append(space+s+"\n");
    }
    
    private void initHeaderData() {
        long xFrom = projectItem.getScaleMinX();
        long xTo = projectItem.getScaleMaxX();
        
        addLine("Selected Time Interval");
        addLine("----------------------------------------------------------------");
        addLine("Start of Interval : " + (xFrom == Long.MIN_VALUE ? "" : Sample.createTimeStampString(xFrom)));
        addLine("End of Interval   : " + (xTo == Long.MAX_VALUE ? "" : Sample.createTimeStampString(xTo)));
        addLine("Time elapsed      : " + ((xTo - xFrom) / 1000) + " sec");
        addLine("");
    }
    
    private StatSummary getOneLineSummary(DataSeriesProperties prop) {
        long xFrom = Long.MIN_VALUE;
        long xTo = Long.MAX_VALUE;
        if (projectItem.isScaleXSet()) {
            xFrom = projectItem.getScaleMinX();
            xTo = projectItem.getScaleMaxX();
        }

        DataSeries series = prop.getSeries();
        StatisticsData stat = (series != null ? series.getStatisticsData(xFrom, xTo, prop.getSmooth()) : null);
        StatSummary summary = new StatSummary(prop.getDisplayName(),
                stat != null && stat.samplesCnt > 0 ? stat.valuesSum/stat.samplesCnt : 0);
        return summary;
    }

    private void initData(DataSeriesProperties prop) {
        long xFrom = Long.MIN_VALUE;
        long xTo = Long.MAX_VALUE;
        if (projectItem.isScaleXSet()) {
            xFrom = projectItem.getScaleMinX();
            xTo = projectItem.getScaleMaxX();
        }

        DataSeries series = prop.getSeries();
        StatisticsData stat = (series != null ? series.getStatisticsData(xFrom, xTo, prop.getSmooth()) : null);
        
        addLine("");
        addLine("");
        addLine("Series Statistics for Series: "+prop.getName());
        addLine("================================================================");
        addLine("");
        if (stat != null && stat.samplesCnt > 0) {
            addLine("Number of samples : "+stat.samplesCnt);
            addLine("First sample      : "+Sample.createTimeStampString(stat.firstSample));
            addLine("Last sample       : "+Sample.createTimeStampString(stat.lastSample));
            addLine("Time elapsed      : "+((stat.lastSample-stat.firstSample)/1000)+" sec");
            addLine("");
            addLine("Values");
            addLine("----------------------------------------------------------------");
            addLine("Min Value         : "+ Util.double2string(stat.valuesMin, 19, 3, false));
            addLine("Avg Value         : "+ Util.double2string(stat.valuesSum/stat.samplesCnt, 19, 3, false));
            addLine("Max Value         : "+ Util.double2string(stat.valuesMax, 19, 3, false));
            addLine("Sum of Values     : "+ Util.double2string(stat.valuesSum, 19, 3, false));
            addLine("Variance          : "+ Util.double2string(stat.getVariance(), 19, 3, false));
            addLine("Standard Deviation: "+ Util.double2string(stat.getStdDeviation(), 19, 3, false));
            addLine("");
            if (stat.samplesCnt >= 2) {
                addLine("Distance between samples");
                addLine("----------------------------------------------------------------");
                addLine("Min Distance      : "+ Util.double2string((((double)stat.distanceMin)/1000), 15, 3, false)+ " sec");
                addLine("Avg Distance      : "+ Util.double2string((((double)((stat.lastSample-stat.firstSample)/(stat.samplesCnt-1)))/1000), 15, 3, false)+ " sec");
                addLine("Max Distance      : "+ Util.double2string((((double)stat.distanceMax)/1000), 15, 3, false)+ " sec");
            }
        } else {
            addLine("No samples found in selected interval.");
        }
    }

}

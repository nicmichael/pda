package de.nmichael.pda;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import de.nmichael.pda.data.DataSeries;
import de.nmichael.pda.data.DataSeriesProperties;
import de.nmichael.pda.data.DataSeriesSet;
import de.nmichael.pda.data.Project;
import de.nmichael.pda.data.ProjectFile;
import de.nmichael.pda.data.Sample;
import de.nmichael.pda.util.SeriesStatistics;
import de.nmichael.pda.util.Util;

public class Correlation extends CLI {
	
	private boolean skipMissing = false;
	private boolean interpolate = false;
	private long tolerance = 1000;
	
    public Correlation(String[] args, int i) {
    	super(args, i);
        for (; i<args.length; i++) {
            if (args[i] != null && args[i].equals("-z")) {
            	skipMissing = true;
                args[i] = null;
            }
            if (args[i] != null && args[i].equals("-i")) {
            	interpolate = true;
                args[i] = null;
            }
            if (args[i] != null && args[i].equals("-t")) {
                args[i] = null;
                if (i+1 < args.length && args[i+1] != null) {
                	tolerance = Util.string2long(args[i+1], tolerance);
                	args[++i] = null;
                }
            }
        }
        checkArgs(args, i);
    }
	
    @Override
    public boolean run() throws Exception {
    	analyzeCorrelation(item.getDataSeriesSet(true), tolerance);
    	return true;
    }
    
	public void analyzeCorrelation(DataSeriesSet set, long tolerance) throws Exception {
		DataSeries[] series = set.getSelectedSeriesArray();
    	Logger.log(Logger.LogType.info, "Scanning samples in " + series.length + " series (tolerance: " + tolerance + " ms, interpolate=" + interpolate + ") ...");
		ArrayList<Double>[] samples = new ArrayList[series.length];
		for (int i=0; i<series.length; i++) {
			samples[i] = new ArrayList<Double>();
		}
		long ts = 0;
		int cnt = 0;
		int incomplete = 0;
		while ( (ts = set.getNextTimestamp(true, ts, tolerance)) > 0 && ts < Long.MAX_VALUE) {
			Sample[] mySamples = set.getSamplesAtTimestamp(true, ts, tolerance, interpolate);
			if (mySamples == null) {
				continue;
			}
			
			boolean missing = false;
			for (int i=0; !missing && i<mySamples.length; i++) {
				if (mySamples[i] == null || Double.isNaN(mySamples[i].getValue())) {
					missing = true;
                    // System.out.println("missing sample " + i + " at time " + ts + " for " + mySamples.length + " samples");
				}
			}
			if (missing) {
				incomplete++;
				if (skipMissing) {
					continue;
				}
			}
			
			for (int i=0; i<mySamples.length; i++) {
        		samples[i].add(mySamples[i] != null ? mySamples[i].getValue() : Double.NaN);
			}
			cnt++;
		}
		double[][] data = new double[cnt][samples.length];
		for (int i=0; i<cnt; i++) {
			for (int j=0; j<samples.length; j++) {
				data[i][j] = samples[j].get(i);
			}
		}
    	Logger.log(Logger.LogType.info, "Found " + cnt + " data points (" + 
		    incomplete + (skipMissing ? " skipped due to missing samples" : " incomplete data points") + ").");
    	if (cnt == 0) {
    		return;
    	}
    	
    	if (Logger.isDebugLoggin() && incomplete > 0) {
    		for (int i=0; i<samples.length; i++) {
    			int missing = 0;
    			for (int j=0; j<samples[i].size(); j++) {
    				if (Double.isNaN(samples[i].get(j))) {
    					missing++;
    				}
    			}
    			if (missing > 0) {
    				Logger.debug(series[i].getName() + " has " + missing + " missing samples.");
    			}
    		}
    	}

    	Logger.log(Logger.LogType.info, "Running correlation analysis ...");
		PearsonsCorrelation corr = new PearsonsCorrelation(data);
		RealMatrix correlationMatrix = corr.getCorrelationMatrix();
	    RealMatrix pValueMatrix = corr.getCorrelationPValues();
	    RealMatrix standardErrorMatrix = corr.getCorrelationStandardErrors();
	    
	    String[][] results = new String[correlationMatrix.getRowDimension()][correlationMatrix.getColumnDimension()];
		for (int i = 0; i < correlationMatrix.getRowDimension(); i++) {
			for (int j = 0; j < correlationMatrix.getColumnDimension(); j++) {
				results[i][j] = String.format("Rank %4.0f Corr %6.3f pValue=%6.3f stdErr=%6.3f %s",
						(1 - Math.abs(correlationMatrix.getEntry(i, j))) * 1000.0,
						correlationMatrix.getEntry(i, j),
						pValueMatrix.getEntry(i, j), 
						standardErrorMatrix.getEntry(i, j),
						series[j].getName());
			}
		}
		
		String output = getOutputName() != null ? getOutputName() : "correlation.txt";
    	if (output.indexOf(".") < 0) {
    		output = output + ".txt";
    	}
    	Logger.log(Logger.LogType.info, "Printing correlation to " + output + " ...");
    	BufferedWriter f = new BufferedWriter(new FileWriter(output));
		for (int i = 0; i < results.length; i++) {
			f.write(String.format("\n\nCorrelation for: %s (min=%1.2f max=%1.2f avg=%1.2f)\n",
					series[i].getName(),
					series[i].getMinValue(),
					series[i].getMaxValue(),
					series[i].computeAvgValue()));
			Arrays.sort(results[i]);
			for (int j = 0; j < results[i].length; j++) {
				if (!results[i][j].startsWith("Rank  NaN")) {
				    f.write("  " + results[i][j] + "\n");
				}
			}
		}
		f.close();
    	Logger.log(Logger.LogType.info, "Done.");
	}
	
	public static void main(String[] args) {
		double[][] data = { 
				{ 1,2 },
				{ 2,4 }, 
				{ 3,6 },
				{ 4,7 },
				{ 5,8 },
				{ 6,9 },
				{ 7,14 },
				{ 8,16 },
				{ 9,18 },
				{ 10,0 },
				};
    	PearsonsCorrelation corr = new PearsonsCorrelation(data);
	    RealMatrix correlationMatrix = corr.getCorrelationMatrix();
        RealMatrix pValueMatrix = corr.getCorrelationPValues();
        RealMatrix standardErrorMatrix = corr.getCorrelationStandardErrors();
	    String[][] results = new String[correlationMatrix.getRowDimension()][correlationMatrix.getColumnDimension()];
		for (int i = 0; i < correlationMatrix.getRowDimension(); i++) {
			for (int j = 0; j < correlationMatrix.getColumnDimension(); j++) {
				System.out.println(String.format("[%d,%d] Corr %6.3f pValue=%6.3f stdErr=%6.3f",
						i, j,
						correlationMatrix.getEntry(i, j),
						pValueMatrix.getEntry(i, j), 
						standardErrorMatrix.getEntry(i, j)));
			}
		}
		
	}

}

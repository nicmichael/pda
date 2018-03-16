/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.data;

public class StatisticsData {
    
    public long samplesCnt = 0;
    public long firstSample = Long.MIN_VALUE;
    public long lastSample = Long.MAX_VALUE;
    
    public double valuesSum = 0.0;
    public double valuesSquareSum = 0.0;
    public double valuesMin = Double.MAX_VALUE;
    public double valuesMax = Double.MIN_VALUE;
    
    public long distanceMax = 0;
    public long distanceMin = Long.MAX_VALUE;

    public double getVariance() {
        if (samplesCnt < 2) return 0.0;
        double avg = valuesSum / (double)samplesCnt;
        return (valuesSquareSum - (double)samplesCnt * avg*avg) / (double)(samplesCnt - 1);
    }
    
    public double getStdDeviation() {
        return Math.sqrt(getVariance());
    }
    
}

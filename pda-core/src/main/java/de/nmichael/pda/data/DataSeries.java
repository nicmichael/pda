/**
 * Title:        Performance Data Analyzer (PDA)
 * Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
 * Website:      http://pda.nmichael.de/
 * License:      GNU General Public License v2
 *
 * @author Nicolas Michael
 * @version 2
 * 
 */
package de.nmichael.pda.data;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.nmichael.pda.util.ColorSelector;

public class DataSeries implements Comparable {

    public static String SEPARATOR = ":";
    public static Pattern namePattern = Pattern.compile("([^:]*):([^:]*):([^:]*):([^:]*)");
    private Parser parser;
    private String parserName;
    private String categoryName;
    private String subcategoryName;
    private String seriesName;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    private boolean selected;     // selected as an individual series
    private boolean parsed;       // parsed as an individual series or group
    private boolean used;         // used either as individual series or within a group
    private boolean isScanned = false;
    private boolean nonZeroSamples = false;
    private Sample minSample;
    private Sample maxSample;
    private Sample lastSample;
    private boolean ignoreFirst = false;
    private boolean cumulative = false;
    private boolean rate = false;
    private int cursor = 0;
    private double preferredScaleMinValue = 0;
    private double preferredScaleMaxValue = Double.MAX_VALUE;
    private int preferredStyle = DataSeriesProperties.STYLE_LINE;
    private DataSeriesProperties dataProperties;

    public DataSeries(Parser parser, String category, String subcategory, String series) {
        this.parser = parser;
        this.parserName = parser.getName();
        this.categoryName = category;
        this.subcategoryName = subcategory;
        this.seriesName = series;
    }
    
    DataSeries(String parserName, String category, String subcategory, String series) {
        this.parserName = parserName;
        this.categoryName = category;
        this.subcategoryName = subcategory;
        this.seriesName = series;
    }
    
    void setParserName(String parserName) {
        this.parserName = parserName;
    }

    public String getName() {
        return parserName + SEPARATOR + categoryName + SEPARATOR + subcategoryName + SEPARATOR + seriesName;
    }

    public String getLocalName() {
        return categoryName + SEPARATOR + subcategoryName + SEPARATOR + seriesName;
    }

    public Parser getParser() {
        return parser;
    }

    public String getParserName() {
        return parserName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getSubcategoryName() {
        return subcategoryName;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public int getNumberOfSamples() {
        return samples.size();
    }

    public Sample getSample(int i) {
        try {
            return samples.get(i);
        } catch(Exception eoutofbounds) {
            return null;
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            setUsed(true);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    public boolean isParsed() {
        return parsed;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isUsed() {
        return used;
    }
    
    public void setHasBeenScanned() {
        this.isScanned = true;
    }

    public boolean hasBeenScanned() {
        return isScanned;
    }

    public void setHasNonZeroSamples() {
        this.nonZeroSamples = true;
    }

    public boolean hasNonZeroSamples() {
        return nonZeroSamples;
    }

    public void setCumulative(boolean cumulative, boolean ignoreFirst, boolean rate) {
    	this.cumulative = cumulative;
    	this.ignoreFirst = ignoreFirst;
    	this.rate = rate;
    }
    
    public boolean isMonotonic() {
        double v = Double.MIN_VALUE;
        for (Sample sample : samples) {
            if (sample.getValue() < v) {
                return false;
            }
            v = sample.getValue();
        }
        return true;
    }
    
    public void convertToMonotonic() {
        ArrayList<Sample> monotonic = new ArrayList<Sample>();
        if (samples.size() < 2) {
            return;
        }
        minSample = null;
        maxSample = null;
        Sample prev = samples.get(0);
        for (int i=1; i<samples.size(); i++) {
            Sample cur = samples.get(i);
            Sample m = new Sample(cur.getTimeStamp(), cur.getValue() - prev.getValue());
            monotonic.add(m);
            updateMinMax(m);
            prev = cur;
        }
    }

    /**
     * Returns a list of all parsers for this series that require parsing
     * for series that are used, but haven't been parsed.
     * For single series, this is either the own parser or null.
     * For groups, this may be any number of parsers from any of the group series.
     * @return the list of diry parsers or null, if none
     */
    public ArrayList<Parser> getDirtyParsers() {
        if (isUsed() && !isParsed()) {
            ArrayList<Parser> plist = new ArrayList<Parser>();
            plist.add(parser);
            return plist;
        }
        return null;
    }

    public void clearSamples() {
        samples = new ArrayList<Sample>();
        parsed = false;
    }

    private void updateMinMax(Sample sample) {
        if (minSample == null || sample.getValue() < minSample.getValue()) {
            minSample = sample;
        }
        if (maxSample == null || sample.getValue() > maxSample.getValue()) {
            maxSample = sample;
        }
    }

    public void addSample(Sample sample) {
        Sample mySample = sample;
        if (cumulative) {
            if (lastSample != null) {
                double value = sample.getValue() - lastSample.getValue();
                if (rate && sample.timestamp > lastSample.timestamp) {
                    value /= ((double) (sample.timestamp - lastSample.timestamp)) / 1000.0;
                }
                mySample = new Sample(sample.timestamp, value);
            } else if (ignoreFirst) {
                mySample = null;
            }
            lastSample = sample;
        }
        if (mySample != null) {
            samples.add(mySample);
            updateMinMax(mySample);
        }
    }

    public void addSample(long ts, double value) {
        Sample sample = new Sample(ts, value);
        samples.add(sample);
        updateMinMax(sample);
    }
    
    public boolean addSampleIfNeeded(long ts, double value) {
        if (!isSamplesNeeded()) {
            return false;
        }
        addSample(new Sample(ts, value));
        return true;
    }
    
    public void addSample(long ts, double value, String label) {
        SampleWithLabel sample = new SampleWithLabel(ts, value, label);
        samples.add(sample);
        updateMinMax(sample);
    }
    
    public boolean addSampleIfNeeded(long ts, double value, String label) {
        if (!isSamplesNeeded()) {
            return false;
        }
        addSample(new SampleWithLabel(ts, value, label));
        return true;
    }
    
    public boolean isSamplesNeeded() {
        // before bugfix, this line only read "isUsed()". However, samples are only cleared
        // in case of a forced reparse, which leads series that have already been selected
        // when another series from the same parser gets selected to be reparsed. This
        // causes samples of this series to appear twice (with a line from last to first sample)
        return isUsed() && !isParsed(); // since we clear samples before reparsing, just need to check for isUsed(); && !isParsed();
    }

    // add amount to an already existing sample at time timestamp
    private boolean addAmountToExistingSample(long timestamp, double amount, int maxStepBack) {
        int stepcnt = 0;
        for (int i = samples.size() - 1; i >= 0; i--) {
            if (stepcnt++ == maxStepBack) {
                break;
            }
            Sample s = samples.get(i);
            if (s.getTimeStamp() == timestamp) {
                s.incValue(amount);
                updateMinMax(s);
                return true;
            }
        }
        return false;
    }

    protected void addAmountToSample(long timestamp, double amount, int maxStepBack) {
        if (!addAmountToExistingSample(timestamp, amount, maxStepBack)) {
            addSample(new Sample(timestamp, amount));
        }
    }

    public void gotoSample(int i) {
        cursor = i;
    }

    public void gotoFirstSample() {
        gotoSample(0);
    }

    public Sample getNextSample() {
        if (cursor >= 0 && cursor < samples.size()) {
            return samples.get(cursor++);
        }
        return null;
    }

    public Sample getCurrentSample() {
        if (cursor >= 0 && cursor < samples.size()) {
            return samples.get(cursor);
        }
        return null;
    }

    public Sample getFirstSample() {
        if (samples.size() == 0) {
            return null;
        }
        return samples.get(0);
    }

    public Sample getLastSample() {
        if (samples.size() == 0) {
            return null;
        }
        return samples.get(samples.size() - 1);
    }
    
    public long getFirstTimestamp() {
        Sample s = getFirstSample();
        return (s != null ? s.getTimeStamp() : 0);
    }

    public long getLastTimestamp() {
        Sample s = getLastSample();
        return (s != null ? s.getTimeStamp() : Long.MAX_VALUE);
    }

    public Sample getMaxSample() {
        return maxSample;
    }

    public Sample getMinSample() {
        return minSample;
    }

    public double getMaxValue() {
        Sample s = getMaxSample();
        if (s != null) {
            return s.getValue();
        } else {
            return Double.MAX_VALUE;
        }
    }

    public double getMinValue() {
        Sample s = getMinSample();
        if (s != null) {
            return s.getValue();
        } else {
            return 0;
        }
    }

    public double getPreferredScaleMinValue() {
        return preferredScaleMinValue;
    }

    public double getPreferredScaleMaxValue() {
        return preferredScaleMaxValue;
    }

    public int getPreferredStyle() {
        return preferredStyle;
    }

    protected void setPreferredScale(double minScale, double maxScale) {
        preferredScaleMinValue = minScale;
        preferredScaleMaxValue = maxScale;
    }

    public void setPreferredStyle(int style) {
        preferredStyle = style;
        if (dataProperties != null) {
            dataProperties.setStyle(style);
        }
    }

    public StatisticsData getStatisticsData(long from, long to, int smooth) {
        StatisticsData stat = new StatisticsData();

        Sample s;
        long lastTimestamp = Long.MIN_VALUE;
        gotoFirstSample();
        double[] smoothval = null;
        int smoothi = 0;
        if (smooth > 1) {
            smoothval = new double[smooth];
        }
        while ((s = getNextSample()) != null) {
            if (s.getTimeStamp() >= from && s.getTimeStamp() <= to) {

                stat.samplesCnt++;
                if (stat.firstSample == Long.MIN_VALUE) {
                    stat.firstSample = s.getTimeStamp();
                }
                stat.lastSample = s.getTimeStamp();

                double value = s.getValue();
                if (smooth > 1) {
                    smoothval[smoothi] = value;
                    smoothi = (smoothi + 1) % smooth;
                    if (stat.samplesCnt >= smooth) {
                        value = 0;
                        for (int i = 0; i < smoothval.length; i++) {
                            value += smoothval[i];
                        }
                        value /= smooth;
                    }
                }

                stat.valuesSum += value;
                stat.valuesSquareSum += value * value;
                if (value < stat.valuesMin) {
                    stat.valuesMin = value;
                }
                if (value > stat.valuesMax) {
                    stat.valuesMax = value;
                }

                if (lastTimestamp != Long.MIN_VALUE) {
                    long distance = Math.abs(s.getTimeStamp() - lastTimestamp);
                    if (distance > stat.distanceMax) {
                        stat.distanceMax = distance;
                    }
                    if (distance < stat.distanceMin) {
                        stat.distanceMin = distance;
                    }
                }
                lastTimestamp = s.getTimeStamp();
            } else {
                if (s.getTimeStamp() > to) {
                    break;
                }
            }
        }

        return stat;
    }

    public void setDataProperties(DataSeriesProperties dataProperties) {
        this.dataProperties = dataProperties;
    }
    
    public void createDataProperties(DataSeriesPropertySet propertySet, ColorSelector colorSelector) {
        DataSeriesProperties prop = new DataSeriesProperties(this);
        prop.setDisplayName(getLocalName());
        prop.setColor(colorSelector.getNextColor());
        prop.setScaleMin(getPreferredScaleMinValue());
        prop.setScaleMax(getPreferredScaleMaxValue());
        prop.setStyle(getPreferredStyle());
        setDataProperties(prop);
        propertySet.addDataProperties(prop);
    }

    public DataSeriesProperties getDataProperties() {
        return dataProperties;
    }
    
    public boolean findDataProperties(DataSeriesPropertySet propertySet) {
        DataSeriesProperties dp = propertySet.getDataProperties(getName());
        if (dp != null) {
            this.setDataProperties(dp);
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Object t) {
        return this.getName().compareTo(((DataSeries) t).getName());
    }

    private static String getNamePart(String s, int i) {
        Matcher m = namePattern.matcher(s);
        if (m.matches()) {
            return m.group(i);
        }
        return "";
    }

    public static String getParserName(String s) {
        return getNamePart(s, 1);
    }

    public static String getCategoryName(String s) {
        return getNamePart(s, 2);
    }

    public static String getSubcategoryName(String s) {
        return getNamePart(s, 3);
    }

    public static String getSeriesName(String s) {
        return getNamePart(s, 4);
    }
    
    public static String fixName(String s) {
        return s.replaceAll(SEPARATOR, "-");
    }
    
}
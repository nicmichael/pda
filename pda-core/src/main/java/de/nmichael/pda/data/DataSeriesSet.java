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

import de.nmichael.pda.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * A set of series.
 * This set is instantiated once per parser and holds all the series
 * of that parser.
 */
public class DataSeriesSet {

    private Parser parser;
    private DataSeriesPropertySet propertySet;
    private Hashtable<String, Hashtable<String, Hashtable<String, DataSeries>>> allseries =
            new Hashtable<String, Hashtable<String, Hashtable<String, DataSeries>>>();
    private Hashtable<String, DataSeriesProperties> savedSelectedSeries;
    private ArrayList<String> savedUsedSeries;

    /**
     * Constructs a new set of series for a parser
     * @param parser the parser
     */
    public DataSeriesSet(Parser parser) {
        this.parser = parser;
    }
    
    /**
     * Creates and adds a new series to this series set.
     * @param category the category name of the new series
     * @param subcategory the subcategory name of the new series
     * @param series the series name of the new series
     * @return the newly created series or null, if this series already exists
     */
    public DataSeries addSeries(String category, String subcategory, String series) {
        category = strip(category);
        subcategory = strip(subcategory);
        series = strip(series);
        Hashtable<String, Hashtable<String, DataSeries>> allsub = allseries.get(category);
        if (allsub == null) {
            allsub = new Hashtable<String, Hashtable<String, DataSeries>>();
        }
        Hashtable<String, DataSeries> allser = allsub.get(subcategory);
        if (allser == null) {
            allser = new Hashtable<String, DataSeries>();
        }
        DataSeries ser = allser.get(series);
        if (ser != null) {
            return null; // duplicate series
        }
        ser = new DataSeries(parser, category, subcategory, series);
        allser.put(series, ser);
        allsub.put(subcategory, allser);
        allseries.put(category, allsub);
        return ser;
    }

    /**
     * Gets or creates a series 
     * @param category the category name of the new series
     * @param subcategory the subcategory name of the new series
     * @param series the series name of the new series
     * @return the newly created series or null, if this series already exists
     */
    public DataSeries getOrAddSeries(String category, String subcategory, String series) {
        DataSeries s = getSeries(category, subcategory, series);
        if (s != null) {
            return s;
        }
        return addSeries(category, subcategory, series);
    }

    /**
     * Adds a sample to an existing series.
     * @param category the category name of the series
     * @param subcategory the subcategory name of the series
     * @param series the series name of the series
     * @param ts the timestamp of the sample
     * @param value the value of the sample
     * @return true, if this sample was added
     */
    public boolean addSample(String category, String subcategory, String series,
            long ts, double value) {
        DataSeries s = getSeries(category, subcategory, series);
        if (s != null) {
            s.addSample(new Sample(ts, value));
            return true;
        }
        return false;
    }

    /**
     * Adds a sample to an existing series, if this sample is "needed". A sample
     * is typically needed if this series has been selected to be plotted, but has
     * not yet been parsed.
     * @param category the category name of the series
     * @param subcategory the subcategory name of the series
     * @param series the series name of the series
     * @param ts the timestamp of the sample
     * @param value the value of the sample
     * @return true, if this sample was added
     */
    public boolean addSampleIfNeeded(String category, String subcategory, String series,
            long ts, double value) {
        DataSeries s = getSeries(category, subcategory, series);
        if (s != null) {
            // only add sample if this series is selected and has not yet been parsed
            s.setHasBeenScanned();
            if (!s.isSamplesNeeded()) {
                if (value != 0) {
                    s.setHasNonZeroSamples();
                }
                return false;
            }
            s.addSample(new Sample(ts, value));
            return true;
        }
        return false;
    }

    /**
     * Removes all series and their samples from this set.
     */
    public void clearAll() {
        allseries =
                new Hashtable<String, Hashtable<String, Hashtable<String, DataSeries>>>();
    }

    /**
     * Removes the samples of all series, but keeps the empty series.
     */
    public void clearSeriesSamples() {
        ArrayList<DataSeries> all = getAllSeries();
        for (DataSeries s : all) {
            s.clearSamples();
        }
    }

    /**
     * Sets all series of this set to be un-selected.
     */
    public void clearSelection() {
        ArrayList<DataSeries> all = getAllSeries();
        for (DataSeries s : all) {
            s.setSelected(false);
        }
    }

    /**
     * Remembers the currently selected series. This method can be used to
     * restore the selection by calling restoreSelection() at a later time.
     */
    protected void rememberSelection() {
        ArrayList<DataSeries> all = getAllSeries();
        savedSelectedSeries = new Hashtable<String, DataSeriesProperties>();
        savedUsedSeries = new ArrayList<String>();
        for (DataSeries s : all) {
            if (s.isSelected()) {
                DataSeriesProperties p = s.getDataProperties();
                if (p != null) {
                    savedSelectedSeries.put(s.getLocalName(), p);
                }
            }
            if (s.isUsed()) {
                savedUsedSeries.add(s.getLocalName());
            }
        }
    }

    /**
     * All series, that had been selected and had been remembered by calling
     * rememberSelection() will be set to be selected again by calling this method.
     */
    protected void restoreSelection() {
        if (savedSelectedSeries == null) {
            return;
        }
        for (String name : savedSelectedSeries.keySet()) {
            DataSeries s = getSeries(name);
            if (s != null) {
                s.setSelected(true);
                DataSeriesProperties p = savedSelectedSeries.get(name);
                if (p != null) {
                    p.setSeries(s);
                }
            }
        }
        for (String name : savedUsedSeries) {
            DataSeries s = getSeries(name);
            if (s != null) {
                s.setUsed(true);
            }
        }
    }

    /**
     * Returns an existing series
     * @param category the category name of the series
     * @param subcategory the subcategory name of the series
     * @param series the series name of the series
     * @return the series, or null if it does not exist
     */
    public DataSeries getSeries(String category, String subcategory, String series) {
        category = strip(category);
        subcategory = strip(subcategory);
        series = strip(series);
        Hashtable<String, Hashtable<String, DataSeries>> allsub = allseries.get(category);
        if (allsub != null) {
            Hashtable<String, DataSeries> allser = allsub.get(subcategory);
            if (allser != null) {
                return allser.get(series);
            }
        }
        return null;
    }

    /**
     * Returns an existing series
     * @param localName the local name of the series in the format category:subcategory:series
     * @return the series or null, if it does not exist
     */
    public DataSeries getSeries(String localName) {
        try {
            int p1 = localName.indexOf(DataSeries.SEPARATOR);
            int p2 = localName.indexOf(DataSeries.SEPARATOR, p1 + 1);
            return getSeries(localName.substring(0, p1),
                    localName.substring(p1 + 1, p2),
                    localName.substring(p2 + 1));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns an existing series
     * @param globalName the global name of the series in the format parser:category:subcategory:series
     * @return the series or null, if it does not exist
     */
    public DataSeries getGlobalSeries(String globalName) {
        try {
            int pos = globalName.indexOf(DataSeries.SEPARATOR);
            return getSeries(globalName.substring(pos+1));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Returns all category names
     * @return an array of all category names
     */
    public String[] getCategoryNames() {
        return allseries.keySet().toArray(new String[0]);
    }

    /**
     * Returns all subcategory names for a given category
     * @param category the category name
     * @return an array of all subcategory names
     */
    public String[] getSubcategoryNames(String category) {
        Hashtable<String, Hashtable<String, DataSeries>> allsub = allseries.get(category);
        if (allsub != null) {
            return allsub.keySet().toArray(new String[0]);
        }
        return null;
    }

    /**
     * Returns all series names for a given category and subcategory
     * @param category the category name
     * @param subcategory the subcategory name
     * @return an array of all series names
     */
    public String[] getSeriesNames(String category, String subcategory) {
        Hashtable<String, Hashtable<String, DataSeries>> allsub = allseries.get(category);
        if (allsub != null) {
            Hashtable<String, DataSeries> allser = allsub.get(subcategory);
            if (allser != null) {
                return allser.keySet().toArray(new String[0]);
            }
        }
        return null;
    }

    /**
     * Returns all series names that match the specified pattern
     * @param p the pattern, a regular expression
     * @return all matching series names
     */
    public String[] getSeriesNames(Pattern p) {
        String[] all = getAllSeriesNames();
        ArrayList<String> matching = new ArrayList<String>();
        for (String s : all) {
            if (p.matcher(s).matches()) {
                matching.add(s);
            }
        }
        return matching.toArray(new String[0]);
    }

    /**
     * Returns the total number of categories
     * @return the number of categories
     */
    public int getNumberOfCategories() {
        return allseries.size();
    }

    /**
     * Returns the total number of subcategories for a given category
     * @param category the category name
     * @return the number of subcategories
     */
    public int getNumberOfSubcategories(String category) {
        Hashtable<String, Hashtable<String, DataSeries>> allsub = allseries.get(category);
        if (allsub != null) {
            return allsub.size();
        }
        return 0;
    }

    /**
     * Returns the total number of series for a given category and subcategory
     * @param category the category name
     * @param subcategory the subcategory name
     * @return the number of series
     */
    public int getNumberOfSeries(String category, String subcategory) {
        Hashtable<String, Hashtable<String, DataSeries>> allsub = allseries.get(category);
        if (allsub != null) {
            Hashtable<String, DataSeries> allser = allsub.get(subcategory);
            if (allser != null) {
                return allser.size();
            }
        }
        return 0;
    }

    /**
     * Returns all series
     * @return an arraylist of all series
     */
    public ArrayList<DataSeries> getAllSeries() {
        ArrayList<DataSeries> all = new ArrayList<DataSeries>();
        String[] cats = getCategoryNames();
        for (String cat : cats) {
            String[] subcats = getSubcategoryNames(cat);
            for (String subcat : subcats) {
                String[] sers = getSeriesNames(cat, subcat);
                for (String ser : sers) {
                    all.add(getSeries(cat, subcat, ser));
                }
            }
        }
        return all;
    }

    /**
     * Returns all series
     * @return an array of all series
     */
    public DataSeries[] getAllSeriesArray() {
        ArrayList<DataSeries> all = getAllSeries();
        DataSeries[] arr = all.toArray(new DataSeries[0]);
        Arrays.sort(arr);
        return arr;
    }

    /**
     * Returns all selected series
     * @return an arraylist of all series
     */
    public ArrayList<DataSeries> getSelectedSeries() {
        ArrayList<DataSeries> all = new ArrayList<DataSeries>();
        String[] cats = getCategoryNames();
        for (String cat : cats) {
            String[] subcats = getSubcategoryNames(cat);
            for (String subcat : subcats) {
                String[] sers = getSeriesNames(cat, subcat);
                for (String ser : sers) {
                    DataSeries s = getSeries(cat, subcat, ser);
                    if (s.isSelected()) {
                        all.add(s);
                    }
                }
            }
        }
        return all;
    }

    /**
     * Returns all selected series
     * @return an array of all selected series
     */
    public DataSeries[] getSelectedSeriesArray() {
        ArrayList<DataSeries> all = getSelectedSeries();
        DataSeries[] arr = all.toArray(new DataSeries[0]);
        Arrays.sort(arr);
        return arr;
    }

    /**
     * Returns all series names. Names are returned fully qualified, including th
     * parser name in the format parser:category:subcategory:series
     * @return an array of all series names
     */
    public String[] getAllSeriesNames() {
        DataSeries[] allser = getAllSeriesArray();
        String[] allnames = new String[allser.length];
        for (int i = 0; i < allser.length; i++) {
            allnames[i] = allser[i].getName();
        }
        return allnames;
    }

    /**
     * Returns the total number of series in this set.
     * @return the number of series
     */
    public int size() {
        return getAllSeries().size();
    }

    /**
     * Sets the preferred scale of all series individually, by finding the maximum
     * sample of each series and setting its scale accordingly.
     */
    public void setPreferredScaleIndividual() {
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    setPreferredScaleIndividual(c, sc, s);
                }
            }
        }
    }

    /**
     * Sets the preferred scale of all series with a given series name individually,
     * by finding the maximum sample of each series and setting its scale accordingly.
     * @param seriesName the series name
     */
    public void setPreferredScaleIndividual(String seriesName) {
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                DataSeries series = getSeries(c, sc, seriesName);
                if (series != null) {
                    double max = series.getMaxValue();
                    series.setPreferredScale(0, Util.roundUpToScale((max != Double.MAX_VALUE ? max : 1)));
                }
            }
        }
    }

    /**
     * Sets the preferred scale of a series with a given name individually,
     * by finding the maximum sample of each series and setting its scale accordingly.
     * @param categoryName the category name of the series
     * @param subcategoryName the subcategory name of the series
     * @param seriesName the series name of the series
     */
    public void setPreferredScaleIndividual(String categoryName, String subcategoryName, String seriesName) {
        DataSeries series = getSeries(categoryName, subcategoryName, seriesName);
        if (series != null) {
            double max = series.getMaxValue();
            series.setPreferredScale(0, Util.roundUpToScale((max != Double.MAX_VALUE ? max : 1)));
        }
    }

    /**
     * Sets the preferred scale of all series with same category, subcategory, and/or series names
     * to the same value.
     * @param sameCategory if true, group series with same category name
     * @param sameSubcategory if true, group series with same subcategory name
     * @param sameSeries if true, group series with same series name
     */
    public void setPreferredScaleSame(boolean sameCategory, boolean sameSubcategory, boolean sameSeries) {
        Hashtable<String, Double> maxValues = new Hashtable<String, Double>();
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    String key = (sameCategory ? c : "") + ":"
                            + (sameSubcategory ? sc : "") + ":"
                            + (sameSeries ? s : "");
                    Double allmax = maxValues.get(key);
                    double max = series.getMaxValue();
                    if (max == Double.MAX_VALUE) {
                        max = 0;
                    }
                    //System.out.println(c + ":" + sc + ":" + s + " = " + max);
                    max = Math.max((allmax != null ? allmax.doubleValue() : 0), max);
                    maxValues.put(key, max);
                }
            }
        }
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    String key = (sameCategory ? c : "") + ":"
                            + (sameSubcategory ? sc : "") + ":"
                            + (sameSeries ? s : "");
                    Double max = maxValues.get(key).doubleValue();
                    series.setPreferredScale(0, Util.roundUpToScale((max != Double.MAX_VALUE ? max : 1)));
                }
            }
        }
    }

    /**
     * Sets the preferred minimum scale of all series with same category, subcategory, and/or series names
     * to the same value.
     * @param sameCategory if true, group series with same category name
     * @param sameSubcategory if true, group series with same subcategory name
     * @param sameSeries if true, group series with same series name
     */
    public void setPreferredScaleSameMin(boolean sameCategory, boolean sameSubcategory, boolean sameSeries) {
        Hashtable<String, Double> minValues = new Hashtable<String, Double>();
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    String key = (sameCategory ? c : "") + ":"
                            + (sameSubcategory ? sc : "") + ":"
                            + (sameSeries ? s : "");
                    Double allmin = minValues.get(key);
                    double min = series.getMinValue();
                    //System.out.println(c + ":" + sc + ":" + s + " = " + max);
                    min = Math.min((allmin != null ? allmin.doubleValue() : 0), min);
                    minValues.put(key, min);
                }
            }
        }
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    String key = (sameCategory ? c : "") + ":"
                            + (sameSubcategory ? sc : "") + ":"
                            + (sameSeries ? s : "");
                    Double min = minValues.get(key).doubleValue();
                    series.setPreferredScale(-1 * Util.roundUpToScale(Math.abs(min)), series.getPreferredScaleMaxValue());
                }
            }
        }
    }

    /**
     * Sets the both the preferred minimum and maximum scale of all series with same category, subcategory, and/or series names
     * to the same value (min=max for all series).
     * @param sameCategory if true, group series with same category name
     * @param sameSubcategory if true, group series with same subcategory name
     * @param sameSeries if true, group series with same series name
     */
    public void setPreferredScaleMinMaxSame(boolean sameCategory, boolean sameSubcategory, boolean sameSeries) {
        Hashtable<String, Double> maxValues = new Hashtable<String, Double>();
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    String key = (sameCategory ? c : "") + ":"
                            + (sameSubcategory ? sc : "") + ":"
                            + (sameSeries ? s : "");
                    Double allmax = maxValues.get(key);
                    double max = Math.max(series.getMaxValue(), Math.abs(series.getMinValue()));
                    if (max == Double.MAX_VALUE) {
                        max = 0;
                    }
                    //System.out.println(c + ":" + sc + ":" + s + " = " + max);
                    max = Math.max((allmax != null ? allmax.doubleValue() : 0), max);
                    maxValues.put(key, max);
                }
            }
        }
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    String key = (sameCategory ? c : "") + ":"
                            + (sameSubcategory ? sc : "") + ":"
                            + (sameSeries ? s : "");
                    Double max = maxValues.get(key).doubleValue();
                    double scaledMax = Util.roundUpToScale((max != Double.MAX_VALUE ? max : 1));
                    series.setPreferredScale(-1 * scaledMax, scaledMax);
                }
            }
        }
    }

    /**
     * Sets the preferred scale of all series with a set of series names to an
     * automatically determined min and max value
     * @param seriesNames the series names of these series
     */
    public void setPreferredScaleSame(String[] seriesNames) {
        double allmax = 0;
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : seriesNames) {
                    DataSeries series = getSeries(c, sc, s);
                    if (series != null) {
                        double max = series.getMaxValue();
                        if (max != Double.MAX_VALUE) {
                            allmax = Math.max(max, allmax);
                        }
                    }
                }
            }
        }
        allmax = Util.roundUpToScale((allmax != Double.MAX_VALUE ? allmax : 1));
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : seriesNames) {
                    DataSeries series = getSeries(c, sc, s);
                    if (series != null) {
                        series.setPreferredScale(0, allmax);
                    }
                }
            }
        }
    }

    /**
     * Sets the preferred scale of all series to a specific min and max value
     * @param minScale the min value
     * @param maxScale the max value
     */
    public void setPreferredScaleAll(double minScale, double maxScale) {
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    series.setPreferredScale(minScale, maxScale);
                }
            }
        }
    }

    /**
     * Sets the preferred scale of all series of a given category to a specific
     * min and max value
     * @param categoryName the category name of these series
     * @param minScale the min value
     * @param maxScale the max value
     */
    public void setPreferredScaleCategory(String categoryName, double minScale, double maxScale) {
        String[] cats = getSubcategoryNames(categoryName);
        if (cats == null) {
            return;
        }
        for (String sc : cats) {
            for (String s : getSeriesNames(categoryName, sc)) {
                DataSeries series = getSeries(categoryName, sc, s);
                if (series != null) {
                    series.setPreferredScale(minScale, maxScale);
                }
            }
        }
    }

    /**
     * Sets the preferred scale of all series of a given series name to a specific
     * min and max value
     * @param seriesName the series name of these series
     * @param minScale the min value
     * @param maxScale the max value
     */
    public void setPreferredScaleSeries(String seriesName, double minScale, double maxScale) {
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                DataSeries series = getSeries(c, sc, seriesName);
                if (series != null) {
                    series.setPreferredScale(minScale, maxScale);
                }
            }
        }
    }

    /**
     *  Sets the preferred style of all series
     * @param style the style of the series, one of DataSeriesProperties.SYLE_XXX
     */
    public void setPreferredStyleAll(int style) {
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                for (String s : getSeriesNames(c, sc)) {
                    DataSeries series = getSeries(c, sc, s);
                    series.setPreferredStyle(style);
                }
            }
        }
    }

    /**
     *  Sets the preferred style of all series with a specicif series name
     * @param series the series name of the series
     * @param style the style of the series, one of DataSeriesProperties.SYLE_XXX
     */
    public void setPreferredStyleSeries(String series, int style) {
        for (String c : getCategoryNames()) {
            for (String sc : getSubcategoryNames(c)) {
                DataSeries s = getSeries(c, sc, series);
                if (s != null) {
                    s.setPreferredStyle(style);
                }
            }
        }
    }
    
    /**
     * Returns the first timestamp of any series in this set.
     * @param onlySelectedSeries if true, only consider selected series
     * @return the first timestamp
     */
    public long getFirstTimestamp(boolean onlySelectedSeries) {
        long ts = Long.MAX_VALUE;
        for (DataSeries series : getAllSeries()) {
            if (onlySelectedSeries && !series.isSelected()) {
                continue;
            }
            Sample s = series.getFirstSample();
            if (s != null && s.timestamp > 0) {
                ts = Math.min(ts, s.timestamp);
            }
        }
        return (ts != Long.MAX_VALUE ? ts : 0);
    }

    /**
     * Returns the last timestamp of any series in this set.
     * @param onlySelectedSeries if true, only consider selected series
     * @return the last timestamp
     */
    public long getLastTimestamp(boolean onlySelectedSeries) {
        long ts = 0;
        for (DataSeries series : getAllSeries()) {
            if (onlySelectedSeries && !series.isSelected()) {
                continue;
            }
            Sample s = series.getLastSample();
            if (s != null && s.timestamp > 0) {
                ts = Math.max(ts, s.timestamp);
            }
        }
        return (ts != 0 ? ts : Long.MAX_VALUE);
    }
    
    /**
     * Strips a string from all characters that are not allowed in series names
     * @param s the string
     * @return the stripped string
     */
    private String strip(String s) {
        return s.replaceAll(":", "_");
    }

}

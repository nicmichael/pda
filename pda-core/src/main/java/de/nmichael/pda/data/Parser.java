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

import de.nmichael.pda.Logger;
import de.nmichael.pda.Parsers;
import de.nmichael.pda.util.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Parser {
    
    class SeriesName {
        String category;
        String subcategory;
        String series;
        boolean base1024units;
        double scaleFactor;
        SeriesName(String category, String subcategory, String series, boolean base1024units, double scaleFactor) {
            this.category = category;
            this.subcategory = subcategory;
            this.series = series;
            this.base1024units = base1024units;
            this.scaleFactor = scaleFactor;
        }
    }

    public static final String XML_PARSER = "Parser";
    public static final String XML_NAME = "Name";
    public static final String XML_PARSER_CLASS = "Class";
    public static final String XML_PARSER_PARAMETER = "Parameter";
    public static final String XML_PARSER_PARAM_NAME = "Name";
    public static final String XML_PARSER_PARAM_VALUE = "Value";
    public static final String XML_FILENAME = "FileName";
    public static final String PARAM_INTERVAL = "Interval [sec]";
    public static final String PARAM_TIME_OFFSET_HH = "Time Offet [hh]";
    public static final String PARAM_TIME_OFFSET_MM = "Time Offet [mm]";
    public static final String PARAM_TIME_OFFSET_SS = "Time Offet [ss]";
    private long interval = 0;
    private long tsOffset_hours = 0;
    private long tsOffset_minutes = 0;
    private long tsOffset_secs = 0;
    protected final Hashtable<String, String> parameters = new Hashtable<String, String>();
    private String name;
    private FileFormatDescription fileFormatDescription;
    private String filename;
    private String projectFileName;
    private BufferedReader f;
    private TimeStamp timestamp;
    private DataSeriesSet seriesSet;
    private Pattern newSamplesHeader;
    private Hashtable<Pattern,SeriesName> patterns = new Hashtable<Pattern,SeriesName>(); 

    public Parser(String name) {
        initialize(name);
    }

    public static boolean canHandle(String filename, String search) {
        String name = Util.getNameOfFile(filename);
        return (name != null && name.indexOf(search) >= 0);
    }

    public abstract boolean canHandle(String filename);

    public abstract void parse();

    public abstract void createAllSeries();

    private void initialize(String name) {
        this.name = name;
        seriesSet = new DataSeriesSet(this);
        setParameter(PARAM_INTERVAL, Long.toString(interval));
        setParameter(PARAM_TIME_OFFSET_HH, "0");
        setParameter(PARAM_TIME_OFFSET_MM, "0");
        setParameter(PARAM_TIME_OFFSET_SS, "0");
        timestamp = new TimeStamp(tsOffset_hours, tsOffset_minutes, tsOffset_secs);
    }

    protected void setName(String name) {
        if (name == null || name.length() == 0) {
            name = "unknown";
        }
        this.name = name.replaceAll(":", "-");
        // update all series names
        ArrayList<DataSeries> series = series().getAllSeries();
        for (DataSeries s : series) {
            s.setParserName(name);
            if (s.getDataProperties() != null) {
                s.getDataProperties().updateName();
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getRelativeFilename() {
        if (projectFileName != null && projectFileName.length() > 0) {
            String projectDir = Util.getPathOfFile(projectFileName);
            if (projectDir != null && projectDir.length() >0 && 
                filename.startsWith(projectDir) && filename.length() > projectFileName.length()) {
                String relativeName = filename.substring(projectDir.length());
                String filesep = System.getProperty("file.separator");
                if (relativeName.startsWith(relativeName) && relativeName.length() > 1) {
                    relativeName = relativeName.substring(1);
                }
                return relativeName;
            }
        }
        return filename;
    }

    protected void setSupportedFileFormat(FileFormatDescription fileFormat) {
        this.fileFormatDescription = fileFormat;
    }
    
    protected void setDefaultInterval(long interval) {
        this.interval = interval;
    }

    public FileFormatDescription getSupportedFileFormat() {
        return this.fileFormatDescription;
    }

    public DataSeriesSet series() {
        return seriesSet;
    }

    public void logError(String msg) {
        Logger.log(Logger.LogType.error, "Parser " + getName() + " [" + filename + "]: " + msg);
    }

    public void logWarning(String msg) {
        Logger.log(Logger.LogType.warning, "Parser " + getName() + " [" + filename + "]: " + msg);
    }

    public void logInfo(String msg) {
        Logger.log(Logger.LogType.info, "Parser " + getName() + " [" + filename + "]: " + msg);
    }

    public void logDebug(String msg) {
        Logger.log(Logger.LogType.debug, "Parser " + getName() + " [" + filename + "]: " + msg);
    }

    public void setFilename(String absfilename) {
        this.filename = absfilename;
        setTimestampFromFilename();
    }

    public void setFilename(String filename, String projectFileName) {
        this.projectFileName = projectFileName;
        String filesep = System.getProperty("file.separator");
        if (!filename.startsWith(filesep) &&
            (filename.length() < 2 || filename.charAt(1) != ':')) {
            this.filename = Util.getPathOfFile(projectFileName) + filesep + filename;
        } else {
            this.filename = filename;
        }
        setTimestampFromFilename();
    }
    
    private void setTimestampFromFilename() {
        if (filename != null) {
            Pattern p = Pattern.compile(
                    ".*[^0-9]([0-9][0-9][0-9][0-9])([0-9][0-9])([0-9][0-9])([0-9][0-9])([0-9][0-9])([0-9][0-9])[^0-9].*");
            Matcher m = p.matcher(filename);
            if (m.matches()) {
                timestamp.set(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 
                        Integer.parseInt(m.group(4)), Integer.parseInt(m.group(5)), Integer.parseInt(m.group(6)));
            }
        }
    }

    public void setProjectFilename(String projectFileName) {
        this.projectFileName = projectFileName;
    }
    
    public boolean parse(boolean forceReparseAll, boolean updateSeriesProperties) {
        if (getAllSeriesNames(forceReparseAll).length == 0) {
            return false;
        }
        if (!forceReparseAll) {
            for (DataSeries s : series().getAllSeries()) {
                if (s.isUsed() && !s.isParsed()) {
                    s.clearSamples();
                    s.setParsed(false);
                }
            }
        }
        timestamp.reset(tsOffset_hours, tsOffset_minutes, tsOffset_secs);
        boolean success = true;
        try {
            f = new BufferedReader(new FileReader(filename));
        } catch (Exception e) {
            logError("cannot open file '" + filename + "': " + e.getMessage());
            return false;
        }
        try {
            parse();
            for (DataSeries s : series().getAllSeries()) {
                if ( (s.isUsed() && !s.isParsed()) || s.getNumberOfSamples() > 0 ) {
                    DataSeriesProperties p = s.getDataProperties();
                    if (p != null && updateSeriesProperties && !s.isParsed()) {
                        // only overwrite any previously set series properties when we're asked to,
                        // and only if we haven't yet parsed this series.
                        // first criteria might be needless....? probably can be removed
                        p.setScaleMax(s.getPreferredScaleMaxValue());
                    }
                    s.setParsed(true);
                }
            }
        } catch (Exception e) {
            success = false;
            logError("Parse Error: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            f.close();
        } catch (Exception eignore) {
        }
        return success;
    }
    
    public void setAllUsed() {
        for (DataSeries s : series().getAllSeries()) {
            s.setUsed(true);
        }
    }

    public String[] getAllSeriesNames(boolean forceReparseAll) {
        if (!forceReparseAll && seriesSet.size() > 0) {
            return seriesSet.getAllSeriesNames();
        }
        seriesSet.rememberSelection();
        seriesSet.clearAll();
        try {
            f = new BufferedReader(new FileReader(filename));
        } catch (Exception e) {
            logError("cannot open file '" + filename + "': " + e.getMessage());
            return null;
        }
        try {
            createAllSeries();
            seriesSet.restoreSelection();
            return seriesSet.getAllSeriesNames();
        } catch (Exception e) {
            logError("Parse Error: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            f.close();
        } catch (Exception eignore) {
        }
        return null;
    }

    protected String readLine() {
        return readLine(false);
    }
    
    protected String readLine(boolean trimTime) {
        String s = null;
        try {
            s = (f != null ? f.readLine() : null);
            if (s != null) {
                return timestamp.getTimeStampFromLine(s, newSamplesHeader, interval, trimTime);
            }
            return s;
        } catch (Exception e) {
            Logger.log(Logger.LogType.error, e.toString());
            return s;
        }
    }

    protected void fmark(int i) throws IOException {
        f.mark(i);
    }

    protected void freset() throws IOException {
        f.reset();
    }

    public TimeStamp getCurrentTimeStamp() {
        return timestamp;
    }

    protected void setNewSamplesHeader(String header) {
        newSamplesHeader = Pattern.compile(".*" + header + ".*");
    }

    protected void setNewSamplesHeaderPattern(String pattern) {
        newSamplesHeader = Pattern.compile(pattern);
    }

    public String getMatchingStringFromFile(Pattern pattern) {
        String str = null;
        try {
            f.mark(10000);
            String s;
            s = f.readLine();
            for (int cnt = 0; cnt < 10 && s != null; cnt++) {
                Matcher m = pattern.matcher(s);
                if (m.matches()) {
                    str = m.group(1);
                    s = null;
                } else {
                    s = f.readLine();
                }
            }
            f.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public String getMatchingStringFromFile(String pattern) {
        String str = null;
        try {
            Pattern p = Pattern.compile(pattern);
            return getMatchingStringFromFile(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public Matcher getMatchingMatcherFromFile(String pattern) {
        Matcher m = null;
        try {
            Pattern p = Pattern.compile(pattern);
            f.mark(10000);
            String s;
            s = f.readLine();
            for (int cnt = 0; cnt < 10 && s != null; cnt++) {
                m = p.matcher(s);
                if (m.matches()) {
                    s = null;
                } else {
                    m = null;
                    s = f.readLine();
                }
            }
            f.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }
    
    protected double[] getValuesFromMatcher(Matcher m) {
        if (m.matches()) {
            double[] values = new double[m.groupCount()];
            for (int i=0; i<values.length; i++) {
                try {
                    values[i] = Double.parseDouble(m.group(i+1));
                } catch(Exception e) {
                }
            }
            return values;
        }
        return null;
    }

    /**
     * For Columns with fixed Widths, separated by Strings, get the start index of each Column
     * Example: "           ExecuteQueueRuntime  count  count   count    count"
     * returns: [0,         11,                  32,    39,     47,      56 ]
     * @param s the string 
     * @return the column positions
     */
    protected int[] getColumnPositionsFromString(String s, boolean alignedRight) {
        Vector<Integer> pos = new Vector<Integer>();
        int step = 0;

        if (s.length() > 0) {
            pos.add(0);
        }

        for (int i = 0; i < s.length(); i++) {
            if (alignedRight) {
                // right aligned
                // step=0 - in leading spaces
                // step=1 - in characters
                if (step == 0 && s.charAt(i) != ' ' && s.charAt(i) != '\t') {
                    step = 1;
                    continue;
                }
                if (step == 1 && (s.charAt(i) == ' ' || s.charAt(i) == '\t')) {
                    pos.add(i);
                    step = 0;
                }
            } else {
                // left aligned
                // step=0 - in characters
                // step=1 - in trailing spaces
                if (step == 0 && (s.charAt(i) == ' ' || s.charAt(i) == '\t')) {
                    step = 1;
                    continue;
                }
                if (step == 1 && s.charAt(i) != ' ' && s.charAt(i) != '\t') {
                    pos.add(i);
                    step = 0;
                }
            }
        }
        int[] positions = new int[pos.size()];
        for (int i = 0; i < pos.size(); i++) {
            positions[i] = pos.get(i);
        }
        return positions;
    }

    protected String[] splitStringByColumns(String s, int[] pos) {
        if (s == null) {
            return null;
        }
        String[] cols = new String[pos.length];
        for (int i = 0; i < pos.length; i++) {
            try {
                cols[i] = s.substring(pos[i], (i + 1 < pos.length ? pos[i + 1] : s.length()));
                if (cols[i] != null) {
                    cols[i] = cols[i].trim();
                }
            } catch (Exception e) {
                cols[i] = null;
            }
        }
        return cols;
    }

    public String[] getParameterNames() {
        String[] keys = parameters.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        return keys;
    }

    public void setParameter(String name, String value) {
        if (name == null || value == null) {
            return;
        }
        parameters.put(name, value);
        if (PARAM_INTERVAL.equals(name)) {
            try {
                interval = Long.parseLong(value.trim());
            } catch (Exception e) {
                interval = 0;
            }
        }
        if (PARAM_TIME_OFFSET_HH.equals(name)) {
            try {
                tsOffset_hours = Long.parseLong(value.trim());
            } catch (Exception e) {
                tsOffset_hours = 0;
            }
        }
        if (PARAM_TIME_OFFSET_MM.equals(name)) {
            try {
                tsOffset_minutes = Long.parseLong(value.trim());
            } catch (Exception e) {
                tsOffset_minutes = 0;
            }
        }
        if (PARAM_TIME_OFFSET_SS.equals(name)) {
            try {
                tsOffset_secs = Long.parseLong(value.trim());
            } catch (Exception e) {
                tsOffset_secs = 0;
            }
        }
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(XMLHelper.xmlTagStart(XML_PARSER));
        s.append(XMLHelper.xmlTag(XML_PARSER_CLASS, getClass().getCanonicalName()));
        s.append(XMLHelper.xmlTag(XML_FILENAME, getRelativeFilename()));
        s.append(XMLHelper.xmlTag(XML_NAME, getName()));
        String[] params = getParameterNames();
        for (int p = 0; params != null && p < params.length; p++) {
            s.append(XMLHelper.xmlTagStart(XML_PARSER_PARAMETER));
            s.append(XMLHelper.xmlTag(XML_PARSER_PARAM_NAME, params[p]));
            s.append(XMLHelper.xmlTag(XML_PARSER_PARAM_VALUE, getParameter(params[p])));
            s.append(XMLHelper.xmlTagEnd(XML_PARSER_PARAMETER));
        }
        s.append(XMLHelper.xmlTagEnd(XML_PARSER));
        return s.toString();
    }

    public static Parser restoreParser(Element parserElement, String projectFileName) {
        Element e = ProjectFile.getChildNode(parserElement, XML_PARSER_CLASS);
        String className = (e != null ? e.getTextContent().trim() : null);
        if (className == null) {
            return null;
        }
        Parser p = Parsers.createParser(className);
        if (p == null) {
            return null;
        }
        e = ProjectFile.getChildNode(parserElement, XML_FILENAME);
        if (e != null) {
            p.setFilename(e.getTextContent().trim(), projectFileName);
            p.getAllSeriesNames(true);
        }
        e = ProjectFile.getChildNode(parserElement, XML_NAME);
        if (e != null) {
            p.setName(e.getTextContent().trim());
        }
        NodeList nl = parserElement.getChildNodes();
        for (int i = 0; nl != null && i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                e = (Element) n;
                if (XML_PARSER_PARAMETER.equals(e.getNodeName())) {
                    Element pn = ProjectFile.getChildNode(e, XML_PARSER_PARAM_NAME);
                    Element pv = ProjectFile.getChildNode(e, XML_PARSER_PARAM_VALUE);
                    if (pn != null && pv != null) {
                        p.setParameter(pn.getTextContent().trim(), pv.getTextContent().trim());
                    }
                }
            }
        }
        return p;
    }
    
    protected long getOffsetSeconds() {
        return tsOffset_hours*3600 + tsOffset_minutes*60 + tsOffset_secs;
    }
    
    public void registerSeriesPattern(String category, String subcategory, String series, Pattern p, boolean base1024units) {
        patterns.put(p, new SeriesName(category, subcategory, series, base1024units, 1));
    }
    
    public void registerSeriesPattern(String category, String subcategory, String series, Pattern p, double factor) {
        patterns.put(p, new SeriesName(category, subcategory, series, false, factor));
    }
    
    public DataSeries addSampleForRegisteredPatterns(long ts, String s, boolean onlyIfNeeded) {
        Matcher m;
        for (Pattern p : patterns.keySet()) {
            m = p.matcher(s);
            if (m.matches()) {
                SeriesName sn = patterns.get(p);
                DataSeries ds = series().getOrAddSeries(sn.category, sn.subcategory, sn.series);
                for (int i=0; i<m.groupCount(); i++) {
                    if (onlyIfNeeded) {
                        ds.addSampleIfNeeded(ts, Util.parseDouble(m.group(i+1), sn.base1024units) * sn.scaleFactor);
                    } else {
                        ds.addSample(ts, Util.parseDouble(m.group(i+1), sn.base1024units) * sn.scaleFactor);
                    }
                }
                return ds;
            }
        }
        return null;
    }
    
    public int addAllSamplesForRegisteredPatterns(long ts, String s, boolean onlyIfNeeded) {
        int c = 0;
        Matcher m;
        for (Pattern p : patterns.keySet()) {
            m = p.matcher(s);
            if (m.matches()) {
                SeriesName sn = patterns.get(p);
                DataSeries ds = series().getOrAddSeries(sn.category, sn.subcategory, sn.series);
                for (int i=0; i<m.groupCount(); i++) {
                    if (onlyIfNeeded) {
                        ds.addSampleIfNeeded(ts, Util.parseDouble(m.group(i+1), sn.base1024units) * sn.scaleFactor);
                    } else {
                        ds.addSample(ts, Util.parseDouble(m.group(i+1), sn.base1024units) * sn.scaleFactor);
                    }
                }
                c++;
            }
        }
        return c;
    }

}
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

import de.nmichael.pda.util.Util;
import java.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataSeriesGroup extends DataSeries {
    
    public static final String GROUP_PARSER_NAME = "group";

    public static final String XML_GROUP              = "Group";
    public static final String XML_CATEGORYNAME       = "CategoryName";
    public static final String XML_SUBCATEGORYNAME    = "SubcategoryName";
    public static final String XML_SERIESNAME         = "SeriesName";
    public static final String XML_FUNCTION           = "Function";
    public static final String XML_MEMBERS            = "MemberSeries";
    public static final String XML_MEMBER             = "Series";
    
    public static final String[] FUNCTIONS = { "Sum", "Average", "Maximum", "Minimum", "Variance" };
    public static final int FUNC_SUM = 0;
    public static final int FUNC_AVG = 1;
    public static final int FUNC_MAX = 2;
    public static final int FUNC_MIN = 3;
    public static final int FUNC_VAR = 4;

    private int function;
    private ArrayList<DataSeries> memberSeries = new ArrayList<DataSeries>();
    private ArrayList<String> memberPatterns = new ArrayList<String>();
    
    public DataSeriesGroup(String categoryName, String subcategroyName, String seriesName) {
        super(GROUP_PARSER_NAME, categoryName, subcategroyName, seriesName);
        function = FUNC_SUM;
        clearMembers();
    }
    
    public void setFunction(int function) {
        this.function = function;
    }
    
    public void setFunction(String function) {
        setFunction(Util.searchArray(FUNCTIONS, function, FUNC_SUM));
    }
    
    public int getFunction() {
        return function;
    }
    
    public boolean addMember(DataSeries series) {
        if (!memberSeries.contains(series)) {
            memberSeries.add(series);
            return true;
        }
        return false;
    }
    
    public boolean isMember(String seriesName) {
        for (DataSeries s : memberSeries) {
            if (s.getName().equals(seriesName)) {
                return true;
            }
        }
        return false;
    }
    
    public void addMemberPattern(String patternString) {
        if (!memberPatterns.contains(patternString)) {
            memberPatterns.add(patternString);
        }
    }
    
    public int getNumerOfMembers() {
        return memberSeries.size();
    }
    
    public DataSeries getMember(int idx) {
        return memberSeries.get(idx);
    }
    
    public ArrayList<DataSeries> getAllMembers() {
        return memberSeries;
    }
    
    public void removeMember(int idx) {
        memberSeries.remove(idx);
    }
    
    public void removeMember(DataSeries series) {
        for (int i=0; i<memberSeries.size(); i++) {
            if (memberSeries.get(i).getName().equals(series.getName())) {
                memberSeries.remove(i);
                return;
            }
        }
    }
    
    public void clearMembers() {
        memberSeries = new ArrayList<DataSeries>();
        memberPatterns = new ArrayList<String>();
    }
    
    @Override
    public boolean isParsed() {
        if (!super.isParsed()) {
            return false;
        }
        for (DataSeries s : memberSeries) {
            if (!s.isParsed()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setUsed(boolean used) {
        for (DataSeries s : memberSeries) {
            s.setUsed(used);
        }
        super.setUsed(used);
    }

    @Override
    public ArrayList<Parser> getDirtyParsers() {
        if (isUsed() && !isParsed()) {
            ArrayList<Parser> plist = new ArrayList<Parser>();
            for (DataSeries s : getAllMembers()) {
                s.setUsed(true); // should already be set
                if (!s.isParsed()) {
                    plist.add(s.getParser());
                }
            }
            return plist;
        }
        return null;
    }
    
    private double getEmptyAggregationValue() {
        switch (function) {
            case FUNC_SUM:
            case FUNC_AVG:
            case FUNC_VAR:
                return 0;
            case FUNC_MIN:
                return Double.MAX_VALUE;
            case FUNC_MAX:
                return Double.MIN_VALUE;
        }
        return 0;
    }
    
    private int findNextSample(int[] index) {
        long tsmin = Long.MAX_VALUE;
        int idx = -1;
        for (int i=0; i<index.length; i++) {
            Sample s = memberSeries.get(i).getSample(index[i]);
            if (s != null && s.getTimeStamp() < tsmin) {
                tsmin = s.getTimeStamp();
                idx = i;
            }
        }
        return idx;
    }
    
    protected void rebuildSamples(long tolerance) {
        try {
            clearSamples();
            int[] index = new int[memberSeries.size()];
            long ts = 0;
            double v = getEmptyAggregationValue();
            double v2 = 0;
            int count = 0;
            do {
                int midx = findNextSample(index);
                if (midx < 0) {
                    break;
                }
                Sample s = memberSeries.get(midx).getSample(index[midx]++);
                if (s != null) {
                    if (ts / tolerance != s.getTimeStamp() / tolerance) {
                        if (count > 0) {
                            if (function == FUNC_AVG) {
                                v = v / (double)count;
                            }
                            if (function == FUNC_VAR) {
                                if (count < 2) {
                                    v = 0.0;
                                }
                                double avg = v / (double) count;
                                v = (v2 - (double) count * avg * avg) / (double) (count - 1);
                            }
                            addSample(new Sample(ts, v));
                        }
                        ts = s.getTimeStamp();
                        v = getEmptyAggregationValue();
                        v2 = 0;
                        count = 0;
                    }
                    switch (function) {
                        case FUNC_SUM:
                        case FUNC_AVG:
                            v += s.getValue();
                            break;
                        case FUNC_MIN:
                            v = Math.min(s.getValue(), v);
                            break;
                        case FUNC_MAX:
                            v = Math.max(s.getValue(), v);
                            break;
                        case FUNC_VAR:
                            v += s.getValue(); // sum
                            v2 += s.getValue() * s.getValue(); // square value for variance
                            break;
                    }
                    count++;
                }
            } while(true);
            if (count > 0) {
                if (function == FUNC_AVG) {
                    v = v / (double) count;
                }
                addSample(new Sample(ts, v));
            }
            if (getNumberOfSamples() > 0) {
                setParsed(true);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getPreferredScaleMinValue() {
        double min = Double.MAX_VALUE;
        for (DataSeries s : getAllMembers()) {
            min = Math.min(s.getPreferredScaleMinValue(), min);
        }
        min = Math.min(super.getPreferredScaleMinValue(), min);
        return (min != Double.MAX_VALUE ? min : super.getPreferredScaleMinValue());
    }

    @Override
    public double getPreferredScaleMaxValue() {
        double max = 0;
        for (DataSeries s : getAllMembers()) {
            max = Math.max(s.getPreferredScaleMaxValue(), max);
        }
        max = Math.max(super.getPreferredScaleMaxValue(), max);
        return (max > 0 ? max : super.getPreferredScaleMaxValue());
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(XMLHelper.xmlTagStart(XML_GROUP));
        s.append(XMLHelper.xmlTag(XML_CATEGORYNAME, getCategoryName()));
        s.append(XMLHelper.xmlTag(XML_SUBCATEGORYNAME, getSubcategoryName()));
        s.append(XMLHelper.xmlTag(XML_SERIESNAME, getSeriesName()));
        s.append(XMLHelper.xmlTag(XML_FUNCTION, FUNCTIONS[function]));
        s.append(XMLHelper.xmlTagStart(XML_MEMBERS));
        for (int i=0; i<memberSeries.size(); i++) {
            s.append(XMLHelper.xmlTag(XML_MEMBER, memberSeries.get(i).getName()));
        }
        s.append(XMLHelper.xmlTagEnd(XML_MEMBERS));
        s.append(XMLHelper.xmlTagEnd(XML_GROUP));
        return s.toString();
    }
    
    static DataSeriesGroup restoreGroup(Element groupElement, ProjectItem pi) {
        Element e = ProjectFile.getChildNode(groupElement, XML_CATEGORYNAME);
        String categoryName = (e != null ? e.getTextContent().trim() : "");
        e = ProjectFile.getChildNode(groupElement, XML_SUBCATEGORYNAME);
        String subcategoryName = (e != null ? e.getTextContent().trim() : "");
        e = ProjectFile.getChildNode(groupElement, XML_SERIESNAME);
        String seriesName = (e != null ? e.getTextContent().trim() : "");
        DataSeriesGroup g = new DataSeriesGroup(categoryName, subcategoryName, seriesName);
        if (g != null) {
            e = ProjectFile.getChildNode(groupElement, XML_FUNCTION);
            if (e != null) {
                g.setFunction(e.getTextContent().trim());
            }
            e = ProjectFile.getChildNode(groupElement, XML_MEMBERS);
            if (e != null) {
                NodeList nl = e.getChildNodes();
                for (int i = 0; nl != null && i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        e = (Element) n;
                        if (XML_MEMBER.equals(e.getNodeName())) {
                            DataSeries s = pi.getSeries(e.getTextContent().trim());
                            if (s != null) {
                                g.addMember(s);
                            }
                        }
                    }
                }
            }
        }
        return g;
    }

}

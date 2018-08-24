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

import java.util.*;
import de.nmichael.pda.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProjectItem {
    
    public static final String XML_PROJECT_ITEM       = "ProjectItem";
    public static final String XML_PROJECT_ITEM_TYPE  = "Type";
    public static final String XML_PROJECT_ITEM_GRAPH = "Graph";
    public static final String XML_PROJECT_ITEM_TABLE = "Table";
    public static final String XML_PROJECT_ITEM_LOAD  = "Load";
    public static final String XML_NAME               = "Name";
    public static final String XML_SCALE              = "Scale";
    public static final String XML_SCALE_MINX         = "ScaleMinX";
    public static final String XML_SCALE_MAXX         = "ScaleMaxX";
    public static final String XML_PRINTIMAGE         = "PrintImage";
    public static final String XML_DATA_PROPERTIES    = "DataProperties";
    public static final String XML_DATA_GROUPS        = "DataGroups";

    private String baseFilename;
    private ColorSelector colorSelector = new ColorSelector();
    
    public enum Type {
        graph,
        table,
        load
    }

    private Type type;
    private String name = "";
    private ParserSet parserSet = new ParserSet();
    private DataSeriesPropertySet propertySet = new DataSeriesPropertySet();
    private DataSeriesGroupSet groupSet = new DataSeriesGroupSet();
    private long scaleMinXexplicitlySet = -1;
    private long scaleMaxXexplicitlySet = -1;
    private String pngFile = null;

    public ProjectItem(Type type) {
        this.type = type;
    }
    

    public void setType(Type type) {
        this.type = type;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void setBaseFileName(String baseFilename) {
        this.baseFilename = baseFilename;
    }
    
    public ParserSet getParsers() {
        return parserSet;
    }
    
    public void addParser(Parser p) {
        parserSet.addParser(p);
    }
    
    public DataSeriesPropertySet getSeriesProperties() {
        return propertySet;
    }
    
    public DataSeriesGroupSet getGroups() {
        return groupSet;
    }
    
    public boolean isScaleXSet() {
        return (scaleMinXexplicitlySet != -1 || scaleMaxXexplicitlySet != -1);
    }
    
    public long getScaleMinX() {
        if (scaleMinXexplicitlySet != -1) {
            return scaleMinXexplicitlySet;
        }
        long min = Long.MAX_VALUE;
        for (int i=0; i<propertySet.size(); i++) {
            DataSeries s = propertySet.getDataProperties(i).getSeries();
            if (s != null) {
                long mins = s.getFirstTimestamp();
                if (mins > 0) {
                    min = Math.min(mins, min);
                }
            }
        }
        return min;
    }
    
    public long getScaleMaxX() {
        if (scaleMaxXexplicitlySet != -1) {
            return scaleMaxXexplicitlySet;
        }
        long max = 0;
        for (int i=0; i<propertySet.size(); i++) {
            DataSeries s = propertySet.getDataProperties(i).getSeries();
            if (s != null) {
                long maxs = s.getLastTimestamp();
                if (maxs < Long.MAX_VALUE) {
                    max = Math.max(maxs, max);
                }
            }
        }
        return max;
    }
    
    public long getMinX(long preferredMinX) {
        long min = Long.MAX_VALUE;
        for (int i=0; i<propertySet.size(); i++) {
            DataSeries s = propertySet.getDataProperties(i).getSeries();
            if (s != null) {
                long mins = s.getFirstTimestamp();
                if (mins > 0) {
                    min = Math.min(mins, min);
                }
            }
        }
        return Math.max(min, preferredMinX);
    }
    
    public long getMaxX(long preferredMaxX) {
        long max = 0;
        for (int i=0; i<propertySet.size(); i++) {
            DataSeries s = propertySet.getDataProperties(i).getSeries();
            if (s != null) {
                long maxs = s.getLastTimestamp();
                if (maxs < Long.MAX_VALUE) {
                    max = Math.max(maxs, max);
                }
            }
        }
        return Math.min(max, preferredMaxX);
    }
    
    public void setScaleMinX(long minX) {
        scaleMinXexplicitlySet = minX;
    }
    
    public void setScaleMaxX(long maxX) {
        scaleMaxXexplicitlySet = maxX;
    }
    
    public void unsetScaleMinX() {
        scaleMinXexplicitlySet = -1;
    }
    
    public void unsetScaleMaxX() {
        scaleMaxXexplicitlySet = -1;
    }
    
    public void setPngFilename(String fname) {
        this.pngFile = fname;
    }
    
    public String getPngFilename() {
        return this.pngFile;
    }
    
    public DataSeries getSeries(DataSeriesProperties prop) {
        return getSeries(prop.getName());
    }
    
    public DataSeries getSeries(String name) {
        String parserName = DataSeries.getParserName(name);
        String categoryName = DataSeries.getCategoryName(name);
        String subcategoryName = DataSeries.getSubcategoryName(name);
        String seriesName = DataSeries.getSeriesName(name);
        if (!parserName.equals(DataSeriesGroup.GROUP_PARSER_NAME)) {
            Parser p = parserSet.getParser(parserName);
            if (p != null) {
                return p.series().getSeries(categoryName, subcategoryName, seriesName);
            }
        } else {
            return groupSet.getSeriesGroup(categoryName, subcategoryName, seriesName);
        }
        return null;
    }
    
    public DataSeries[] getAllSeries(boolean withGroups) {
        ArrayList<DataSeries> all = new ArrayList<DataSeries>();
        for (int i=0; i<parserSet.size(); i++) {
            Parser p = parserSet.getParser(i);
            all.addAll(p.series().getAllSeries());
        }
        if (withGroups) {
            for (int i = 0; i < groupSet.size(); i++) {
                all.add(groupSet.getSeriesGroup(i));
            }
        }
        DataSeries[] arr = all.toArray(new DataSeries[0]);
        Arrays.sort(arr);
        return arr;
    }

    public DataSeriesSet getDataSeriesSet(boolean onlySelected) {
        DataSeriesSet set = new DataSeriesSet(null);
        ParserSet pset = getParsers();
        for (int i=0; i<pset.size(); i++) {
            set.add(pset.getParser(i).series());
        }
        return set;
    }

    public String[] getAllSeriesNames(boolean withGroups) {
        DataSeries[] series = getAllSeries(withGroups);
        String[] names = new String[ (series != null ? series.length : 0) ];
        for (int i=0; i<names.length; i++) {
            names[i] = series[i].getName();
        }
        return names;
    }
    
    public ColorSelector getColorSelector() {
        return colorSelector;
    }
    
    public void removeParser(Parser p) {
        parserSet.removeParser(p);
        propertySet.removeDataPropertiesForParser(p.getName());
    }
    
    public String toString() {
        StringBuilder s = new StringBuilder();
        // ProjectItem Type
        ArrayList<String> paramNames = new ArrayList<String>();
        ArrayList<String> paramValues = new ArrayList<String>();
        if (getType() == ProjectItem.Type.graph) {
            paramNames.add(XML_PROJECT_ITEM_TYPE);
            paramValues.add(XML_PROJECT_ITEM_GRAPH);
        }
        if (getType() == ProjectItem.Type.table) {
            paramNames.add(XML_PROJECT_ITEM_TYPE);
            paramValues.add(XML_PROJECT_ITEM_TABLE);
        }
        if (getType() == ProjectItem.Type.load) {
            paramNames.add(XML_PROJECT_ITEM_TYPE);
            paramValues.add(XML_PROJECT_ITEM_LOAD);
        }
        s.append(XMLHelper.xmlTagStart(XML_PROJECT_ITEM,
                paramNames.toArray(new String[0]),
                paramValues.toArray(new String[0])));

        // Name
        s.append(XMLHelper.xmlTag(XML_NAME, getName()));

        // Scale
        if (isScaleXSet()) {
            s.append(XMLHelper.xmlTagStart(XML_SCALE));
            s.append(XMLHelper.xmlTag(XML_SCALE_MINX, Long.toString(getScaleMinX())));
            s.append(XMLHelper.xmlTag(XML_SCALE_MAXX, Long.toString(getScaleMaxX())));
            s.append(XMLHelper.xmlTagEnd(XML_SCALE));
        }

        // Image
        if (getPngFilename() != null) {
            s.append(XMLHelper.xmlTag(XML_PRINTIMAGE, getPngFilename()));
        }

        ParserSet parserSet = getParsers();
        for (int j = 0; j < parserSet.size(); j++) {
            Parser p = parserSet.getParser(j);
            p.setProjectFilename(baseFilename);
            s.append(p.toString());
        }

        DataSeriesGroupSet groupSet = getGroups();
        for (int j = 0; j < groupSet.size(); j++) {
            DataSeriesGroup g = groupSet.getSeriesGroup(j);
            s.append(g.toString());
        }

        DataSeriesPropertySet seriesSet = getSeriesProperties();
        for (int j = 0; j < seriesSet.size(); j++) {
            DataSeriesProperties sp = seriesSet.getDataProperties(j);
            s.append(sp.toString());
        }

        s.append(XMLHelper.xmlTagEnd(XML_PROJECT_ITEM));

        return s.toString();

    }

    static ProjectItem restoreProjectItem(Element projectElement, String projectFileName) {
        String stype = projectElement.getAttribute(XML_PROJECT_ITEM_TYPE);
        ProjectItem.Type type = ProjectItem.Type.graph;
        if (XML_PROJECT_ITEM_GRAPH.equals(stype)) {
            type = ProjectItem.Type.graph;
        }
        if (XML_PROJECT_ITEM_TABLE.equals(stype)) {
            type = ProjectItem.Type.table;
        }
        if (XML_PROJECT_ITEM_LOAD.equals(stype)) {
            type = ProjectItem.Type.load;
        }

        ProjectItem pi = new ProjectItem(type);
        NodeList nl = projectElement.getChildNodes();
        for (int i=0; nl != null && i<nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element)n;
                
                if (XML_NAME.equals(e.getNodeName())) {
                    pi.setName(e.getTextContent().trim());
                }
                
                if (XML_SCALE.equals(e.getNodeName())) {
                    Element se = ProjectFile.getChildNode(e, XML_SCALE_MINX);
                    pi.setScaleMinX(se != null ? Util.string2long(se.getTextContent().trim(), -1) : -1);
                    se = ProjectFile.getChildNode(e, XML_SCALE_MAXX);
                    pi.setScaleMaxX(se != null ? Util.string2long(se.getTextContent().trim(), -1) : -1);
                }
                
                if (XML_PRINTIMAGE.equals(e.getNodeName())) {
                    pi.setPngFilename(e.getTextContent().trim());
                }
                
                if (Parser.XML_PARSER.equals(e.getNodeName())) {
                    Parser p = Parser.restoreParser(e, projectFileName);
                    if (p != null) {
                        pi.parserSet.addParser(p);
                    }
                }
                
                if (DataSeriesGroup.XML_GROUP.equals(e.getNodeName())) {
                    DataSeriesGroup g = DataSeriesGroup.restoreGroup(e, pi);
                    if (g != null) {
                        pi.groupSet.addGroup(g);
                    }
                }
                
                if (DataSeriesProperties.XML_DATA_SERIES.equals(e.getNodeName())) {
                    DataSeriesProperties p = DataSeriesProperties.restoreProperties(e, pi);
                    if (p != null) {
                        pi.propertySet.addDataProperties(p);
                    }
                }
                
            }
        }
        pi.parserSet.parseAll();
        pi.getGroups().parseAllUpdate();

        return pi;
    }
}

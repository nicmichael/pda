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

import java.awt.Color;
import de.nmichael.pda.util.*;
import gov.noaa.pmel.sgt.LineAttribute;
import org.w3c.dom.Element;

public class DataSeriesProperties {
    
    public static final String XML_DATA_SERIES     = "DataSeries";
    public static final String XML_NAME            = "Name";
    public static final String XML_DISPLAYNAME     = "DisplayName";
    public static final String XML_COLOR           = "Color";
    public static final String XML_VISIBLE         = "Visible";
    public static final String XML_SCALE_MIN       = "ScaleMin";
    public static final String XML_SCALE_MAX       = "ScaleMax";
    public static final String XML_STYLE           = "Style";
    public static final String XML_LINE_WIDTH      = "LineWidth";
    public static final String XML_LINE_STYLE      = "LineStyle";
    public static final String XML_SMOOTH          = "Smooth";
    public static final String XML_VALUE_AXIS      = "ValueAxis";
    
    public static final String[] STYLES = { "Line" , "Points" , "Dots" , "Impulses" };
    public static final int STYLE_LINE = 0;
    public static final int STYLE_POINTS = 1;
    public static final int STYLE_DOTS = 2;
    public static final int STYLE_IMPULSES = 3;
    
    public static final String[] LINESTYLES = { "Solid" , "Dashed", "Strong" };
    public static final int LINESTYLE_SOLID = 0;
    public static final int LINESTYLE_DASHED = 1;
    public static final int LINESTYLE_STRONG = 2;
    
    public static final String[] VALUEAXIS = { "linear" , "logarithmic" };
    public static final int VALUEAXIS_LINEAR = 0;
    public static final int VALUEAXIS_LOGARITHMIC = 1;
    
    private DataSeries series;
    private String name;
    private String displayName;
    private Color color;
    private boolean visible;
    private double scaleMin;
    private double scaleMax;
    private int style;
    private int lineWidth = -1;
    private int lineStyle = -1;
    private int valueAxis;
    private int smooth;
    
    public DataSeriesProperties(DataSeries dataSeries) {
        this.series = dataSeries;
        name = series != null ? series.getName() : null;
        setDisplayName(series != null ? series.getLocalName() : null);
        color = Color.blue;
        visible = true;
        scaleMin = 0.0;
        scaleMax = 100.0;
        style = STYLE_LINE;
        lineWidth = 2;
        lineStyle = LINESTYLE_SOLID;
        valueAxis = VALUEAXIS_LINEAR;
        smooth = 1;
    }
    
    public DataSeriesProperties clone() {
        DataSeriesProperties prop = new DataSeriesProperties(series);
        prop.name = name;
        prop.displayName = displayName;
        prop.color = color;
        prop.visible = visible;
        prop.scaleMin = scaleMin;
        prop.scaleMax = scaleMax;
        prop.style = style;
        prop.lineWidth = lineWidth;
        prop.lineStyle = lineStyle;
        prop.valueAxis = valueAxis;
        prop.smooth = smooth;
        return prop;
    }
    
    protected void setSeries(DataSeries dataSeries) {
        this.series = dataSeries;
    }

    public String getParserName() {
        return series.getParserName();
    }
    
    public String getCategoryName() {
        return series.getCategoryName();
    }
    
    public String getSubcategoryName() {
        return series.getSubcategoryName();
    }

    public String getSeriesName() {
        return series.getSeriesName();
    }
    
    void updateName() {
        name = series.getName();
    }
    
    public String getName() {
        return name;
    }
    
    public String getParserFileSeriesName() {
        try {
            return getParserName() + "[" + getSeries().getParser().getRelativeFilename() + "]" +
                   ":" + getSeries().getLocalName();
        } catch(Exception e) {
            return getName();
        }
    }

    public void setDisplayName(String name) {
        this.displayName = name;
        if (name != null) {
            while (displayName.startsWith(DataSeries.SEPARATOR)) {
                displayName = displayName.substring(1);
            }
            while (displayName.endsWith(DataSeries.SEPARATOR)) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
        }
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public DataSeries getSeries() {
        return series;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setLineWidth(int width) {
        this.lineWidth = width;
        if (lineWidth > 1) {
            if (lineStyle == LINESTYLE_DASHED) {
                lineWidth = 1;
            } else {
                lineStyle = LINESTYLE_STRONG;
            }
        }
    }
    
    public int getLineWidth() {
        return (lineWidth > 0 ? lineWidth : 1);
    }
    
    public void setLineStyle(int lineStyle) {
        switch(lineStyle) {
            case LINESTYLE_SOLID:
            case LINESTYLE_STRONG:
                if (lineWidth > 1) {
                    this.lineStyle = LINESTYLE_STRONG;
                } else {
                    this.lineStyle = LINESTYLE_SOLID;
                }
                break;
            case LINESTYLE_DASHED:
                this.lineStyle = lineStyle;
                break;
        }
    }
    
    public void setLineStyle(String lineStyle) {
        setLineStyle(Util.searchArray(LINESTYLES, lineStyle, LINESTYLE_SOLID));
    }
    
    public int getLineStyle() {
        return (lineWidth > 1 ? 
                LineAttribute.HEAVY : 
                (lineStyle >= 0 ? lineStyle : LineAttribute.SOLID));
    }
    
    public void setScaleMin(double value) {
        scaleMin = value;
    }
    
    public void setScaleMax(double value) {
        scaleMax = value;
    }
    
    public double getScaleMin() {
        if (scaleMin != 0) { // @todo: was >= 0
            return scaleMin;
        } else {
            if (series != null) {
                double min = series.getPreferredScaleMinValue();
                if (min <= 0) {
                    // @todo min = series.getMinValue();
                }
                return min;
            } else {
                return 0;
            }
        }
    }
    
    public double getScaleMax() {
        if (scaleMax >= 0 && scaleMax < Double.MAX_VALUE) {
            return scaleMax;
        } else {
            if (series != null) {
                double max = series.getPreferredScaleMaxValue();
                if (max <= 0 || max == Double.MAX_VALUE) {
                    max = series.getMaxValue();
                }
                return max;
            } else {
                return Double.MAX_VALUE;
            }
        }
    }
    
    public void setStyle(int style) {
        this.style = style;
    }
    
    public void setStyle(String style) {
        setStyle(Util.searchArray(STYLES, style, STYLE_LINE));
    }
    
    public int getStyle() {
        return this.style;
    }
    
    public void setValueAxis(int valueAxis) {
        this.valueAxis = valueAxis;
    }
    
    public void setValueAxis(String valueAxis) {
        setValueAxis(Util.searchArray(VALUEAXIS, valueAxis, VALUEAXIS_LINEAR));
    }
    
    public int getValueAxis() {
        return this.valueAxis;
    }
    
    public void setSmooth(int smooth) {
        this.smooth = smooth;
    }
    
    public int getSmooth() {
        int s = this.smooth;
        if (s <= 1) s = 1;
        return s;
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(XMLHelper.xmlTagStart(XML_DATA_SERIES));
        s.append(XMLHelper.xmlTag(XML_NAME, name));
        s.append(XMLHelper.xmlTag(XML_DISPLAYNAME, displayName));
        s.append(XMLHelper.xmlTag(XML_COLOR, Util.getColor(color)));
        s.append(XMLHelper.xmlTag(XML_VISIBLE, Boolean.toString(visible)));
        s.append(XMLHelper.xmlTag(XML_SCALE_MIN, Double.toString(scaleMin)));
        s.append(XMLHelper.xmlTag(XML_SCALE_MAX, Double.toString(scaleMax)));
        s.append(XMLHelper.xmlTag(XML_STYLE, STYLES[style]));
        s.append(XMLHelper.xmlTag(XML_LINE_WIDTH, Integer.toString(lineWidth)));
        s.append(XMLHelper.xmlTag(XML_LINE_STYLE, LINESTYLES[lineStyle]));
        s.append(XMLHelper.xmlTag(XML_SMOOTH, Integer.toString(smooth)));
        s.append(XMLHelper.xmlTag(XML_VALUE_AXIS, VALUEAXIS[valueAxis]));
        s.append(XMLHelper.xmlTagEnd(XML_DATA_SERIES));
        return s.toString();
    }

    static DataSeriesProperties restoreProperties(Element groupElement, ProjectItem pi) {
        Element e = ProjectFile.getChildNode(groupElement, XML_NAME);
        String name = (e != null ? e.getTextContent().trim() : null);
        if (name == null) {
            return null;
        }
        DataSeries s = pi.getSeries(name);
        if (s == null) {
            return null;
        }
        s.setUsed(true);
        s.setSelected(true);
        DataSeriesProperties p = new DataSeriesProperties(s);
        s.setDataProperties(p);
        e = ProjectFile.getChildNode(groupElement, XML_DISPLAYNAME);
        if (e != null) {
            p.setDisplayName(e.getTextContent().trim());
        }
        e = ProjectFile.getChildNode(groupElement, XML_COLOR);
        if (e != null) {
            p.setColor(Util.getColor(e.getTextContent().trim()));
        }
        e = ProjectFile.getChildNode(groupElement, XML_VISIBLE);
        if (e != null) {
            p.setVisible(Boolean.parseBoolean(e.getTextContent().trim()));
        }
        e = ProjectFile.getChildNode(groupElement, XML_SCALE_MIN);
        if (e != null) {
            p.setScaleMin(Util.string2double(e.getTextContent().trim(), 0));
        }
        e = ProjectFile.getChildNode(groupElement, XML_SCALE_MAX);
        if (e != null) {
            p.setScaleMax(Util.string2double(e.getTextContent().trim(), Double.MAX_VALUE));
        }
        e = ProjectFile.getChildNode(groupElement, XML_STYLE);
        if (e != null) {
            p.setStyle(e.getTextContent().trim());
        }
        e = ProjectFile.getChildNode(groupElement, XML_LINE_WIDTH);
        if (e != null) {
            p.setLineWidth(Util.string2int(e.getTextContent().trim(), 1));
        }
        e = ProjectFile.getChildNode(groupElement, XML_LINE_STYLE);
        if (e != null) {
            p.setLineStyle(e.getTextContent().trim());
        }
        e = ProjectFile.getChildNode(groupElement, XML_SMOOTH);
        if (e != null) {
            p.setSmooth(Util.string2int(e.getTextContent().trim(), 1));
        }
        e = ProjectFile.getChildNode(groupElement, XML_VALUE_AXIS);
        if (e != null) {
            p.setValueAxis(e.getTextContent().trim());
        }
        return p;
    }
}

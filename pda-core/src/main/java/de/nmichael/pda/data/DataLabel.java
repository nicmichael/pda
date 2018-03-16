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
import java.awt.Color;
import java.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DataLabel {
    
    public static final String XML_LABEL              = "Label";
    public static final String XML_POSX               = "PosX";
    public static final String XML_POSY               = "PosY";
    public static final String XML_TEXT               = "Text";
    public static final String XML_COLOR              = "Color";
    public static final String XML_SIZE               = "size";

    private String text;
    private double px;
    private double py;
    private Color color;
    private double size;

    public DataLabel(String text, double px, double py, Color color, double size) {
        this.text = text;
        this.px = px;
        this.py = py;
        this.color= color;
        this.size = size;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
    
    public void setPX(double px) {
        this.px = px;
    }
  
    public double getPX() {
        return px;
    }

    public void setPY(double py) {
        this.py = py;
    }

    public double getPY() {
        return py;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setSize(double size) {
        this.size = size;
    }
    
    public double getSize() {
        return size;
    }

    public String toString() {
        if (text == null) {
            return "";
        }
        return XMLHelper.xmlTagStart(XML_LABEL) + 
               XMLHelper.xmlTag(XML_POSX, Double.toString(px)) +
               XMLHelper.xmlTag(XML_POSY, Double.toString(py)) +
               XMLHelper.xmlTag(XML_TEXT, text) +
               XMLHelper.xmlTag(XML_COLOR, Util.getColor(color)) +
               XMLHelper.xmlTag(XML_SIZE, Double.toString(size)) +
               XMLHelper.xmlTagEnd(XML_LABEL);
    }
    
    static ArrayList<DataLabel> restoreLabels(Element e) {
        ArrayList<DataLabel> labels = new ArrayList<DataLabel>();
        NodeList nl = e.getChildNodes();
        for (int i=0; nl != null && i<nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                e = (Element)n;
                if (XML_LABEL.equals(e.getNodeName())) {
                    Element el = ProjectFile.getChildNode(e, XML_TEXT);
                    String text = (el != null ? el.getTextContent().trim() : "");
                    el = ProjectFile.getChildNode(e, XML_POSX);
                    double x = (el != null ? Util.string2double(el.getTextContent().trim(), 0) : 0);
                    el = ProjectFile.getChildNode(e, XML_POSY);
                    double y = (el != null ? Util.string2double(el.getTextContent().trim(), 0) : 0);
                    el = ProjectFile.getChildNode(e, XML_COLOR);
                    Color c = (el != null ? Util.getColor(el.getTextContent().trim()) : Color.black);
                    el = ProjectFile.getChildNode(e, XML_SIZE);
                    double size = (el != null ? Util.string2double(el.getTextContent().trim(), 0.2) : 0.2);
                    labels.add(new DataLabel(text, x, y, c, size));
                }
            }
        }
        return labels;
    }
    
}

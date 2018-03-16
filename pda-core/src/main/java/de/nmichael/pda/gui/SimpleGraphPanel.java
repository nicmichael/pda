/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2013 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.gui;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import de.nmichael.pda.data.*;

public class SimpleGraphPanel extends JPanel {
/*
    Vector _data = new Vector();
    Vector _category = new Vector();
    Vector _series = new Vector();
    Vector _names = new Vector();
    Vector _properties = new Vector();
    
    int sizeX = 0;
    int sizeY = 0;
    double minX = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = Double.MIN_VALUE;
    double rangeX = 0;
    double rangeY = 0;
    
    private int getXValue(Sample sample) {
        return (int)((((sample.getTimeStamp() - minX) * sizeX) / rangeX) + 10);
    }

    private int getYValue(Sample sample) {
        return (int)((sizeY - (((sample.getValue() - minY) * sizeY) / rangeY)) + 10);
    }
    
    public void paint(Graphics gg) {
        Graphics2D g = (Graphics2D)gg;
        
        sizeX = (int)this.getSize().getWidth() - 20;
        sizeY = (int)this.getSize().getHeight() - 20;
        
        g.setBackground(Color.white);
        g.setColor(Color.white);
        g.clearRect(10, 10, sizeX, sizeY);

        rangeX = (maxX - minX) + 1;
        int ytext = 30;
        for (int i=0; i<_data.size(); i++) {
            IDataSet data = (IDataSet)_data.get(i);
            int c = ((Integer)_category.get(i)).intValue();
            int s = ((Integer)_series.get(i)).intValue();
            String name = (String)_names.get(i);
            DataProperties prop = (DataProperties)_properties.get(i);
            
            minY = prop.getScaleMin();
            maxY = prop.getScaleMax();
            rangeY = (maxY - minY) + 1;
            
            g.setColor(prop.getColor());
            
            g.drawString(name + " [" + prop.getScaleMin() + ":" + prop.getScaleMax() + "]",20,ytext);
            ytext += 15;
            
            switch (prop.getStyle()) {
                case DataProperties.STYLE_LINE: drawLines(g,data,c,s); break;
                case DataProperties.STYLE_POINTS: drawPoints(g,data,c,s); break;
                case DataProperties.STYLE_DOTS: drawDots(g,data,c,s); break;
                case DataProperties.STYLE_IMPULSES: drawImpulses(g,data,c,s); break;
            }
            
        }
    }
    
    private void drawLines(Graphics2D g, IDataSet data, int c, int s) {
        int x1,y1,x2,y2;
        data.gotoFirstSample(c,s);
        Sample sample = data.getNextSample(c,s);
        if (sample == null) return;
        x1 = getXValue(sample);
        y1 = getYValue(sample);
        while ( (sample = data.getNextSample(c,s)) != null) {
            x2 = getXValue(sample);
            y2 = getYValue(sample);
            g.drawLine(x1,y1,x2,y2);
            x1 = x2;
            y1 = y2;
        }
    }
    
    private void drawPoints(Graphics2D g, IDataSet data, int c, int s) {
        int x,y;
        data.gotoFirstSample(c,s);
        Sample sample = data.getNextSample(c,s);
        if (sample == null) return;
        do {
            x = getXValue(sample);
            y = getYValue(sample);
            g.drawLine(x-2,y-2,x+2,y+2);
            g.drawLine(x-2,y+2,x+2,y-2);
        } while ( (sample = data.getNextSample(c,s)) != null);
    }

    private void drawDots(Graphics2D g, IDataSet data, int c, int s) {
        int x,y;
        data.gotoFirstSample(c,s);
        Sample sample = data.getNextSample(c,s);
        if (sample == null) return;
        do {
            x = getXValue(sample);
            y = getYValue(sample);
            g.drawLine(x,y,x,y);
        } while ( (sample = data.getNextSample(c,s)) != null);
    }

    private void drawImpulses(Graphics2D g, IDataSet data, int c, int s) {
        int x,y;
        data.gotoFirstSample(c,s);
        Sample sample = data.getNextSample(c,s);
        if (sample == null) return;
        do {
            x = getXValue(sample);
            y = getYValue(sample);
            g.drawLine(x,sizeY-10,x,y);
        } while ( (sample = data.getNextSample(c,s)) != null);
    }

    public void plotData(IDataSet data, int c, int s, String name, DataProperties prop) {
        if (data.getFirstSample(c,s).getTimeStamp() < minX) minX = data.getFirstSample(c,s).getTimeStamp();
        if (data.getLastSample(c,s).getTimeStamp() > maxX) maxX = data.getLastSample(c,s).getTimeStamp();
        
        _data.add(data);
        _category.add(new Integer(c));
        _series.add(new Integer(s));
        _names.add(name);
        _properties.add(prop);
    }
    
    
*/
}

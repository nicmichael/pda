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

import java.util.*;
import de.nmichael.pda.data.*;

public class DataSeriesPropertySet {
    
    private Vector<DataSeriesProperties> seriesProperties;
    
    /** Creates a new instance of PropertySet */
    public DataSeriesPropertySet() {
        seriesProperties = new Vector<DataSeriesProperties>();
    }
    
    public void addDataProperties(DataSeriesProperties prop) {
        // check for duplicates
        for (int i=0; i<seriesProperties.size(); i++) {
            if (seriesProperties.get(i).getName().equals(prop.getName())) {
                seriesProperties.remove(i);
                seriesProperties.add(i, prop);
                return;
            }
        }
        seriesProperties.add(prop);
    }
    
    public void insertDataProperties(DataSeriesProperties prop, int i) {
        if (i<0 || i>size()) return;
        seriesProperties.insertElementAt(prop,i);
    }
    
    public DataSeriesProperties getDataProperties(int i) {
        if (i<0 || i>=seriesProperties.size()) return null;
        return seriesProperties.get(i);
    }
    
    public DataSeriesProperties getDataProperties(String name) {
        for (int i=0; i<seriesProperties.size(); i++) {
            if (name.equals(seriesProperties.get(i).getName())) {
                return seriesProperties.get(i);
            }
        }
        return null;
    }
    
    public void removeDataProperties(int i) {
        if (i>= 0 && i<size()) seriesProperties.remove(i);
    }
    
    public void removeDataProperties(DataSeriesProperties p) {
        if (p != null) {
            if (p.getSeries() != null) {
                p.getSeries().setSelected(false);
                p.getSeries().setDataProperties(null);
            }
            seriesProperties.remove(p);
        }
    }
    
    public void removeDataPropertiesForParser(String parser) {
        if (parser != null) {
            for (int i=0; i<seriesProperties.size(); i++) {
                if (parser.equals(seriesProperties.get(i).getParserName())) {
                    seriesProperties.remove(i--);
                }
            }
        }
    }
    
    public int size() {
        return seriesProperties.size();
    }
    
    public void moveUp(DataSeriesProperties p) {
        for (int i=1; i<seriesProperties.size(); i++) {
            if (p == seriesProperties.get(i)) {
                seriesProperties.remove(i);
                seriesProperties.add(i-1, p);
                return;
            }
        }
    }
    
    public void moveDown(DataSeriesProperties p) {
        for (int i=0; i<seriesProperties.size()-1; i++) {
            if (p == seriesProperties.get(i)) {
                seriesProperties.remove(i);
                seriesProperties.add(i+1, p);
                return;
            }
        }
    }

}

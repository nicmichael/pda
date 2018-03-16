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

public class DataSeriesGroupSet {
    
    private Vector<DataSeriesGroup> groups;
    
    /** Creates a new instance of GroupSet */
    public DataSeriesGroupSet() {
        groups = new Vector<DataSeriesGroup>();
    }
    
    public void addGroup(DataSeriesGroup group) {
        groups.add(group);
    }
    
    public DataSeriesGroup getSeriesGroup(int i) {
        if (i<0 || i>=groups.size()) return null;
        return groups.get(i);
    }
    
    public DataSeriesGroup getSeriesGroup(String localName) {
        for (int i=0; i<groups.size(); i++) {
            if (groups.get(i).getLocalName().contains(localName)) {
                return groups.get(i);
            }
        }
        return null;
    }
    
    public DataSeriesGroup getSeriesGroup(String categoryName, String subcategoryName, String seriesName) {
        return getSeriesGroup(categoryName + DataSeries.SEPARATOR +
                              subcategoryName + DataSeries.SEPARATOR +
                              seriesName);
    }
    
    public void removeSeriesGroup(int i) {
        if (i>= 0 && i<size()) groups.remove(i);
    }
    
    public int size() {
        return groups.size();
    }
    
    public void parseAllUpdate() {
        for (DataSeriesGroup g : groups) {
            if (!g.isParsed()) {
                g.rebuildSamples(1000);
            }
        }
    }
    

}

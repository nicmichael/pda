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
import de.nmichael.pda.util.Util;

class DataTable {
    int type;
    String header;
}

public class DataTables {
    
    public static final String XML_TABLE              = "Table";
    public static final String XML_TYPE               = "Type";
    public static final String XML_HEADER             = "Header";
    
    
    public static final int TABLE_AVG = 0;
    public static final int TABLE_MIN = 1;
    public static final int TABLE_MAX = 2;
    public static final int TABLE_AVG_PER_LOAD = 3;
    public static final String[] TABLE_NAMES = new String[] { "Average","Minimum","Maximum","Average per KLoad" };
    
    private Vector tables;
    
    /** Creates a new instance of DataTables */
    public DataTables() {
        tables = new Vector();
    }
    
    public static DataTable restoreFromString(String s) {
        DataTable table = new DataTable();
        table.type = TABLE_AVG;
        table.header = "";
        if (s != null) {
            Vector v = Util.split(s, "|");
            if (v.size() >= 1) {
                if (((String)v.get(0)).toLowerCase().trim().equals("avg")) table.type = TABLE_AVG;
                if (((String)v.get(0)).toLowerCase().trim().equals("min")) table.type = TABLE_MIN;
                if (((String)v.get(0)).toLowerCase().trim().equals("max")) table.type = TABLE_MAX;
                if (((String)v.get(0)).toLowerCase().trim().equals("avg_per_load")) table.type = TABLE_AVG_PER_LOAD;
            }
            if (v.size() >= 2) {
                table.header = (String)v.get(1);
            }
        }
        return table;
    }
    
    public void addTable(DataTable t) {
        tables.add(t);
    }
    
    public int size() {
        return tables.size();
    }
    
    public int getTableType(int i) {
        if (i<0 || i>=size()) return -1;
        return ((DataTable)tables.get(i)).type;
    }
    
    public String getTableName(int i) {
        if (i<0 || i>=size()) return null;
        return TABLE_NAMES[((DataTable)tables.get(i)).type];
    }
    
    public String getTableHeader(int i) {
        if (i<0 || i>=size()) return null;
        return ((DataTable)tables.get(i)).header;
    }
    
}

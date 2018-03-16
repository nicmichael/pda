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

public class ParserSet {
    
    private Vector<Parser> parsers;
    private Hashtable<String,Integer> parserIds = new Hashtable<String,Integer>();
    
    public ParserSet() {
        parsers = new Vector<Parser>();
    }
    
    public void addParser(Parser parser) {
        String name = parser.getName();
        
        if (name == null || name.equals(DataSeriesGroup.GROUP_PARSER_NAME)) {
            name = "default";
            parser.setName(name);
        }
        
        // check for duplicate names
        Integer id = parserIds.get(name);
        if (id == null) {
            id = new Integer(1);
        } else {
            // duplicate name: rename old and new parser
            Parser pold = getParser(name);
            if (pold != null) {
                pold.setName(name + "-" + id.intValue());
            }
            id = new Integer(id.intValue() + 1);
            parser.setName(name + "-" + id.intValue());
        }
        parserIds.put(name, id);
        parsers.add(parser);
    }
    
    public Parser getParser(int i) {
        if (i<0 || i>=parsers.size()) {
            return null;
        }
        return parsers.get(i);
    }
    
    public Parser getParser(String parserName) {
        if (parserName == null) {
            return null;
        }
        for (int i=0; i<parsers.size(); i++) {
            if (parsers.get(i).getName().equals(parserName)) {
                return parsers.get(i);
            }
        }
        return null;
    }
    
    public void removeParser(int i) {
        if (i>= 0 && i<size()) {
            parsers.remove(i);
        }
    }
    
    public void removeParser(Parser p) {
        if (p != null) {
            parsers.remove(p);
        }
    }
    
    public int size() {
        return parsers.size();
    }
    
    public DataSeries getSeries(String s) {
        String parserName = DataSeries.getParserName(s);
        Parser p = getParser(parserName);
        if (p == null) {
            return null;
        }
        return p.series().getSeries(DataSeries.getCategoryName(s),
                             DataSeries.getSubcategoryName(s),
                             DataSeries.getSeriesName(s));
    }
    

}

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.nmichael.pda.Logger;
import de.nmichael.pda.data.*;

public class ParserSet {
    
    private Vector<Parser> parsers;
    private Hashtable<String,Parser> parserByBasename = new Hashtable<String,Parser>();
    
    public ParserSet() {
        parsers = new Vector<Parser>();
    }
    
    public void addParser(Parser parser) {
        String name = parser.getName();
        if (name == null || name.equals(DataSeriesGroup.GROUP_PARSER_NAME)) {
            name = "default";
        }
        String baseName = name;
        int pos;
        if ( (pos = name.indexOf("-")) >= 0 && pos+1 < name.length()) {
            baseName = name.substring(0, pos);
        }
        
        int c = 0;
        while (parserByBasename.get(name) != null) {
            name = baseName + "-" + (++c);
        }
        
        parser.setName(name);
        parserByBasename.put(name, parser);
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
    
	public void parseAll() {
		try {
			ExecutorService executor = Executors
					.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1));
			for (int j = 0; j < size(); j++) {
				final Parser p = getParser(j);
				executor.submit(new Runnable() {
					public void run() {
						p.parse(false, false);
					}
				});
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

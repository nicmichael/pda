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

public class Quantiles  {
    
    public static final String Q90 = "q90";
    public static final String Q95 = "q95";
    public static final String Q99 = "q99";
    
    private Hashtable<String,Float> quantiles = new Hashtable<String,Float>();
    
    public Quantiles() {
    }
    
    public void add(String key, float value) {
        quantiles.put(key, value);
    }
    
    public float get(String key) {
        return quantiles.get(key);
    }
    
    public int size() {
        return quantiles.size();
    }
    
    public String[] getKeys() {
        String[] keys = quantiles.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        return keys;
    }
    
}

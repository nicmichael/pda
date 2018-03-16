/**
* Title:        Performance Data Analyzer (PDA)
* Copyright:    Copyright (c) 2006-2015 by Nicolas Michael
* Website:      http://pda.nmichael.de/
* License:      GNU General Public License v2
*
* @author Nicolas Michael
* @version 2
*/

package de.nmichael.pda.data;

public interface Converter {
    
    /**
     * Convert a file
     * @param filename the name of the file
     * @param parser the name of the parser that was used to read the file
     * @param series the series of the file
     * @return true, if successful
     */
    public boolean convert(String filename, String parser, DataSeriesSet series);
    
}
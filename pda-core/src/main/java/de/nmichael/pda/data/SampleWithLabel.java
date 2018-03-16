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
import java.util.regex.*;
import gov.noaa.pmel.util.GeoDate;
import de.nmichael.pda.util.*;

public class SampleWithLabel extends Sample {

    private String label;

    public SampleWithLabel(long timestamp, double value, String label) {
        super(timestamp, value);
        this.label = label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}

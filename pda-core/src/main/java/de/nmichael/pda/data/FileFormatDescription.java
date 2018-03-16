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

public class FileFormatDescription {
    
    public static final String PRODUCT_GENERIC   = "Generic";
    public static final String PRODUCT_SOLARIS   = "Solaris";
    public static final String PRODUCT_LINUX     = "Linux";
    public static final String PRODUCT_JAVA      = "Java";
    public static final String PRODUCT_ORACLE    = "Oracle";
    public static final String PRODUCT_WEBLOGIC  = "WebLogic";
    
    private String productName;
    private String productVersion;
    private String componentName;
    private String[][] argumentOptions;
    private String description;   
    private String exampleFormat;
    
    public FileFormatDescription(String productName, String productVersion, 
            String componentName, String[][] argumentOptions, String description,
            String exampleFormat) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.componentName = componentName;
        this.argumentOptions = argumentOptions;
        this.description = description;
        this.exampleFormat = exampleFormat;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public String getProductVersion() {
        return productVersion;
    }
    
    public String getComponentName() {
        return componentName;
    }
    
    public String[][] getArgumentOption() {
        return argumentOptions;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getExampleFormat() {
        return exampleFormat;
    }
    
}
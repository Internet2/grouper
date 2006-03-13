/*--
 
Copyright 2005 Internet2.  All Rights Reserved.
See doc/license.txt in this distribution.
 */
/*
 * Search.java
 *
 * Created on March 8, 2006, 12:33 PM
 *
 * Author Ellen Sluss
 */

package edu.internet2.middleware.subject.provider;

/**
 *
 * @author esluss
 *
 */

// Holds the a search type and its parameters that were read from the sources.xml file.
 
        
import java.util.Properties;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.naming.directory.SearchControls;

public class Search {
    
    private static Log log = LogFactory.getLog(Search.class);
    
    protected Properties params = new Properties();
    protected String searchType = null;
    
    
    
    /**
     * Creates a new instance of Search
     */
    public Search() {
    }
    
    public void setSearchType(String searchType) {
        this.searchType = searchType;
        
    }
    public String getSearchType() {
        return this.searchType;
        
    }
    public void addParam(String name, String value) {
        this.params.setProperty(name, value);
    }
    /**
     * (non-javadoc)
     * @param name
     * @return param
     */
    protected String getParam(String name) {
        return this.params.getProperty(name);
    }
    
    /**
     * (non-javadoc)
     * @return params
     */
    protected Properties getParams() {
        return this.params;
    }
    
    
    
    
}

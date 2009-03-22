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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class Search {

  /** */
  @SuppressWarnings("unused")
  private static Log log = LogFactory.getLog(Search.class);

  /** */
  protected Properties params = new Properties();

  /** */
  protected String searchType = null;

  /**
   * Creates a new instance of Search
   */
  public Search() {
  }

  /**
   * 
   * @param searchType1
   */
  public void setSearchType(String searchType1) {
    this.searchType = searchType1;

  }

  /**
   * 
   * @return type
   */
  public String getSearchType() {
    return this.searchType;

  }

  /**
   * 
   * @param name
   * @param value
   */
  public void addParam(String name, String value) {
    this.params.setProperty(name, value);
  }

  /**
   * @param name
   * @return param
   */
  protected String getParam(String name) {
    return this.params.getProperty(name);
  }

  /**
   * @return params
   */
  protected Properties getParams() {
    return this.params;
  }

}

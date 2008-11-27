/*
 * @author mchyzer $Id: WsResponseMeta.java,v 1.1 2008-11-27 14:25:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * response metadata (version, warnings, etc)
 */
public class WsResponseMeta {

  /** 
   * if there are warnings, they will be there
   */
  private StringBuilder resultWarnings = new StringBuilder();

  /**
   * append error message to list of error messages
   * 
   * @param warning
   */
  public void appendResultWarning(String warning) {
    this.resultWarnings.append(warning);
  }

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getResultWarnings() {
    return this.resultWarnings.toString();
  }

  /**
   * get the length of request (if specified in bean)
   */
  private long millis = -1;
  
  /**
   * 
   * @return millis
   */
  public String getMillis() {
    return Long.toString(this.millis);
  }
  
  /** server version */
  private String serverVersion = null;
  
  /**
   * @return the serverVersion
   */
  public String getServerVersion() {
    return this.serverVersion;
  }

  
  
  /**
   * @param serverVersion1 the serverVersion to set
   */
  public void setServerVersion(String serverVersion1) {
    this.serverVersion = serverVersion1;
  }

  /**
   * @param resultWarnings1 the resultWarnings to set
   */
  public void setResultWarnings(String resultWarnings1) {
    this.resultWarnings = new StringBuilder(GrouperClientUtils.defaultString(resultWarnings1));
  }
  
  /**
   * @param millis1 the millis to set
   */
  public void setMillis(String millis1) {
    //reset to unset
    this.millis = GrouperClientUtils.longValue(millis1, -1);
  }

}

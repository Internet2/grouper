/*
 * @author mchyzer $Id: WsResponseMeta.java,v 1.2 2008-03-30 09:01:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;
import edu.internet2.middleware.grouper.ws.GrouperWsVersion;

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
   * start of request
   */
  private long millisStart = GrouperServiceJ2ee.retrieveRequestStartMillis();
  
  /**
   * 
   * @return
   */
  public String getMillis() {
    if (this.millis != -1) {
      return Long.toString(this.millis);
    }
    return Long.toString(System.currentTimeMillis() - this.millisStart);
  }
  
  /** server version */
  private String serverVersion = GrouperWsVersion.currentVersion().name();
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
    this.resultWarnings = new StringBuilder(StringUtils.defaultString(resultWarnings1));
  }
  
  /**
   * @param millis1 the millis to set
   */
  public void setMillis(String millis1) {
    //reset to unset
    this.millis = GrouperUtil.longValue(millis1, -1);
  }

}

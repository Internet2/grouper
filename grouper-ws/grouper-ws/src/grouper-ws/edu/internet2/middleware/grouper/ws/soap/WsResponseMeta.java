/*
 * @author mchyzer $Id: WsResponseMeta.java,v 1.1 2008-03-24 20:19:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;

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

}

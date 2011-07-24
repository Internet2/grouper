/*
 * @author mchyzer $Id: WsResponseMeta.java,v 1.6 2008-12-04 07:51:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;

/**
 * response metadata (version, warnings, etc)
 */
public class WsResponseMeta {

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

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
    if (this.resultWarnings == null) {
      this.resultWarnings = new StringBuilder();
    }
    this.resultWarnings.append(warning);
  }

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getResultWarnings() {
    return StringUtils.trimToNull(this.resultWarnings.toString());
  }

  /**
   * the builder for warnings
   * @return the builder for warnings
   */
  public StringBuilder warnings() {
    return this.resultWarnings;
  }
  
  /**
   * get the length of request (if specified in bean)
   */
  private long millis = -1;
  
  /**
   * start of request
   */
  @XStreamOmitField
  private long millisStart = GrouperServiceJ2ee.retrieveRequestStartMillis();
  
  /**
   * 
   * @return millis
   */
  public String getMillis() {
    if (this.millis == -1) {
      this.millis = System.currentTimeMillis() - this.millisStart;
    }
    return Long.toString(this.millis);
  }
  
  /** server version */
  private String serverVersion = GrouperVersion.currentVersion().toString();
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
    this.resultWarnings = StringUtils.isBlank(resultWarnings1) ? null : new StringBuilder(resultWarnings1);
  }
  
  /**
   * @param millis1 the millis to set
   */
  public void setMillis(String millis1) {
    //reset to unset
    this.millis = GrouperUtil.longValue(millis1, -1);
  }

}

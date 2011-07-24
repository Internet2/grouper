/*
 * @author mchyzer
 * $Id: WsParam.java,v 1.1 2008-03-24 20:19:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;


/**
 * param for a web service operation
 */
public class WsParam {

  /** name of param */
  private String paramName;
  
  /** value of param */
  private String paramValue;
  
  /**
   * @param paramName1
   * @param paramValue1
   */
  public WsParam(String paramName1, String paramValue1) {
    super();
    this.paramName = paramName1;
    this.paramValue = paramValue1;
  }

  /**
   * 
   */
  public WsParam() {
    //default
  }

  /**
   * @return the paramName
   */
  public String getParamName() {
    return this.paramName;
  }

  
  /**
   * @param paramName1 the paramName to set
   */
  public void setParamName(String paramName1) {
    this.paramName = paramName1;
  }

  
  /**
   * @return the paramValue
   */
  public String getParamValue() {
    return this.paramValue;
  }

  
  /**
   * @param paramValue1 the paramValue to set
   */
  public void setParamValue(String paramValue1) {
    this.paramValue = paramValue1;
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
  
}

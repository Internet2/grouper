/*
 * @author mchyzer
 * $Id: WsParam.java,v 1.1 2008-03-24 20:19:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;


/**
 * env param for limit
 */
public class WsLimitEnvParam {

  /** 
   * env param name, can typecast the value in the name field, e.g.
   * (integer)amount, (decimal)amount, (timestamp)now, (boolean)isTwoFactor,
   * (null)something, (emptyString)affiliation 
   */
  private String envParamName;
  
  /** env param value */
  private String envParamValue;
  
  /**
   * env param name, can typecast the value in the name field, e.g.
   * (integer)amount, (decimal)amount, (timestamp)now, (boolean)isTwoFactor,
   * (null)something, (emptyString)affiliation 
   * @return name
   */
  public String getEnvParamName() {
    return this.envParamName;
  }

  /**
   * env param name, can typecast the value in the name field, e.g.
   * (integer)amount, (decimal)amount, (timestamp)now, (boolean)isTwoFactor,
   * (null)something, (emptyString)affiliation 
   * @param envParamName1
   */
  public void setEnvParamName(String envParamName1) {
    this.envParamName = envParamName1;
  }

  /**
   * env param value
   * @return env param value
   */
  public String getEnvParamValue() {
    return this.envParamValue;
  }

  /**
   * env param value
   * @param envParamValue1
   */
  public void setEnvParamValue(String envParamValue1) {
    this.envParamValue = envParamValue1;
  }

  /**
   * this typecasts the value.  Default is string.  Can be integer, decimal, 
   * timestamp, boolean, null, emptyString
   * @return type
   */
  public String getEnvParamType() {
    return this.envParamType;
  }

  /**
   * this typecasts the value.  Default is string.  Can be integer, decimal, 
   * timestamp, boolean, null, emptyString
   * @param envParamType1
   */
  public void setEnvParamType(String envParamType1) {
    this.envParamType = envParamType1;
  }

  /** 
   * this typecasts the value.  Default is string.  Can be integer, decimal, 
   * timestamp, boolean, null, emptyString
   */
  private String envParamType;
  
  /**
   * @param paramName1
   * @param paramValue1
   * @param paramType
   */
  public WsLimitEnvParam(String paramName1, String paramValue1, String paramType) {
    super();
    this.envParamName = paramName1;
    this.envParamValue = paramValue1;
    this.envParamType = paramType;
  }

  /**
   * 
   */
  public WsLimitEnvParam() {
    //default
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

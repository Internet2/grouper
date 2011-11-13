/*
 * @author mchyzer
 * $Id: WsParam.java,v 1.1 2008-03-24 20:19:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;


/**
 * param for permission query if there are run time limits this is the runtime data
 */
public class WsPermissionEnvVar {

  /**
   * limit env vars
   * @param paramName1
   * @param paramValue1
   * @param paramType2
   */
  public WsPermissionEnvVar(String paramName1, String paramValue1, String paramType2) {
    super();
    this.paramName = paramName1;
    this.paramValue = paramValue1;
    this.paramType = paramType2;
  }

  /**
   * name of param.  Note you can typecast the value by putting a name like:
   * (integer)someName
   * These are the possible types: text, integer, decimal, date, timestamp, boolean, null, emptyString.
   * Default (if none specified) is text
   */
  private String paramName;
  
  /** value of param */
  private String paramValue;
  
  /** 
   * type of value, e.g. text, integer, decimal, date, timestamp, boolean, null, emptyString.
   * note you can instead typecast on the paramName, e.g. (integer)someParamName
   * if this is not filled in, and there is no typecase on paramName, then will be text (string)
   */
  private String paramType;
  
  /**
   * type of value, e.g. text, integer, decimal, date, timestamp, boolean, null, emptyString.
   * note you can instead typecast on the paramName, e.g. (integer)someParamName
   * @return the param type
   */
  public String getParamType() {
    return this.paramType;
  }

  /**
   * type of value, e.g. text, integer, decimal, date, timestamp, boolean, null, emptyString.
   * note you can also typecast on the paramName, e.g. (integer)someParamName
   * @param paramType1
   */
  public void setParamType(String paramType1) {
    this.paramType = paramType1;
  }

  /**
   * 
   */
  public WsPermissionEnvVar() {
    //default
  }

  /**
   * name of param.  Note you can typecast the value by putting a name like:
   * (integer)someName
   * These are the possible types: text, integer, decimal, date, timestamp, boolean, null, emptyString.
   * Default (if none specified) is text
   * @return the paramName
   */
  public String getParamName() {
    return this.paramName;
  }

  
  /**
   * name of param.  Note you can typecast the value by putting a name like:
   * (integer)someName
   * These are the possible types: text, integer, decimal, date, timestamp, boolean, null, emptyString.
   * Default (if none specified) is text
   * @param paramName1 the paramName to set
   */
  public void setParamName(String paramName1) {
    this.paramName = paramName1;
  }

  
  /**
   * value of param
   * @return the paramValue
   */
  public String getParamValue() {
    return this.paramValue;
  }

  
  /**
   * value of param
   * @param paramValue1 the paramValue to set
   */
  public void setParamValue(String paramValue1) {
    this.paramValue = paramValue1;
  }
  
}

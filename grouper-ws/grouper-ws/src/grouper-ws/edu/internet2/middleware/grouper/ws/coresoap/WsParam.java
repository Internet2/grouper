/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: WsParam.java,v 1.1 2008-03-24 20:19:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * param for a web service operation
 */
@ApiModel(description = "Name value pairs")
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
  @ApiModelProperty(value = "Parameter name")
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
  @ApiModelProperty(value = "Parameter value")
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

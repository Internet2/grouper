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
 * @author mchyzer $Id: WsResponseMeta.java,v 1.6 2008-12-04 07:51:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;

import edu.internet2.middleware.grouper.misc.GrouperVersion;

/**
 * response metadata (version, warnings, etc)
 */
public class WsResponseMeta {

  /** 
   * if there are warnings, they will be there
   */
  private String resultWarnings = null;

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getResultWarnings() {
    return this.resultWarnings;
  }

  /**
   * get the length of request (if specified in bean)
   */
  private String millis;
  
  /**
   * 
   * @return millis
   */
  public String getMillis() {
    return this.millis;
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
    this.resultWarnings = resultWarnings1;
  }
  
  /**
   * @param millis1 the millis to set
   */
  public void setMillis(String millis1) {
    this.millis = millis1;
  }

}

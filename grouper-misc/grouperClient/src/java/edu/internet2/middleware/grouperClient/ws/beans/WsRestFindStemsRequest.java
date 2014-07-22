/**
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
 */
/*
 * @author mchyzer
 * $Id: WsRestFindStemsRequest.java,v 1.2 2009-12-15 06:47:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * bean that will be the data from rest request
 */
public class WsRestFindStemsRequest implements WsRequestBean {
  
  /** query filter for request */
  private WsStemQueryFilter wsStemQueryFilter;

  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private WsParam[] params;
  
  /**  to pass in a list of uuids or names to lookup.  Note the stems are returned
   * in alphabetical order */
  private WsStemLookup[] wsStemLookups;
  
  /**
   *  to pass in a list of uuids or names to lookup.  Note the stems are returned
   * in alphabetical order
   * @return stem lookups
   */
  public WsStemLookup[] getWsStemLookups() {
    return this.wsStemLookups;
  }

  /**
   *  to pass in a list of uuids or names to lookup.  Note the stems are returned
   * in alphabetical order
   * @param wsStemLookups1
   */
  public void setWsStemLookups(WsStemLookup[] wsStemLookups1) {
    this.wsStemLookups = wsStemLookups1;
  }

  /**
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  
  /**
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  
  /**
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  
  /**
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  
  /**
   * @return the params
   */
  public WsParam[] getParams() {
    return this.params;
  }


  
  /**
   * @param params1 the params to set
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * query filter for request
   * @return the wsQueryFilter
   */
  public WsStemQueryFilter getWsStemQueryFilter() {
    return this.wsStemQueryFilter;
  }


  
  /**
   * query filter for request
   * @param wsQueryFilter1 the wsQueryFilter to set
   */
  public void setWsStemQueryFilter(WsStemQueryFilter wsQueryFilter1) {
    this.wsStemQueryFilter = wsQueryFilter1;
  }

}

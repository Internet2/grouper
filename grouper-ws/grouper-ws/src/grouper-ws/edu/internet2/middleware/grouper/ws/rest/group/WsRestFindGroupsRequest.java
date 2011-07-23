/*
 * @author mchyzer
 * $Id: WsRestFindGroupsRequest.java,v 1.2 2009-12-15 06:47:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsQueryFilter;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup;

/**
 * bean that will be the data from rest request
 * @see GrouperServiceLogic#getGroups(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsSubjectLookup[], edu.internet2.middleware.grouper.ws.member.WsMemberFilter, WsSubjectLookup, boolean, boolean, String[], WsParam[])
 * for method
 */
public class WsRestFindGroupsRequest implements WsRequestBean {
  
  /** query filter for request */
  private WsQueryFilter wsQueryFilter;

  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private String includeGroupDetail;
  
  /** field */
  private WsParam[] params;
  
  /** wsGroupLookups if you want to just pass in a list of uuids and/or names.  Note the groups are returned
   * in alphabetical order */
  private WsGroupLookup[] wsGroupLookups;
  
  /**
   * wsGroupLookups if you want to just pass in a list of uuids and/or names.  Note the groups are returned
   * in alphabetical order
   * @return group lookups
   */
  public WsGroupLookup[] getWsGroupLookups() {
    return this.wsGroupLookups;
  }


  /**
   * wsGroupLookups if you want to just pass in a list of uuids and/or names.  Note the groups are returned
   * in alphabetical order
   * @param wsGroupLookups1
   */
  public void setWsGroupLookups(WsGroupLookup[] wsGroupLookups1) {
    this.wsGroupLookups = wsGroupLookups1;
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
   * @return the includeGroupDetail
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  
  /**
   * @param includeGroupDetail1 the includeGroupDetail to set
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
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
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.GET;
  }


  
  /**
   * query filter for request
   * @return the wsQueryFilter
   */
  public WsQueryFilter getWsQueryFilter() {
    return this.wsQueryFilter;
  }


  
  /**
   * query filter for request
   * @param wsQueryFilter1 the wsQueryFilter to set
   */
  public void setWsQueryFilter(WsQueryFilter wsQueryFilter1) {
    this.wsQueryFilter = wsQueryFilter1;
  }

}

/*
 * @author mchyzer
 * $Id: GcFindGroups.java,v 1.3 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsQueryFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestFindGroupsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run find groups
 */
public class GcFindGroups {

  /** query filters */
  private WsQueryFilter queryFilter;

  /**
   * assign a query filter
   * @param theQueryFilter
   * @return this for chaining
   */
  public GcFindGroups assignQueryFilter(WsQueryFilter theQueryFilter) {
    this.queryFilter = theQueryFilter;
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcFindGroups assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcFindGroups addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcFindGroups addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcFindGroups assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (this.queryFilter == null) {
      throw new RuntimeException("Need to pass in a query filter: " + this);
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcFindGroups assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsFindGroupsResults execute() {
    this.validate();
    WsFindGroupsResults wsFindGroupsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestFindGroupsRequest findGroups = new WsRestFindGroupsRequest();

      findGroups.setActAsSubjectLookup(this.actAsSubject);

      if (this.includeGroupDetail != null) {
        findGroups.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }
      
      findGroups.setWsQueryFilter(this.queryFilter);
      
      //add params if there are any
      if (this.params.size() > 0) {
        findGroups.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsFindGroupsResults = (WsFindGroupsResults)
        grouperClientWs.executeService("groups", findGroups, "findGroups", this.clientVersion);
      
      String resultMessage = wsFindGroupsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsFindGroupsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsFindGroupsResults;
    
  }
  
}

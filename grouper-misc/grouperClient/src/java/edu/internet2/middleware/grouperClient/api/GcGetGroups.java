/*
 * @author mchyzer
 * $Id: GcGetGroups.java,v 1.5 2009-03-15 08:16:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetGroupsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get groups web service call
 */
public class GcGetGroups {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetGroups assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  

  /** subject lookups */
  private List<WsSubjectLookup> subjectLookups = new ArrayList<WsSubjectLookup>();

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcGetGroups addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetGroups addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcGetGroups addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectId
   * @return this for chaining
   */
  public GcGetGroups addSubjectId(String subjectId) {
    this.subjectLookups.add(new WsSubjectLookup(subjectId, null, null));
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectIdentifier
   * @return this for chaining
   */
  public GcGetGroups addSubjectIdentifier(String subjectIdentifier) {
    this.subjectLookups.add(new WsSubjectLookup(null, null, subjectIdentifier));
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetGroups assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.subjectLookups) == 0) {
      throw new RuntimeException("Need at least one subject to add to group: " + this);
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * member filter 
   */
  private WsMemberFilter memberFilter;

  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetGroups addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetGroups assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetGroups assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetGroupsResults execute() {
    this.validate();
    WsGetGroupsResults wsGetGroupsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetGroupsRequest getGroups = new WsRestGetGroupsRequest();

      getGroups.setActAsSubjectLookup(this.actAsSubject);

      if (this.includeGroupDetail != null) {
        getGroups.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getGroups.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        getGroups.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      getGroups.setMemberFilter(this.memberFilter == null ? null : this.memberFilter.name());

      WsSubjectLookup[] subjectLookupsResults = GrouperClientUtils.toArray(this.subjectLookups, 
          WsSubjectLookup.class);
      getGroups.setSubjectLookups(subjectLookupsResults);

      //add params if there are any
      if (this.params.size() > 0) {
        getGroups.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetGroupsResults = (WsGetGroupsResults)
        grouperClientWs.executeService("subjects", getGroups, "getGroups", this.clientVersion);
      
      String resultMessage = wsGetGroupsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetGroupsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetGroupsResults;
    
  }

  /**
   * assign the member filter
   * @param theMemberFilter
   * @return this for chaining
   */
  public GcGetGroups assignMemberFilter(WsMemberFilter theMemberFilter) {
    this.memberFilter = theMemberFilter;
    return this;
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    WsGetGroupsResults wsGetGroupsResults = new GcGetGroups()
      .addSubjectLookup(new WsSubjectLookup("10021368", null, null)).execute();
    WsGetGroupsResult wsGroupsResult = wsGetGroupsResults.getResults()[0];
    for (WsGroup wsGroup : GrouperClientUtils.nonNull(wsGroupsResult.getWsGroups(), WsGroup.class)) {
      System.out.println(wsGroup.getName());
    }
  }
  
}

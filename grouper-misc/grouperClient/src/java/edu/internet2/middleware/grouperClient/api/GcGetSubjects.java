/*
 * @author mchyzer
 * $Id: GcGetSubjects.java,v 1.1 2009-12-30 04:23:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.WsMemberFilter;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGetSubjectsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a get subjects web service call
 */
public class GcGetSubjects {

  /** ws subject lookups to find memberships about */
  private Set<WsSubjectLookup> wsSubjectLookups = new LinkedHashSet<WsSubjectLookup>();
  
  /** client version */
  private String clientVersion;

  /** search for subject by name or whatever */
  private String searchString;

  /**
   * assign the search string
   * @param theSearchString
   * @return this for chaining
   */
  public GcGetSubjects assignSearchString(String theSearchString) {
    this.searchString = theSearchString;
    return this;
  }
  
  /**
   * assign a group to filter subjects from
   * @param theGroupLookup
   * @return this for chaining
   */
  public GcGetSubjects assignGroupLookup(WsGroupLookup theGroupLookup) {
    this.wsGroupLookup = theGroupLookup;
    return this;
  }
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGetSubjects assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** if filtering by group, this is the group lookup */
  private WsGroupLookup wsGroupLookup;
    
  /**
   * set the subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcGetSubjects addWsSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.wsSubjectLookups.add(wsSubjectLookup);
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
  public GcGetSubjects addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGetSubjects addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** member filter */
  private WsMemberFilter memberFilter;
  
  /**
   * assign the member filter
   * @param theMemberFilter
   * @return this for chaining
   */
  public GcGetSubjects assignMemberFilter(WsMemberFilter theMemberFilter) {
    this.memberFilter = theMemberFilter;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcGetSubjects assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.searchString)
        && GrouperClientUtils.length(this.wsSubjectLookups) == 0) {
      throw new RuntimeException("Search string or subject lookup is required: " + this);
    }
  }
  
  /** field name to add member */
  private String fieldName;
  
  /**
   * assign the field name to the request
   * @param theFieldName
   * @return this for chaining
   */
  public GcGetSubjects assignFieldName(String theFieldName) {
    this.fieldName = theFieldName;
    return this;
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /** source ids to limit the results to, or null for all sources */
  private Set<String> sourceIds = null;
  
  /**
   * add a source id to filter by (or none for all sources)
   * @param sourceId
   * @return this for chaining
   */
  public GcGetSubjects addSourceId(String sourceId) {
    if (this.sourceIds == null) {
      this.sourceIds = new LinkedHashSet<String>();
    }
    this.sourceIds.add(sourceId);
    return this;
  }
  
  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcGetSubjects addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGetSubjects assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcGetSubjects assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGetSubjectsResults execute() {
    this.validate();
    WsGetSubjectsResults wsGetSubjectsResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGetSubjectsRequest getSubjects = new WsRestGetSubjectsRequest();

      getSubjects.setActAsSubjectLookup(this.actAsSubject);

      getSubjects.setFieldName(this.fieldName);
      
      getSubjects.setWsGroupLookup(this.wsGroupLookup);
      
      if (this.includeGroupDetail != null) {
        getSubjects.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        getSubjects.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      getSubjects.setSearchString(this.searchString);
     
      getSubjects.setMemberFilter(this.memberFilter == null ? null : this.memberFilter.name());

      //add params if there are any
      if (this.params.size() > 0) {
        getSubjects.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      if (GrouperClientUtils.length(this.sourceIds) > 0) {
        getSubjects.setSourceIds(GrouperClientUtils.toArray(this.sourceIds, String.class));
      }
      
      if (this.subjectAttributeNames.size() > 0) {
        getSubjects.setSubjectAttributeNames(
            GrouperClientUtils.toArray(this.subjectAttributeNames, String.class));
      }
      
      if (GrouperClientUtils.length(this.wsSubjectLookups) > 0) {
        getSubjects.setWsSubjectLookups(GrouperClientUtils.toArray(this.wsSubjectLookups, WsSubjectLookup.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGetSubjectsResults = (WsGetSubjectsResults)
        grouperClientWs.executeService("subjects", getSubjects, "getSubjects", this.clientVersion, true);
      
      String resultMessage = wsGetSubjectsResults.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsGetSubjectsResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGetSubjectsResults;
    
  }
  
}

/*
 * @author mchyzer
 * $Id: GcAssignGrouperPrivilegesLite.java,v 1.2 2008-12-08 02:55:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignGrouperPrivilegesLiteRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a assign grouper privileges web service call
 */
public class GcAssignGrouperPrivilegesLite {

  /** if we are assigning or unassigning */
  private Boolean allowed = null;
  
  /**
   * assign if allowed
   * @param isAllowed
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignAllowed(boolean isAllowed) {
    this.allowed = isAllowed;
    return this;
  }
  
  /** group name to assign privileges */
  private String groupName;
  
  /** privilege type, e.g. access or naming */
  private String privilegeType;

  /**
   * assign privilege type
   * @param thePrivilegeType
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignPrivilegeType(String thePrivilegeType) {
    this.privilegeType = thePrivilegeType;
    return this;
  }
  
  /** privilege name, e.g. admin */
  private String privilegeName;

  /**
   * assign the privilege name to query
   * @param thePrivilegeName
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignPrivilegeName(String thePrivilegeName) {
    this.privilegeName = thePrivilegeName;
    return this;
  }
   
  /**
   * set the group name
   * @param theGroupName
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignGroupName(String theGroupName) {
    this.groupName = theGroupName;
    return this;
  }
  
  /** stem name to query about privileges */
  private String stemName;
  
  /**
   * set the stem name to query privs
   * @param theStemName
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignStemName(String theStemName) {
    this.stemName = theStemName;
    return this;
  }
  
  /** subject lookups */
  private WsSubjectLookup subjectLookup = null;

  /** params */
  private List<WsParam> params = new ArrayList<WsParam>();

  /**
   * add a param to the list
   * @param paramName
   * @param paramValue
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite addParam(WsParam wsParam) {
    this.params.add(wsParam);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookup = wsSubjectLookup;
    return this;
  }
  
  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.params) > 2) {
      throw new RuntimeException("Params can only be size 2 for this request");
    }
    if (this.allowed == null) {
      throw new RuntimeException("Specify true or false for is allowed");
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  
  /** if the subject detail should be sent back */
  private Boolean includeSubjectDetail;

  /** subject attribute names to return */
  private Set<String> subjectAttributeNames = new LinkedHashSet<String>();

  /**
   * 
   * @param subjectAttributeName
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite addSubjectAttributeName(String subjectAttributeName) {
    this.subjectAttributeNames.add(subjectAttributeName);
    return this;
  }
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * if should include subject detail
   * @param theIncludeSubjectDetail
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignIncludeSubjectDetail(Boolean theIncludeSubjectDetail) {
    this.includeSubjectDetail = theIncludeSubjectDetail;
    return this;
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAssignGrouperPrivilegesLite assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAssignGrouperPrivilegesLiteResult execute() {
    this.validate();
    WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignGrouperPrivilegesLiteRequest wsAssignGrouperPrivilegesLite = new WsRestAssignGrouperPrivilegesLiteRequest();

      if (this.actAsSubject != null) {
        wsAssignGrouperPrivilegesLite.setActAsSubjectId(this.actAsSubject.getSubjectId());
        wsAssignGrouperPrivilegesLite.setActAsSubjectIdentifier(this.actAsSubject.getSubjectIdentifier());
        wsAssignGrouperPrivilegesLite.setActAsSubjectSourceId(this.actAsSubject.getSubjectSourceId());
      }

      if (this.includeGroupDetail != null) {
        wsAssignGrouperPrivilegesLite.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      if (this.includeSubjectDetail != null) {
        wsAssignGrouperPrivilegesLite.setIncludeSubjectDetail(this.includeSubjectDetail ? "T" : "F");
      }
      
      wsAssignGrouperPrivilegesLite.setGroupName(this.groupName);
      wsAssignGrouperPrivilegesLite.setStemName(this.stemName);
      
      wsAssignGrouperPrivilegesLite.setPrivilegeType(this.privilegeType);
      wsAssignGrouperPrivilegesLite.setPrivilegeName(this.privilegeName);
      
      if (this.subjectAttributeNames.size() > 0) {
        wsAssignGrouperPrivilegesLite.setSubjectAttributeNames(
            GrouperClientUtils.join(this.subjectAttributeNames.iterator(), ','));
      }
      
      if (this.subjectLookup != null) {
        wsAssignGrouperPrivilegesLite.setSubjectId(this.subjectLookup.getSubjectId());
        wsAssignGrouperPrivilegesLite.setSubjectIdentifier(this.subjectLookup.getSubjectIdentifier());
        wsAssignGrouperPrivilegesLite.setSubjectId(this.subjectLookup.getSubjectId());
      }

      //add params if there are any
      if (this.params.size() > 0) {
        wsAssignGrouperPrivilegesLite.setParamName0(this.params.get(0).getParamName());
        wsAssignGrouperPrivilegesLite.setParamValue0(this.params.get(0).getParamValue());
      }
      if (this.params.size() > 1) {
        wsAssignGrouperPrivilegesLite.setParamName1(this.params.get(1).getParamName());
        wsAssignGrouperPrivilegesLite.setParamValue1(this.params.get(1).getParamValue());
      }
      
      //right now we only have allowed permissions
      if (this.allowed != null) {
        wsAssignGrouperPrivilegesLite.setAllowed(this.allowed ? "T" : "F");
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsAssignGrouperPrivilegesLiteResult = (WsAssignGrouperPrivilegesLiteResult)
        grouperClientWs.executeService("grouperPrivileges", wsAssignGrouperPrivilegesLite, 
            "assignGrouperPrivilegesLite", this.clientVersion, false);
      
      String resultMessage = wsAssignGrouperPrivilegesLiteResult.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsAssignGrouperPrivilegesLiteResult, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAssignGrouperPrivilegesLiteResult;
    
  }
  
}

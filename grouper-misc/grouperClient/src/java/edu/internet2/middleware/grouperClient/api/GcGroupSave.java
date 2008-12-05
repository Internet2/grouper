/*
 * @author mchyzer
 * $Id: GcGroupSave.java,v 1.2 2008-12-05 02:24:39 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GcTransactionType;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupToSave;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestGroupSaveRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;


/**
 * class to run a group save web service call
 */
public class GcGroupSave {

  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcGroupSave assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** groups to save */
  private List<WsGroupToSave> groupsToSave = new ArrayList<WsGroupToSave>();

  /**
   * add a group to save
   * @param wsGroupToSave
   * @return group to save
   */
  public GcGroupSave addGroupToSave(WsGroupToSave wsGroupToSave) {
    this.groupsToSave.add(wsGroupToSave);
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
  public GcGroupSave addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcGroupSave addParam(WsParam wsParam) {
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
  public GcGroupSave assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.groupsToSave) == 0) {
      throw new RuntimeException("Group name is required: " + this);
    }
  }
  
  /** if the group detail should be sent back */
  private Boolean includeGroupDetail;
  /**
   * tx type for request 
   */
  private GcTransactionType txType;
  
  /**
   * assign if the group detail should be included
   * @param theIncludeGroupDetail
   * @return this for chaining
   */
  public GcGroupSave assignIncludeGroupDetail(Boolean theIncludeGroupDetail) {
    this.includeGroupDetail = theIncludeGroupDetail;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsGroupSaveResults execute() {
    this.validate();
    WsGroupSaveResults wsGroupSaveResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestGroupSaveRequest groupSave = new WsRestGroupSaveRequest();

      groupSave.setActAsSubjectLookup(this.actAsSubject);

      groupSave.setTxType(this.txType == null ? null : this.txType.name());

      if (this.includeGroupDetail != null) {
        groupSave.setIncludeGroupDetail(this.includeGroupDetail ? "T" : "F");
      }

      groupSave.setWsGroupToSaves(GrouperClientUtils.toArray(this.groupsToSave, WsGroupToSave.class));
      
      //add params if there are any
      if (this.params.size() > 0) {
        groupSave.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();
      
      //kick off the web service
      wsGroupSaveResults = (WsGroupSaveResults)
        grouperClientWs.executeService("groups", groupSave, "groupSave", this.clientVersion);
      
      String groupSaveResultMessage = "";
      
      //try to get the inner message
      try {
        groupSaveResultMessage = wsGroupSaveResults.getResults()[0].getResultMetadata().getResultMessage();

      } catch (Exception e) {}
      
      String resultMessage = wsGroupSaveResults.getResultMetadata().getResultMessage() + "\n"
        + groupSaveResultMessage;
      
      grouperClientWs.handleFailure(resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsGroupSaveResults;
    
  }

  /**
   * assign the tx type
   * @param gcTransactionType
   * @return self for chaining
   */
  public GcGroupSave assignTxType(GcTransactionType gcTransactionType) {
    this.txType = gcTransactionType;
    return this;
  }
  
}

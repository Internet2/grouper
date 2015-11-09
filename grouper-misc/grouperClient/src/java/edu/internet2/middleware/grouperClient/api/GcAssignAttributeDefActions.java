package edu.internet2.middleware.grouperClient.api;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefAssignActionResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeDefLookup;
import edu.internet2.middleware.grouperClient.ws.beans.WsParam;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestAssignAttributeDefActionsRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

/** class to assign actions to attribute def **/
public class GcAssignAttributeDefActions {

  /** client version */
  private String clientVersion;
  
  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcAssignAttributeDefActions assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }
  
  /** Attribute Definition to be modified **/
  private WsAttributeDefLookup wsAttributeDefLookup;
  
  /**
   * @param wsAttributeDefLookup1
   * @return this for chaining
   */
  public GcAssignAttributeDefActions assignAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
	this.wsAttributeDefLookup = wsAttributeDefLookup1;
	return this;
  }

  /** actions to assign */
  private Set<String> actions = new LinkedHashSet<String>();
	  
  /**
   * @param action
   * @return this for chaining
   */
  public GcAssignAttributeDefActions addAction(String action) {
    this.actions.add(action);
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
  public GcAssignAttributeDefActions addParam(String paramName, String paramValue) {
    this.params.add(new WsParam(paramName, paramValue));
    return this;
  }
	  
  /**
   * add a param to the list
   * @param wsParam
   * @return this for chaining
   */
  public GcAssignAttributeDefActions addParam(WsParam wsParam) {
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
  public GcAssignAttributeDefActions assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  /**
   * if we are assigning or unassigning
   */
  private Boolean assign;

  /**
   * if we are assigning or unassigning
   * @param isAssign
   * @return this for chaining
   */
  public GcAssignAttributeDefActions assign(Boolean isAssign) {
    this.assign = isAssign;
    return this;
  }
  
  /**
   * if it is an assignment, if we are replacing existing assignments
   */
  private Boolean replaceAllExisting;
  
  /**
   * if it is an assignment, if we are replacing existing assignments
   * @param replaceAllExisting1
   * @return this for chaining
   */
  public GcAssignAttributeDefActions assignReplaceAllExisting(Boolean replaceAllExisting1) {
    this.replaceAllExisting = replaceAllExisting1;
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.length(this.actions) == 0) {
      throw new RuntimeException("actions are required: " + this);
    }
    if (this.wsAttributeDefLookup == null || (GrouperClientUtils.isBlank(this.wsAttributeDefLookup.getName())
        && GrouperClientUtils.isBlank(this.wsAttributeDefLookup.getUuid())
        && GrouperClientUtils.isBlank(this.wsAttributeDefLookup.getIdIndex()))) {
      throw new RuntimeException("AttributeDef is required: " + this);
    }
    if (this.assign == null) {
      throw new RuntimeException("Assign is required, true means you are assigning, false means you are removing a direct assignment");
    }
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsAttributeDefAssignActionResults execute() {
    this.validate();
    WsAttributeDefAssignActionResults wsAttributeDefAssignActionResults = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestAssignAttributeDefActionsRequest assignAttributeDefActionRequest = new WsRestAssignAttributeDefActionsRequest();

      assignAttributeDefActionRequest.setActAsSubjectLookup(this.actAsSubject);

      assignAttributeDefActionRequest.setWsAttributeDefLookup(this.wsAttributeDefLookup);
      
      assignAttributeDefActionRequest.setActions(GrouperClientUtils.toArray(this.actions, String.class));
      
      if (this.assign != null) {
    	  assignAttributeDefActionRequest.setAssign(this.assign ? "T" : "F");
      }
      
      if (this.replaceAllExisting != null) {
    	  assignAttributeDefActionRequest.setReplaceAllExisting(this.replaceAllExisting ? "T" : "F");
      }
      
      //add params if there are any
      if (this.params.size() > 0) {
    	  assignAttributeDefActionRequest.setParams(GrouperClientUtils.toArray(this.params, WsParam.class));
      }

      GrouperClientWs grouperClientWs = new GrouperClientWs();

      //kick off the web service
      wsAttributeDefAssignActionResults = (WsAttributeDefAssignActionResults)
        grouperClientWs.executeService("attributeDefActions", assignAttributeDefActionRequest, "assignActionsToAttributeDef", this.clientVersion, false);
      
      String attributeDefNameSaveResultMessage = "";
      
      //try to get the message
      try {
        attributeDefNameSaveResultMessage = wsAttributeDefAssignActionResults.getResultMetadata().getResultMessage();

      } catch (Exception e) {}
      
      String resultMessage = wsAttributeDefAssignActionResults.getResultMetadata().getResultMessage() + "\n"
        + attributeDefNameSaveResultMessage;
      
      grouperClientWs.handleFailure(wsAttributeDefAssignActionResults, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsAttributeDefAssignActionResults;
    
  }


}

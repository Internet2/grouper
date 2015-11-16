package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * request bean in body of rest request
 * Add/Remove/Replace actions from attribute def
 */
public class WsRestAssignAttributeDefActionsRequest implements WsRequestBean {

  /** attribute def to add or remove from actions **/
  private WsAttributeDefLookup wsAttributeDefLookup;

  /**
   * attribute def to add or remove from actions
   * @return wsAttributeDefLookup
   */
  public WsAttributeDefLookup getWsAttributeDefLookup() {
    return this.wsAttributeDefLookup;
  }

  /**
   * attribute def to add or remove from actions
   * @param wsAttributeDefLookup1
   */
  public void setWsAttributeDefLookup(WsAttributeDefLookup wsAttributeDefLookup1) {
    this.wsAttributeDefLookup = wsAttributeDefLookup1;
  }

  /** actions to be added/removed/replaced **/
  private String[] actions;

  /**
   * actions to be added/removed/replaced
   * @return actions
   */
  public String[] getActions() {
    return this.actions;
  }

  /**
   * actions to be added/removed/replaced
   * @param actions1
   */
  public void setActions(String[] actions1) {
    this.actions = actions1;
  }

  /**
     * T to assign, or F to remove assignment
     */
  private String assign;

  /**
   * T if assigning, if this list should replace all existing immediately inherited attribute def names
   */
  private String replaceAllExisting;

  /**
   * T to assign, or F to remove assignment
   * @return assign
   */
  public String getAssign() {
    return this.assign;
  }

  /**
   * T to assign, or F to remove assignment
   * @param assign1
   */
  public void setAssign(String assign1) {
    this.assign = assign1;
  }

  /**
   * T if assigning, if this list should replace all existing immediately inherited attribute def names
   * @return replaceAllExisting
   */
  public String getReplaceAllExisting() {
    return this.replaceAllExisting;
  }

  /**
   * T if assigning, if this list should replace all existing immediately inherited attribute def names
   * @param replaceAllExisting1
   */
  public void setReplaceAllExisting(String replaceAllExisting1) {
    this.replaceAllExisting = replaceAllExisting1;
  }

  /** is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000 */
  private String clientVersion;

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * is the version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /** if acting as someone else */
  private WsSubjectLookup actAsSubjectLookup;

  /**
   * if acting as someone else
   * @return act as subject
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * if acting as someone else
   * @param actAsSubjectLookup1
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /** optional: reserved for future use */
  private WsParam[] params;

  /**
   * optional: reserved for future use
   * @return params
   */
  public WsParam[] getParams() {
    return this.params;
  }

  /**
   * optional: reserved for future use
   * @param params1
   */
  public void setParams(WsParam[] params1) {
    this.params = params1;
  }

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
  */
  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }

}

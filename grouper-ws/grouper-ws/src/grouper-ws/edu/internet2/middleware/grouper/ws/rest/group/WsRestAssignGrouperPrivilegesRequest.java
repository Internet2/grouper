/**
 * 
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

/**
 * <pre>
 * Class with data about assigning privileges for a subject and group
 * 
 * </pre>
 * @author mchyzer
 */
public class WsRestAssignGrouperPrivilegesRequest implements WsRequestBean {

  /** attribute names to return */
  private String[] subjectAttributeNames;
    
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * T to replace existing members, F or blank to just change assignments.  Only for allowed T
   */
  private String replaceAllExisting;
  
  
  
  /**
   * T to replace existing members, F or blank to just change assignments.  Only for allowed T
   * @return replace all existing
   */
  public String getReplaceAllExisting() {
    return this.replaceAllExisting;
  }


  /**
   * 
   * @param replaceAllExisting1
   */
  public void setReplaceAllExisting(String replaceAllExisting1) {
    this.replaceAllExisting = replaceAllExisting1;
  }

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(WsRestAssignGrouperPrivilegesRequest.class);

  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }
  
  /**
   * T or F as to whether this privilege is being assigned or removed
   */
  private String allowed;
  
  /**
   * version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   */
  private String clientVersion;

  /**
   * group to assign privilege
   */
  private WsGroupLookup wsGroupLookup;
  
  /**
   * group to assign privilege
   * @return group lookup
   */
  public WsGroupLookup getWsGroupLookup() {
    return this.wsGroupLookup;
  }


  /**
   * group to assign privilege
   * @param wsGroupLookup1
   */
  public void setWsGroupLookup(WsGroupLookup wsGroupLookup1) {
    this.wsGroupLookup = wsGroupLookup1;
  }

  /**
   * stem to assign privilege to
   */
  private WsStemLookup wsStemLookup;
  
  /**
   * stem to assign privilege
   * @return stem lookup
   */
  public WsStemLookup getWsStemLookup() {
    return this.wsStemLookup;
  }

  
  /**
   * group to assign privilege
   * @param wsStemLookup1
   */
  public void setWsStemLookup(WsStemLookup wsStemLookup1) {
    this.wsStemLookup = wsStemLookup1;
  }

  /**
   * privilegeType (e.g. "access" for groups and "naming" for stems)
   */
  private String privilegeType;
  
  /**
   * (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   */
  private String[] privilegeNames;
  
  /**
   * 
   */
  private String includeSubjectDetail;
  
  /**
   * T or F as for if group detail should be included
   */
  private String includeGroupDetail;
  
  /** subjects to assign to */
  private WsSubjectLookup[] wsSubjectLookups;

  /** who to act as if not the connecting user */
  private WsSubjectLookup actAsSubjectLookup;

  /** field */
  private String txType;

  /** field */
  private WsParam[] params;

  /**
   * version of the client.  Must be in GrouperWsVersion, e.g. v1_3_000
   * @return version
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * 
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  /**
   * privilegeType (e.g. "access" for groups and "naming" for stems)
   * @return type
   */
  public String getPrivilegeType() {
    return this.privilegeType;
  }

  /**
   * privilegeType (e.g. "access" for groups and "naming" for stems)
   * @param privilegeType1
   */
  public void setPrivilegeType(String privilegeType1) {
    this.privilegeType = privilegeType1;
  }

  /**
   * (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @return name
   */
  public String[] getPrivilegeNames() {
    return this.privilegeNames;
  }

  /**
   * (e.g. for groups: read, view, update, admin, optin, optout.  e.g. for stems:
   * stem, create)
   * @param privilegeNames1
   */
  public void setPrivilegeNames(String[] privilegeNames1) {
    this.privilegeNames = privilegeNames1;
  }

  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @return include detail
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }

  /**
   * T|F, for if the extended subject information should be
   * returned (anything more than just the id)
   * @param includeSubjectDetail1
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }

  /**
   * T or F as for if group detail should be included
   * @return T of F
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }

  /**
   * T or F as for if group detail should be included
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }

  /**
   * T or F as to whether this privilege is being assigned or removed
   * @return allowed
   */
  public String getAllowed() {
    return this.allowed;
  }

  /**
   * T or F as to whether this privilege is being assigned or removed
   * @param allowed1
   */
  public void setAllowed(String allowed1) {
    this.allowed = allowed1;
  }


  /**
   * subjects to assign to
   * @return the subjectLookups
   */
  public WsSubjectLookup[] getWsSubjectLookups() {
    return this.wsSubjectLookups;
  }


  /**
   * subjects to assign to
   * @param subjectLookups1 the subjectLookups to set
   */
  public void setWsSubjectLookups(WsSubjectLookup[] subjectLookups1) {
    this.wsSubjectLookups = subjectLookups1;
  }


  /**
   * who to act as if not the connecting user
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }


  /**
   * who to act as if not the connecting user
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }


  /**
   * @return the subjectAttributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }


  /**
   * @param subjectAttributeNames1 the subjectAttributeNames to set
   */
  public void setSubjectAttributeNames(String[] subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }


  /**
   * @return the txType
   */
  public String getTxType() {
    return this.txType;
  }


  /**
   * @param txType1 the txType to set
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
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
  

  
}

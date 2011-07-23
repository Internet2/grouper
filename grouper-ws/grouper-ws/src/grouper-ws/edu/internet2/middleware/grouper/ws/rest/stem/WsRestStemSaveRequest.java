/*
 * @author mchyzer
 * $Id: WsRestStemSaveRequest.java,v 1.1 2008-03-30 09:01:03 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.stem;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsParam;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsStemToSave;
import edu.internet2.middleware.grouper.ws.soap_v2_0.WsSubjectLookup;

/**
 * bean that will be the data from rest request
 * @see GrouperServiceLogic#getGroups(edu.internet2.middleware.grouper.ws.GrouperWsVersion, WsSubjectLookup[], edu.internet2.middleware.grouper.ws.member.WsMemberFilter, WsSubjectLookup, boolean, boolean, String[], WsParam[])
 * for method
 */
public class WsRestStemSaveRequest implements WsRequestBean {
  
  /** field */
  private WsStemToSave[] wsStemToSaves;
  
  /** field */
  private String txType;
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private WsParam[] params;
  
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
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }
  
  /**
   * field
   * @return the txType
   */
  public String getTxType() {
    return this.txType;
  }


  
  /**
   * field
   * @param txType1 the txType to set
   */
  public void setTxType(String txType1) {
    this.txType = txType1;
  }


  
  /**
   * @return the wsStemToSaves
   */
  public WsStemToSave[] getWsStemToSaves() {
    return this.wsStemToSaves;
  }


  
  /**
   * @param wsStemToSaves1 the wsStemToSaves to set
   */
  public void setWsStemToSaves(WsStemToSave[] wsStemToSaves1) {
    this.wsStemToSaves = wsStemToSaves1;
  }

}

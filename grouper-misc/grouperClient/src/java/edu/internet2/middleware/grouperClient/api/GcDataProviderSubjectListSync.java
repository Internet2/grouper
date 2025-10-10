package edu.internet2.middleware.grouperClient.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClient.ws.GrouperClientWs;
import edu.internet2.middleware.grouperClient.ws.beans.WsDataProviderSubjectListSyncResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsRestDataProviderSubjectListSyncRequest;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.morphString.Crypto;


/**
 * class to sync a list of subjects from a data provider
 */
public class GcDataProviderSubjectListSync {


  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   */
  private String wsEndpoint;

  /**
   * endpoint to grouper WS, e.g. https://server.school.edu/grouper-ws/servicesRest
   * @param theWsEndpoint
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignWsEndpoint(String theWsEndpoint) {
    this.wsEndpoint = theWsEndpoint;
    return this;
  }
  
  /**
   * ws user
   */
  private String wsUser;

  /**
   * ws user
   * @param theWsUser
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignWsUser(String theWsUser) {
    this.wsUser = theWsUser;
    return this;
  }
  
  /**
   * ws pass
   */
  private String wsPass;

  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignWsPass(String theWsPass) {
    this.wsPass = theWsPass;
    return this;
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignWsPassEncrypted(String theWsPassEncrypted) {
    String encryptKey = GrouperClientUtils.encryptKey();
    return this.assignWsPass(new Crypto(encryptKey).decrypt(theWsPassEncrypted));
  }
  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignWsPassFile(File theFile) {
    return this.assignWsPass(GrouperClientUtils.readFileIntoString(theFile));
  }

  
  /**
   * ws pass
   * @param theWsPass
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignWsPassFileEncrypted(File theFile) {
    return this.assignWsPassEncrypted(GrouperClientUtils.readFileIntoString(theFile));
  }

  /** act as subject if any */
  private WsSubjectLookup actAsSubject;

  /**
   * assign the act as subject if any
   * @param theActAsSubject
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignActAsSubject(WsSubjectLookup theActAsSubject) {
    this.actAsSubject = theActAsSubject;
    return this;
  }
  
  private String dataProviderConfigId;
  
  /**
   * assign the data provider config id
   * @param theDataProviderConfigId
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignDataProviderConfigId(String theDataProviderConfigId) {
    this.dataProviderConfigId = theDataProviderConfigId;
    return this;
  }
  
  /** subject lookups */
  private List<WsSubjectLookup> subjectLookups = new ArrayList<WsSubjectLookup>();
  
  /** 
   * add a subject lookup
   * @param wsSubjectLookup
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync addSubjectLookup(WsSubjectLookup wsSubjectLookup) {
    this.subjectLookups.add(wsSubjectLookup);
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectId
   * @param sourceId
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync addSubjectId(String subjectId, String sourceId) {
    this.subjectLookups.add(new WsSubjectLookup(subjectId, sourceId, null));
    return this;
  }
  
  /** 
   * add a subject lookup
   * @param subjectIdentifier
   * @param sourceId
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync addSubjectIdentifier(String subjectIdentifier, String sourceId) {
    this.subjectLookups.add(new WsSubjectLookup(null, sourceId, subjectIdentifier));
    return this;
  }
  
  /**
   * validate this call
   */
  private void validate() {
    if (GrouperClientUtils.isBlank(this.dataProviderConfigId)) {
      throw new RuntimeException("dataProviderConfigId is required: " + this);
    }
    if (GrouperClientUtils.length(this.subjectLookups) == 0) {
      throw new RuntimeException("Need at least one subject: " + this);
    }
  }
  
  /** client version */
  private String clientVersion;

  /**
   * assign client version
   * @param theClientVersion
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignClientVersion(String theClientVersion) {
    this.clientVersion = theClientVersion;
    return this;
  }

  /** content type for post request */
  private String contentType;

  /**
   * content type for post request
   * @param theContentType
   * @return this for chaining
   */
  public GcDataProviderSubjectListSync assignContentType(String theContentType) {
    this.contentType = theContentType;
    return this;
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public WsDataProviderSubjectListSyncResult execute() {
    this.validate();
    WsDataProviderSubjectListSyncResult wsDataProviderSubjectListSyncResult = null;
    try {
      //Make the body of the request, in this case with beans and marshaling, but you can make
      //your request document in whatever language or way you want
      WsRestDataProviderSubjectListSyncRequest dataProviderSubjectListSync = new WsRestDataProviderSubjectListSyncRequest();

      dataProviderSubjectListSync.setActAsSubjectLookup(this.actAsSubject);
      dataProviderSubjectListSync.setDataProviderConfigId(this.dataProviderConfigId);

      WsSubjectLookup[] subjectLookupsResults = GrouperClientUtils.toArray(this.subjectLookups, WsSubjectLookup.class);
      dataProviderSubjectListSync.setSubjectLookups(subjectLookupsResults);
      
      GrouperClientWs grouperClientWs = new GrouperClientWs();

      grouperClientWs.assignWsUser(this.wsUser);
      grouperClientWs.assignWsPass(this.wsPass);
      grouperClientWs.assignWsEndpoint(this.wsEndpoint);
      
      //kick off the web service
      wsDataProviderSubjectListSyncResult = (WsDataProviderSubjectListSyncResult)
          grouperClientWs.executeService("dataProviderSubjectListSync", dataProviderSubjectListSync, "dataProviderSubjectListSync",
              this.clientVersion, this.contentType, false);
      
      String resultMessage = wsDataProviderSubjectListSyncResult.getResultMetadata().getResultMessage();
      grouperClientWs.handleFailure(wsDataProviderSubjectListSyncResult, null, resultMessage);
      
    } catch (Exception e) {
      GrouperClientUtils.convertToRuntimeException(e);
    }
    return wsDataProviderSubjectListSyncResult;
  }
  
}

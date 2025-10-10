package edu.internet2.middleware.grouperClient.ws.beans;

/**
 * request bean for rest data provider subject list sync request
 */
public class WsRestDataProviderSubjectListSyncRequest implements WsRequestBean {


  private WsSubjectLookup actAsSubjectLookup;
  private String clientVersion;
  private String dataProviderConfigId;
  private WsSubjectLookup[] subjectLookups;

  /**
   * @return the actAsSubjectLookup
   */
  public WsSubjectLookup getActAsSubjectLookup() {
    return this.actAsSubjectLookup;
  }

  /**
   * @return the clientVersion
   */
  public String getClientVersion() {
    return this.clientVersion;
  }

  /**
   * @param actAsSubjectLookup1 the actAsSubjectLookup to set
   */
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup1) {
    this.actAsSubjectLookup = actAsSubjectLookup1;
  }

  /**
   * @param clientVersion1 the clientVersion to set
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }

  
  public String getDataProviderConfigId() {
    return dataProviderConfigId;
  }

  
  public void setDataProviderConfigId(String dataProviderConfigId) {
    this.dataProviderConfigId = dataProviderConfigId;
  }

  
  public WsSubjectLookup[] getSubjectLookups() {
    return subjectLookups;
  }

  
  public void setSubjectLookups(WsSubjectLookup[] subjectLookups) {
    this.subjectLookups = subjectLookups;
  }
}

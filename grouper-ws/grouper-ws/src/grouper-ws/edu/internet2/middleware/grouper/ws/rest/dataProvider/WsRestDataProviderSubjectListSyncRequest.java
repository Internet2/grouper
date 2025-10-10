package edu.internet2.middleware.grouper.ws.rest.dataProvider;

import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel(description = "bean that will be the data from rest request for executing a data provider sync based on a list of subjects<br /><br /><b>dataProviderConfigId</b>: The config id for the data provider<br />"
    + "<br /><br /><b>subjectLookups</b>: subjects to sync<br />")
public class WsRestDataProviderSubjectListSyncRequest implements WsRequestBean {
  
  private String clientVersion;

  private WsSubjectLookup actAsSubjectLookup;

  private String dataProviderConfigId;
  
  private WsSubjectLookup[] subjectLookups;
  
  @ApiModelProperty(value = "config id for the data provider", example = "myConfigId")
  public String getDataProviderConfigId() {
    return this.dataProviderConfigId;
  }

  
  public void setDataProviderConfigId(String dataProviderConfigId) {
    this.dataProviderConfigId = dataProviderConfigId;
  }

  /**
   * @return the subjectLookups
   */
  public WsSubjectLookup[] getSubjectLookups() {
    return this.subjectLookups;
  }

  
  /**
   * @param subjectLookups1 the subjectLookups to set
   */
  public void setSubjectLookups(WsSubjectLookup[] subjectLookups1) {
    this.subjectLookups = subjectLookups1;
  }


  @ApiModelProperty(value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001")
  public String getClientVersion(){
    return clientVersion;
  }



  
  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }



  
  public WsSubjectLookup getActAsSubjectLookup() {
    return actAsSubjectLookup;
  }



  
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup) {
    this.actAsSubjectLookup = actAsSubjectLookup;
  }


  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }

}

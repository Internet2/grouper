package edu.internet2.middleware.grouper.ws.rest.dataProvider;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to execute a data provider sync based on a list of subjects")
public class WsRestDataProviderSubjectListSyncRequestWrapper {

  private WsRestDataProviderSubjectListSyncRequest wsRestDataProviderSubjectListSyncRequest;
  
  @ApiModelProperty(name = "WsRestDataProviderSubjectListSyncRequest", value = "Identifies the request as an execute data provider sync request")
  public WsRestDataProviderSubjectListSyncRequest getWsRestDataProviderSubjectListSyncRequest() {
    return wsRestDataProviderSubjectListSyncRequest;
  }

  
  public void setWsRestDataProviderSubjectListSyncRequest(
      WsRestDataProviderSubjectListSyncRequest wsRestDataProviderSubjectListSyncRequest) {
    this.wsRestDataProviderSubjectListSyncRequest = wsRestDataProviderSubjectListSyncRequest;
  }
  
  
  
}

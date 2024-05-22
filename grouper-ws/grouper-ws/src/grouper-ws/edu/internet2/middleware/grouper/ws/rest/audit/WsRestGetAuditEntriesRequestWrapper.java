package edu.internet2.middleware.grouper.ws.rest.audit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to get audit entries")
public class WsRestGetAuditEntriesRequestWrapper {

  private WsRestGetAuditEntriesRequest wsRestGetAuditEntriesRequest;
  
  @ApiModelProperty(name = "WsRestGetAuditEntriesRequest", value = "Identifies the request as a get audit entries request")
  public WsRestGetAuditEntriesRequest getWsRestGetAuditEntriesRequest() {
    return wsRestGetAuditEntriesRequest;
  }

  
  public void setWsRestGetAuditEntriesRequest(
      WsRestGetAuditEntriesRequest wsRestGetAuditEntriesRequest) {
    this.wsRestGetAuditEntriesRequest = wsRestGetAuditEntriesRequest;
  }
  
  
  
}

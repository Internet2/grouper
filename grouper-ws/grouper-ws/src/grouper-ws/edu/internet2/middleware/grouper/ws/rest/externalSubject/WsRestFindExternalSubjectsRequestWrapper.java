package edu.internet2.middleware.grouper.ws.rest.externalSubject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to find external subjects")
public class WsRestFindExternalSubjectsRequestWrapper {

  private WsRestFindExternalSubjectsRequest wsRestFindExternalSubjectsRequest;
  
  @ApiModelProperty(name = "WsRestFindExternalSubjectsRequest", value = "Identifies the request as a find external subjects request")
  public WsRestFindExternalSubjectsRequest getWsRestFindExternalSubjectsRequest() {
    return wsRestFindExternalSubjectsRequest;
  }

  
  public void setWsRestFindExternalSubjectsRequest(
      WsRestFindExternalSubjectsRequest wsRestFindExternalSubjectsRequest) {
    this.wsRestFindExternalSubjectsRequest = wsRestFindExternalSubjectsRequest;
  }
  
  
  
}

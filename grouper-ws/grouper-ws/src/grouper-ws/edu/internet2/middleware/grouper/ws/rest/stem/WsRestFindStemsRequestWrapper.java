package edu.internet2.middleware.grouper.ws.rest.stem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to find stems")
public class WsRestFindStemsRequestWrapper {

  private WsRestFindStemsRequest wsRestFindStemsRequest;
  
  @ApiModelProperty(name = "WsRestFindStemsRequest", value = "Identifies the request as a find stems request")
  public WsRestFindStemsRequest getWsRestFindStemsRequest() {
    return wsRestFindStemsRequest;
  }

  
  public void setWsRestFindStemsRequest(
      WsRestFindStemsRequest wsRestFindStemsRequest) {
    this.wsRestFindStemsRequest = wsRestFindStemsRequest;
  }
  
  
  
}

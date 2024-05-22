package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to find attribute defs")
public class WsRestFindAttributeDefsRequestWrapper {

  private WsRestFindAttributeDefsRequest wsRestFindAttributeDefsRequest;
  
  @ApiModelProperty(name = "WsRestFindAttributeDefsRequest", value = "Identifies the request as a find attribute defs request")
  public WsRestFindAttributeDefsRequest getWsRestFindAttributeDefsRequest() {
    return wsRestFindAttributeDefsRequest;
  }

  
  public void setWsRestFindAttributeDefsRequest(
      WsRestFindAttributeDefsRequest wsRestFindAttributeDefsRequest) {
    this.wsRestFindAttributeDefsRequest = wsRestFindAttributeDefsRequest;
  }
  
  
  
}

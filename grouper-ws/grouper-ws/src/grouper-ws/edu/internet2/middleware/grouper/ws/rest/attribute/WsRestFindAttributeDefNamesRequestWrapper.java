package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to delete find attribute def names")
public class WsRestFindAttributeDefNamesRequestWrapper {

  private WsRestFindAttributeDefNamesRequest wsRestFindAttributeDefNamesRequest;
  
  @ApiModelProperty(name = "WsRestFindAttributeDefNamesRequest", value = "Identifies the request as a find attribute def names request")
  public WsRestFindAttributeDefNamesRequest getWsRestFindAttributeDefNamesRequest() {
    return wsRestFindAttributeDefNamesRequest;
  }

  
  public void setWsRestFindAttributeDefNamesRequest(
      WsRestFindAttributeDefNamesRequest wsRestFindAttributeDefNamesRequest) {
    this.wsRestFindAttributeDefNamesRequest = wsRestFindAttributeDefNamesRequest;
  }
  
  
  
}

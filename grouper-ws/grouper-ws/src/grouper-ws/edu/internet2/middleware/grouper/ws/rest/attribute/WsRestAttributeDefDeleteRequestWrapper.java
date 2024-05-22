package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to delete attribute defs")
public class WsRestAttributeDefDeleteRequestWrapper {

  private WsRestAttributeDefDeleteRequest wsRestAttributeDefDeleteRequest;
  
  @ApiModelProperty(name = "WsRestAttributeDefDeleteRequest", value = "Identifies the request as an attribute delete request")
  public WsRestAttributeDefDeleteRequest getWsRestAttributeDefDeleteRequest() {
    return wsRestAttributeDefDeleteRequest;
  }

  
  public void setWsRestAttributeDefDeleteRequest(
      WsRestAttributeDefDeleteRequest wsRestAttributeDefDeleteRequest) {
    this.wsRestAttributeDefDeleteRequest = wsRestAttributeDefDeleteRequest;
  }
  
  
  
}

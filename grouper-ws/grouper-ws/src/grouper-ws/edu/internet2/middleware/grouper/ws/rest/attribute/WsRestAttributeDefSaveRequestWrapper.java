package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to save attribute defs")
public class WsRestAttributeDefSaveRequestWrapper {

  private WsRestAttributeDefSaveRequest wsRestAttributeDefSaveRequest;
  
  @ApiModelProperty(name = "WsRestAttributeDefSaveRequest", value = "Identifies the request as an attribute save request")
  public WsRestAttributeDefSaveRequest getWsRestAttributeDefSaveRequest() {
    return wsRestAttributeDefSaveRequest;
  }

  
  public void setWsRestAttributeDefSaveRequest(
      WsRestAttributeDefSaveRequest wsRestAttributeDefSaveRequest) {
    this.wsRestAttributeDefSaveRequest = wsRestAttributeDefSaveRequest;
  }
  
  
  
}

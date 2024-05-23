package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to save attribute def names")
public class WsRestAttributeDefNameSaveRequestWrapper {

  private WsRestAttributeDefNameSaveRequest wsRestAttributeDefNameSaveRequest;
  
  @ApiModelProperty(name = "WsRestAttributeDefNameSaveRequest", value = "Identifies the request as a save attribute def names request")
  public WsRestAttributeDefNameSaveRequest getWsRestAttributeDefNameSaveRequest() {
    return wsRestAttributeDefNameSaveRequest;
  }

  
  public void setWsRestAttributeDefNameSaveRequest(
      WsRestAttributeDefNameSaveRequest wsRestAttributeDefNameSaveRequest) {
    this.wsRestAttributeDefNameSaveRequest = wsRestAttributeDefNameSaveRequest;
  }
  
  
  
}

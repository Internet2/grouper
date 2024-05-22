package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to assign attribute def actions")
public class WsRestAssignAttributeDefActionsRequestWrapper {

  private WsRestAssignAttributeDefActionsRequest wsRestAssignAttributeDefActionsRequest;
  
  @ApiModelProperty(name = "WsRestAssignAttributeDefActionsRequest", value = "Identifies the request as an assign attribute def actions request")
  public WsRestAssignAttributeDefActionsRequest getWsRestAssignAttributeDefActionsRequest() {
    return wsRestAssignAttributeDefActionsRequest;
  }

  
  public void setWsRestAssignAttributeDefActionsRequest(
      WsRestAssignAttributeDefActionsRequest wsRestAssignAttributeDefActionsRequest) {
    this.wsRestAssignAttributeDefActionsRequest = wsRestAssignAttributeDefActionsRequest;
  }
  
  
  
}

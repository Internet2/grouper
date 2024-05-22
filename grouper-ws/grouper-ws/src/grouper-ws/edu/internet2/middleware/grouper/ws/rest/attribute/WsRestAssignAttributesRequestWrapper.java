package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to assign attributes")
public class WsRestAssignAttributesRequestWrapper {

  private WsRestAssignAttributesRequest wsRestAssignAttributesRequest;
  
  @ApiModelProperty(name = "WsRestAssignAttributesRequest", value = "Identifies the request as an assign attributes request")
  public WsRestAssignAttributesRequest getWsRestAssignAttributesRequest() {
    return wsRestAssignAttributesRequest;
  }

  
  public void setWsRestAssignAttributesRequest(
      WsRestAssignAttributesRequest wsRestAssignAttributesRequest) {
    this.wsRestAssignAttributesRequest = wsRestAssignAttributesRequest;
  }
  
  
  
}

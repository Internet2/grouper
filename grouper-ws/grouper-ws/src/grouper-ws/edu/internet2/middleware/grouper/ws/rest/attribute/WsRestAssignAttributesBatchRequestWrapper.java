package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to assign attribute batch")
public class WsRestAssignAttributesBatchRequestWrapper {

  private WsRestAssignAttributesBatchRequest wsRestAssignAttributesBatchRequest;
  
  @ApiModelProperty(name = "WsRestAssignAttributesBatchRequest", value = "Identifies the request as an assign attribute batch request")
  public WsRestAssignAttributesBatchRequest getWsRestAssignAttributesBatchRequest() {
    return wsRestAssignAttributesBatchRequest;
  }

  
  public void setWsRestAssignAttributesBatchRequest(
      WsRestAssignAttributesBatchRequest wsRestAssignAttributesBatchRequest) {
    this.wsRestAssignAttributesBatchRequest = wsRestAssignAttributesBatchRequest;
  }
  
  
  
}

package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to assign attribute def name inheritence")
public class WsRestAssignAttributeDefNameInheritanceRequestWrapper {

  private WsRestAssignAttributeDefNameInheritanceRequest wsRestAssignAttributeDefNameInheritanceRequest;
  
  @ApiModelProperty(name = "WsRestAttributeDefDeleteRequest", value = "Identifies the request as an assign attribute def name inheritence request")
  public WsRestAssignAttributeDefNameInheritanceRequest getWsRestAssignAttributeDefNameInheritanceRequest() {
    return wsRestAssignAttributeDefNameInheritanceRequest;
  }

  
  public void setWsRestAssignAttributeDefNameInheritanceRequest(
      WsRestAssignAttributeDefNameInheritanceRequest wsRestAssignAttributeDefNameInheritanceRequest) {
    this.wsRestAssignAttributeDefNameInheritanceRequest = wsRestAssignAttributeDefNameInheritanceRequest;
  }
  
  
  
}

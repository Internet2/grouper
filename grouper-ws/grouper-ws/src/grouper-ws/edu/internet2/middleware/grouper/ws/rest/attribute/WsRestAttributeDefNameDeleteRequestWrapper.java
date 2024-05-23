package edu.internet2.middleware.grouper.ws.rest.attribute;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to delete attribute def names")
public class WsRestAttributeDefNameDeleteRequestWrapper {

  private WsRestAttributeDefNameDeleteRequest wsRestAttributeDefNameDeleteRequest;
  
  @ApiModelProperty(name = "WsRestAttributeDefNameDeleteRequest", value = "Identifies the request as an attribute def name delete request")
  public WsRestAttributeDefNameDeleteRequest getWsRestAttributeDefDeleteRequest() {
    return wsRestAttributeDefNameDeleteRequest;
  }

  
  public void setWsRestAttributeDefNameDeleteRequest(
      WsRestAttributeDefNameDeleteRequest wsRestAttributeDefNameDeleteRequest) {
    this.wsRestAttributeDefNameDeleteRequest = wsRestAttributeDefNameDeleteRequest;
  }
  
  
  
}

package edu.internet2.middleware.grouper.ws.rest.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to delete a group")
public class WsRestGroupDeleteRequestWrapper {

  private WsRestGroupDeleteRequest wsRestGroupDeleteRequest;
  
  @ApiModelProperty(name = "WsRestGroupDeleteRequest", value = "Identifies the request as a delete groups request")
  public WsRestGroupDeleteRequest getWsRestGroupDeleteRequest() {
    return wsRestGroupDeleteRequest;
  }
  public void setWsRestGroupDeleteRequest(WsRestGroupDeleteRequest wsRestGroupDeleteRequest1) {
    wsRestGroupDeleteRequest = wsRestGroupDeleteRequest1;
  }
}

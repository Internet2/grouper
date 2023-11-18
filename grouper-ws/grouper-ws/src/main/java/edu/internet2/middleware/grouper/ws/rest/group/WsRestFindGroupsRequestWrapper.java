package edu.internet2.middleware.grouper.ws.rest.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to find groups")
public class WsRestFindGroupsRequestWrapper {

  private WsRestFindGroupsRequest wsRestFindGroupsRequest;
  
  @ApiModelProperty(name = "WsRestFindGroupsRequest", value = "Identifies the request as a findGroups request")
  public WsRestFindGroupsRequest getWsRestFindGroupsRequest() {
    return wsRestFindGroupsRequest;
  }
  public void setWsRestFindGroupsRequest(WsRestFindGroupsRequest wsRestFindGroupsRequest1) {
    wsRestFindGroupsRequest = wsRestFindGroupsRequest1;
  }
}

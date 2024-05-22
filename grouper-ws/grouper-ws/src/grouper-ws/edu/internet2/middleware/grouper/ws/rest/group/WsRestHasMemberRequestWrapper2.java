package edu.internet2.middleware.grouper.ws.rest.group;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to run hasMember")
public class WsRestHasMemberRequestWrapper2 {

  private WsRestHasMemberRequest wsRestHasMemberRequest;
  
  @ApiModelProperty(name = "WsRestHasMemberRequest", value = "Identifies the request as a hasMember request")
  public WsRestHasMemberRequest getWsRestHasMemberRequest() {
    return wsRestHasMemberRequest;
  }
  public void setWsRestHasMemberRequest(WsRestHasMemberRequest wsRestHasMemberRequest1) {
    wsRestHasMemberRequest = wsRestHasMemberRequest1;
  }
}

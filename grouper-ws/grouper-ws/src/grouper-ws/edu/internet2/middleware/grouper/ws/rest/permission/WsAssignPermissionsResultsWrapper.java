package edu.internet2.middleware.grouper.ws.rest.permission;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignPermissionsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignPermissionsResultsWrapper {
  WsAssignPermissionsResults WsAssignPermissionsResults = null;

  @ApiModelProperty(name = "WsAssignPermissionsResults", value = "Identifies the response of an assign permissions request")
  public WsAssignPermissionsResults getWsAssignPermissionsResults() {
    return WsAssignPermissionsResults;
  }

  
  public void setWsAssignPermissionsResults(WsAssignPermissionsResults wsAssignPermissionsResults) {
    WsAssignPermissionsResults = wsAssignPermissionsResults;
  }
  

}

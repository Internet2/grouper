package edu.internet2.middleware.grouper.ws.rest.membership;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetMembershipsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetMembershipsResultsWrapper {
  WsGetMembershipsResults WsGetMembershipsResults = null;

  @ApiModelProperty(name = "WsGetMembershipsResults", value = "Identifies the response of a get Memberships request")
  public WsGetMembershipsResults getWsGetMembershipsResults() {
    return WsGetMembershipsResults;
  }

  
  public void setWsGetMembershipsResults(WsGetMembershipsResults wsGetMembershipsResults) {
    WsGetMembershipsResults = wsGetMembershipsResults;
  }
  

}

package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignAttributesResultsWrapper {
  WsAssignAttributesResults WsAssignAttributesResults = null;

  @ApiModelProperty(name = "WsAssignAttributesResults", value = "Identifies the response of an assign attributes  request")
  public WsAssignAttributesResults getWsAssignAttributesResults() {
    return WsAssignAttributesResults;
  }

  
  public void setWsAssignAttributesResults(WsAssignAttributesResults wsAssignAttributesResults) {
    WsAssignAttributesResults = wsAssignAttributesResults;
  }
  

}

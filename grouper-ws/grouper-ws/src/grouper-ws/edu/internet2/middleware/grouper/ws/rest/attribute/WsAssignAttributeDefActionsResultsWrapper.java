package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefAssignActionResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignAttributeDefActionsResultsWrapper {
  WsAttributeDefAssignActionResults WsAssignAttributeDefActionsResults = null;

  @ApiModelProperty(name = "WsAssignAttributeDefActionsResults", value = "Identifies the response of an assign attribute def actions request")
  public WsAttributeDefAssignActionResults getWsAssignAttributeDefActionsResults() {
    return WsAssignAttributeDefActionsResults;
  }

  
  public void setWsAssignAttributeDefActionsResults(WsAttributeDefAssignActionResults wsAssignAttributeDefActionsResults) {
    WsAssignAttributeDefActionsResults = wsAssignAttributeDefActionsResults;
  }
  

}

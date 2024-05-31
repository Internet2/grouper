package edu.internet2.middleware.grouper.ws.rest.attribute;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignActionsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetAttributeAssignActionsResultsWrapper {
  WsGetAttributeAssignActionsResults WsGetAttributeAssignActionsResults = null;

  @ApiModelProperty(name = "WsGetAttributeAssignActionsResults", value = "Identifies the response of a get attribute assign actions request")
  WsGetAttributeAssignActionsResults getWsGetAttributeAssignActionsResults() {
    return WsGetAttributeAssignActionsResults;
  }

  
  public void setWsGetAttributeAssignActionsResults(WsGetAttributeAssignActionsResults wsGetAttributeAssignActionsResults) {
    WsGetAttributeAssignActionsResults = wsGetAttributeAssignActionsResults;
  }
  

}

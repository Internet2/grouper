package edu.internet2.middleware.grouper.ws.rest.attribute;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetAttributeAssignmentsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetAttributeAssignmentsResultsWrapper {
  WsGetAttributeAssignmentsResults WsGetAttributeAssignmentsResults = null;

  @ApiModelProperty(name = "WsGetAttributeAssignmentsResults", value = "Identifies the response of a get attribute assignments request")
  public WsGetAttributeAssignmentsResults getWsGetAttributeAssignmentsResults() {
    return WsGetAttributeAssignmentsResults;
  }

  
  public void setWsGetAttributeAssignmentsResults(WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults) {
    WsGetAttributeAssignmentsResults = wsGetAttributeAssignmentsResults;
  }
  

}

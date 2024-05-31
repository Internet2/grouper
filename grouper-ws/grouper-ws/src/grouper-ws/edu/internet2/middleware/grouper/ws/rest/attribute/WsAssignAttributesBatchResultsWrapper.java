package edu.internet2.middleware.grouper.ws.rest.attribute;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesBatchResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignAttributesBatchResultsWrapper {
  WsAssignAttributesBatchResults WsAssignAttributesBatchResults = null;

  @ApiModelProperty(name = "WsAssignAttributesBatchResults", value = "Identifies the response of an assign attributes batch request")
  public WsAssignAttributesBatchResults getWsAssignAttributesBatchResults() {
    return WsAssignAttributesBatchResults;
  }

  
  public void setWsAssignAttributesBatchResults(WsAssignAttributesBatchResults wsAssignAttributesBatchResults) {
    WsAssignAttributesBatchResults = wsAssignAttributesBatchResults;
  }
  

}

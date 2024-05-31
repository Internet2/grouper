package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributesLiteResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAssignAttributesLiteResultsWrapper {
  WsAssignAttributesLiteResults WsAssignAttributesLiteResults = null;

  @ApiModelProperty(name = "WsAssignAttributesLiteResults", value = "Identifies the response of an assign attributes lite request")
  public WsAssignAttributesLiteResults getWsAssignAttributesLiteResults() {
    return WsAssignAttributesLiteResults;
  }

  
  public void setWsAssignAttributesLiteResults(WsAssignAttributesLiteResults wsAssignAttributesLiteResults) {
    WsAssignAttributesLiteResults = wsAssignAttributesLiteResults;
  }
  

}

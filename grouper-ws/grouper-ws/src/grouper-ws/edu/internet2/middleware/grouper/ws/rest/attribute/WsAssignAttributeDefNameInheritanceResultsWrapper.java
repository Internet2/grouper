package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeBatchResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsAssignAttributeDefNameInheritanceResults;

import io.swagger.annotations.ApiModelProperty;

public class WsAssignAttributeDefNameInheritanceResultsWrapper {
  WsAssignAttributeDefNameInheritanceResults WsAssignAttributeDefNameinheritanceResults = null;

  @ApiModelProperty(name = "WsAssignAttributeDefNameinheritanceResults", value = "Identifies the response of an assign attribute def name inheritance request")
  public WsAssignAttributeDefNameInheritanceResults getWsAssignAttributeDefNameinheritanceResults() {
    return WsAssignAttributeDefNameinheritanceResults;
  }

  
  public void setWsAssignAttributeDefNameinheritanceResults(WsAssignAttributeDefNameInheritanceResults wsAssignAttributeDefNameinheritanceResults) {
    WsAssignAttributeDefNameinheritanceResults = wsAssignAttributeDefNameinheritanceResults;
  }
  

}

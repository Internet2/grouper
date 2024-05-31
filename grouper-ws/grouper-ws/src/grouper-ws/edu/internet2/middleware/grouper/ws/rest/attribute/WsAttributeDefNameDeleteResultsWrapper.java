package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefNameDeleteResultsWrapper {
  WsAttributeDefNameDeleteResults WsAttributeDefNameDeleteResults = null;

  @ApiModelProperty(name = "WsAttributeDefNameDeleteResults", value = "Identifies the response of an attribute def name delete request")
  public WsAttributeDefNameDeleteResults getWsAttributeDefNameDeleteResults() {
    return WsAttributeDefNameDeleteResults;
  }

  
  public void setWsAttributeDefNameDeleteResults(WsAttributeDefNameDeleteResults wsAttributeDefNameDeleteResults) {
    WsAttributeDefNameDeleteResults = wsAttributeDefNameDeleteResults;
  }
  

}

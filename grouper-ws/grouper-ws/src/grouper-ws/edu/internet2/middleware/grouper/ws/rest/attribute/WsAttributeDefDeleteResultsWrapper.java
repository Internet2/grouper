package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefDeleteResultsWrapper {
  WsAttributeDefDeleteResults WsAttributeDefDeleteResults = null;

  @ApiModelProperty(name = "WsAttributeDefDeleteResults", value = "Identifies the response of an attribute def delete request")
  public WsAttributeDefDeleteResults getWsAttributeDefDeleteResults() {
    return WsAttributeDefDeleteResults;
  }

  
  public void setWsAttributeDefDeleteResults(WsAttributeDefDeleteResults wsAttributeDefDeleteResults) {
    WsAttributeDefDeleteResults = wsAttributeDefDeleteResults;
  }
  

}

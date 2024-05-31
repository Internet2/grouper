package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefSaveLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefSaveLiteResultsWrapper {
  WsAttributeDefSaveLiteResult WsAttributeDefSaveLiteResult = null;

  @ApiModelProperty(name = "WsAttributeDefSaveLiteResult", value = "Identifies the response of an attribute def save lite request")
  public WsAttributeDefSaveLiteResult getWsAttributeDefSaveLiteResults() {
    return WsAttributeDefSaveLiteResult;
  }

  
  public void setWsAttributeDefSaveLiteResults(WsAttributeDefSaveLiteResult wsAttributeDefSaveLiteResult) {
    WsAttributeDefSaveLiteResult = wsAttributeDefSaveLiteResult;
  }
  

}

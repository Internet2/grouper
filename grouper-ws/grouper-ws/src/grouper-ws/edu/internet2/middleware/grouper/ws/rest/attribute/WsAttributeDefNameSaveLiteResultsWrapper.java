package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefNameSaveLiteResultsWrapper {
  WsAttributeDefNameSaveLiteResult WsAttributeDefNameSaveLiteResult = null;

  @ApiModelProperty(name = "WsAttributeDefNameSaveLiteResult", value = "Identifies the response of an attribute def name save lite request")
  public WsAttributeDefNameSaveLiteResult getWsAttributeDefNameSaveLiteResult() {
    return WsAttributeDefNameSaveLiteResult;
  }

  
  public void setWsAttributeDefNameSaveLiteResult(WsAttributeDefNameSaveLiteResult wsAttributeDefNameSaveLiteResult) {
    WsAttributeDefNameSaveLiteResult = wsAttributeDefNameSaveLiteResult;
  }
  

}

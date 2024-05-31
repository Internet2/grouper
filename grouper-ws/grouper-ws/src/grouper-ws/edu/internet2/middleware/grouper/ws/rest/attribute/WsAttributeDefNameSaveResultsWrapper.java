package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameSaveResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefNameSaveResultsWrapper {
  WsAttributeDefNameSaveResults WsAttributeDefNameSaveResults = null;

  @ApiModelProperty(name = "WsAttributeDefNameSaveResults", value = "Identifies the response of an attribute def name save  request")
  public WsAttributeDefNameSaveResults getWsAttributeDefNameSaveResults() {
    return WsAttributeDefNameSaveResults;
  }

  
  public void setWsAttributeDefNameSaveResults(WsAttributeDefNameSaveResults wsAttributeDefNameSaveResults) {
    WsAttributeDefNameSaveResults = wsAttributeDefNameSaveResults;
  }
  

}

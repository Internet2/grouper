package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefSaveResults;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefSaveResultsWrapper {
  WsAttributeDefSaveResults WsAttributeDefSaveResults = null;

  @ApiModelProperty(name = "WsAttributeDefSaveResults", value = "Identifies the response of an attribute def save  request")
  public WsAttributeDefSaveResults getWsAttributeDefSaveResults() {
    return WsAttributeDefSaveResults;
  }

  
  public void setWsAttributeDefSaveResults(WsAttributeDefSaveResults wsAttributeDefSaveResults) {
    WsAttributeDefSaveResults = wsAttributeDefSaveResults;
  }
  

}

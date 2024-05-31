package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameDeleteLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefNameDeleteLiteResultsWrapper {
  WsAttributeDefNameDeleteLiteResult WsAttributeDefNameDeleteLiteResult = null;

  @ApiModelProperty(name = "WsAttributeDefNameDeleteLiteResult", value = "Identifies the response of an attribute def Name Delete lite request")
  public WsAttributeDefNameDeleteLiteResult getWsAttributeDefNameDeleteLiteResult() {
    return WsAttributeDefNameDeleteLiteResult;
  }

  
  public void setWsAttributeDefNameDeleteLiteResults(WsAttributeDefNameDeleteLiteResult wsAttributeDefNameDeleteLiteResult) {
    WsAttributeDefNameDeleteLiteResult = wsAttributeDefNameDeleteLiteResult;
  }
  

}

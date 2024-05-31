package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefDeleteLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsAttributeDefDeleteLiteResultsWrapper {
  WsAttributeDefDeleteLiteResult WsAttributeDefDeleteLiteResult = null;

  @ApiModelProperty(name = "WsAttributeDefDeleteLiteResults", value = "Identifies the response of an attribute def delete lite request")
  public WsAttributeDefDeleteLiteResult getWsAttributeDefDeleteLiteResults() {
    return WsAttributeDefDeleteLiteResult;
  }

  
  public void setWsAttributeDefDeleteLiteResults(WsAttributeDefDeleteLiteResult wsAttributeDefDeleteLiteResults) {
    WsAttributeDefDeleteLiteResult = wsAttributeDefDeleteLiteResults;
  }
  

}

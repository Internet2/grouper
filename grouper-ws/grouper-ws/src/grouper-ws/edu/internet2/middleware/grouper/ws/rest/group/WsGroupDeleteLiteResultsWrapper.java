package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsGroupDeleteLiteResultsWrapper {
  WsGroupDeleteLiteResult WsGroupDeleteLiteResult = null;

  @ApiModelProperty(name = "WsGroupDeleteLiteResult", value = "Identifies the response of a group delete Lite request")
  public WsGroupDeleteLiteResult getWsGroupDeleteLiteResults() {
    return WsGroupDeleteLiteResult;
  }

  
  public void setWsGroupDeleteLiteResults(WsGroupDeleteLiteResult wsGroupDeleteLiteResult) {
    WsGroupDeleteLiteResult = wsGroupDeleteLiteResult;
  }
  

}

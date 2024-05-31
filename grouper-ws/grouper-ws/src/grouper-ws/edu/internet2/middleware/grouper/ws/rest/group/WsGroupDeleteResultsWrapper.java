package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupDeleteResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGroupDeleteResultsWrapper {
  WsGroupDeleteResults WsGroupDeleteResults = null;

  @ApiModelProperty(name = "WsGroupDeleteResults", value = "Identifies the response of a group delete request")
  public WsGroupDeleteResults getWsGroupDeleteResults() {
    return WsGroupDeleteResults;
  }

  
  public void setWsGroupDeleteResults(WsGroupDeleteResults wsGroupDeleteResults) {
    WsGroupDeleteResults = wsGroupDeleteResults;
  }
  

}

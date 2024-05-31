package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsGroupSaveLiteResultsWrapper {
  WsGroupSaveLiteResult WsGroupSaveLiteResults = null;

  @ApiModelProperty(name = "WsGroupSaveLiteResult", value = "Identifies the response of a group Save Lite request")
  public WsGroupSaveLiteResult getWsGroupSaveLiteResults() {
    return WsGroupSaveLiteResults;
  }

  
  public void setWsGroupSaveLiteResults(WsGroupSaveLiteResult wsGroupSaveLiteResults) {
    WsGroupSaveLiteResults = wsGroupSaveLiteResults;
  }
  

}

package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupSaveResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGroupSaveResultsWrapper {
  WsGroupSaveResults WsGroupSaveResults = null;

  @ApiModelProperty(name = "WsGroupSaveResults", value = "Identifies the response of a group save request")
  public WsGroupSaveResults getWsGroupSaveResults() {
    return WsGroupSaveResults;
  }

  
  public void setWsGroupSaveResults(WsGroupSaveResults wsGroupSaveResults) {
    WsGroupSaveResults = wsGroupSaveResults;
  }
  

}

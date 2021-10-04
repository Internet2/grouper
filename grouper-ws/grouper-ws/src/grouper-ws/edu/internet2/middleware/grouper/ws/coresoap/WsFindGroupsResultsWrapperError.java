package edu.internet2.middleware.grouper.ws.coresoap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response body when not successful contains error message and metadata")
public class WsFindGroupsResultsWrapperError {
  private WsResultsError wsFindGroupsResults;

  @ApiModelProperty(name = "WsFindGroupsResults", value = "Identifies the response for findGroups")
  public WsResultsError getWsFindGroupsResults() {
    return wsFindGroupsResults;
  }
  public void setWsFindGroupsResults(WsResultsError wsFindGroupsResults1) {
    this.wsFindGroupsResults = wsFindGroupsResults1;
  }
}

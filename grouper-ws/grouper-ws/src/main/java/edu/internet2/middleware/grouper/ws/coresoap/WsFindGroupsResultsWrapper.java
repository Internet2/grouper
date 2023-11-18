package edu.internet2.middleware.grouper.ws.coresoap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response body when a findGroups is successful")
public class WsFindGroupsResultsWrapper {
  private WsFindGroupsResults wsFindGroupsResults;

  @ApiModelProperty(name = "WsFindGroupsResults", value = "Identifies the response for findGroups")
  public WsFindGroupsResults getWsFindGroupsResults() {
    return wsFindGroupsResults;
  }
  public void setWsFindGroupsResults(WsFindGroupsResults wsFindGroupsResults) {
    this.wsFindGroupsResults = wsFindGroupsResults;
  }
}

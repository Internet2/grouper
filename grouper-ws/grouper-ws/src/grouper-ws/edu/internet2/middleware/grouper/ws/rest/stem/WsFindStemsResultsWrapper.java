package edu.internet2.middleware.grouper.ws.rest.stem;

import edu.internet2.middleware.grouper.ws.coresoap.WsFindStemsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsFindStemsResultsWrapper {
  WsFindStemsResults WsFindStemsResults = null;

  @ApiModelProperty(name = "WsFindStemsResults", value = "Identifies the response of a find stems request")
  public WsFindStemsResults getWsFindStemsResults() {
    return WsFindStemsResults;
  }

  
  public void setWsFindStemsResults(WsFindStemsResults wsFindStemsResults) {
    WsFindStemsResults = wsFindStemsResults;
  }
  

}

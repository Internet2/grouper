package edu.internet2.middleware.grouper.ws.rest.stem;

import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteResults;
import io.swagger.annotations.ApiModelProperty;

public class WsStemDeleteResultsWrapper {
  WsStemDeleteResults WsStemDeleteResults = null;

  @ApiModelProperty(name = "WsStemDeleteResults", value = "Identifies the response of a stem delete request")
  public WsStemDeleteResults getWsStemDeleteResults() {
    return WsStemDeleteResults;
  }

  
  public void setWsStemDeleteResults(WsStemDeleteResults wsStemDeleteResults) {
    WsStemDeleteResults = wsStemDeleteResults;
  }
  

}

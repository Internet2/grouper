package edu.internet2.middleware.grouper.ws.rest.stem;


import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveResults;
import io.swagger.annotations.ApiModelProperty;

public class WsStemSaveResultsWrapper {
  WsStemSaveResults WsStemSaveResults = null;

  @ApiModelProperty(name = "WsStemSaveResults", value = "Identifies the response of a stem save request")
  public WsStemSaveResults getWsStemSaveResults() {
    return WsStemSaveResults;
  }

  
  public void setWsStemSaveResults(WsStemSaveResults wsStemSaveResults) {
    WsStemSaveResults = wsStemSaveResults;
  }
  

}

package edu.internet2.middleware.grouper.ws.rest.stem;


import edu.internet2.middleware.grouper.ws.coresoap.WsStemSaveLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsStemSaveLiteResultsWrapper {
  WsStemSaveLiteResult WsStemSaveLiteResult = null;

  @ApiModelProperty(name = "WsStemSaveLiteResult", value = "Identifies the response of a stem Save Lite request")
  public WsStemSaveLiteResult getWsStemSaveLiteResults() {
    return WsStemSaveLiteResult;
  }

  
  public void setWsStemSaveLiteResults(WsStemSaveLiteResult wsStemSaveLiteResult) {
    WsStemSaveLiteResult = wsStemSaveLiteResult;
  }
  

}

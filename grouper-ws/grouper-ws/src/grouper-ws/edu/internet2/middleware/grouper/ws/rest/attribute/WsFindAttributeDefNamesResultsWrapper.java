package edu.internet2.middleware.grouper.ws.rest.attribute;

import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefNamesResults;
import io.swagger.annotations.ApiModelProperty;

public class WsFindAttributeDefNamesResultsWrapper {
  WsFindAttributeDefNamesResults WsFindAttributeDefNamesResults = null;

  @ApiModelProperty(name = "WsFindAttributeDefNamesResults", value = "Identifies the response of an attribute def Name Delete  request")
  public WsFindAttributeDefNamesResults getWsFindAttributeDefNamesResults() {
    return WsFindAttributeDefNamesResults;
  }

  
  public void setWsFindAttributeDefNamesResults(WsFindAttributeDefNamesResults wsFindAttributeDefNamesResults) {
    WsFindAttributeDefNamesResults = wsFindAttributeDefNamesResults;
  }
  

}

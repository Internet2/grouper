package edu.internet2.middleware.grouper.ws.rest.attribute;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindAttributeDefsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsFindAttributeDefsResultsWrapper {
  WsFindAttributeDefsResults WsFindAttributeDefsResults = null;

  @ApiModelProperty(name = "WsFindAttributeDefsResults", value = "Identifies the response of an attribute def Name Delete  request")
  public WsFindAttributeDefsResults getWsFindAttributeDefsResults() {
    return WsFindAttributeDefsResults;
  }

  
  public void setWsFindAttributeDefsResults(WsFindAttributeDefsResults wsFindAttributeDefsResults) {
    WsFindAttributeDefsResults = wsFindAttributeDefsResults;
  }
  

}

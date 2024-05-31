package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberResults;
import io.swagger.annotations.ApiModelProperty;

public class WsHasMemberResultsWrapper {
  WsHasMemberResults WsHasMemberResults = null;

  @ApiModelProperty(name = "WsHasMemberResults", value = "Identifies the response of a has member request")
  public WsHasMemberResults getWsHasMemberResults() {
    return WsHasMemberResults;
  }

  
  public void setWsHasMemberResults(WsHasMemberResults wsHasMemberResults) {
    WsHasMemberResults = wsHasMemberResults;
  }
  

}

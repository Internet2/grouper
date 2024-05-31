package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.coresoap.WsHasMemberLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsHasMemberLiteResultsWrapper {
  WsHasMemberLiteResult WsHasMemberLiteResult = null;

  @ApiModelProperty(name = "WsHasMemberLiteResult", value = "Identifies the response of a has Member Lite request")
  public WsHasMemberLiteResult getWsHasMemberLiteResult() {
    return WsHasMemberLiteResult;
  }

  
  public void setWsHasMemberLiteResults(WsHasMemberLiteResult wsHasMemberLiteResult) {
    WsHasMemberLiteResult = wsHasMemberLiteResult;
  }
  

}

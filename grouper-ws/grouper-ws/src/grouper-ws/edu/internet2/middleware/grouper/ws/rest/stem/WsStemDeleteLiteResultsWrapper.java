package edu.internet2.middleware.grouper.ws.rest.stem;


import edu.internet2.middleware.grouper.ws.coresoap.WsStemDeleteLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsStemDeleteLiteResultsWrapper {
  WsStemDeleteLiteResult WsStemDeleteLiteResult = null;

  @ApiModelProperty(name = "WsStemDeleteLiteResult", value = "Identifies the response of a stem DeleteLite request")
  public WsStemDeleteLiteResult getWsStemDeleteLiteResults() {
    return WsStemDeleteLiteResult;
  }

  
  public void setWsStemDeleteLiteResults(WsStemDeleteLiteResult wsStemDeleteLiteResult) {
    WsStemDeleteLiteResult = wsStemDeleteLiteResult;
  }
  

}

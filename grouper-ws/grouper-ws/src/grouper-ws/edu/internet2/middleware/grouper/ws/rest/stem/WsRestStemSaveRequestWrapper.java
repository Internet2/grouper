package edu.internet2.middleware.grouper.ws.rest.stem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to save stems")
public class WsRestStemSaveRequestWrapper {

  private WsRestStemSaveRequest wsRestStemSaveRequest;
  
  @ApiModelProperty(name = "WsRestStemSaveRequest", value = "Identifies the request as a save stems request")
  public WsRestStemSaveRequest getWsRestStemSaveRequest() {
    return wsRestStemSaveRequest;
  }

  
  public void setWsRestStemSaveRequest(
      WsRestStemSaveRequest wsRestStemSaveRequest) {
    this.wsRestStemSaveRequest = wsRestStemSaveRequest;
  }
  
  
  
}
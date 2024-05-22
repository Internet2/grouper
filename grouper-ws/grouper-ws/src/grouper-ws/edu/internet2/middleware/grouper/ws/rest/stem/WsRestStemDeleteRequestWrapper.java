package edu.internet2.middleware.grouper.ws.rest.stem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to delete stems")
public class WsRestStemDeleteRequestWrapper {

  private WsRestStemDeleteRequest wsRestStemDeleteRequest;
  
  @ApiModelProperty(name = "WsRestStemDeleteRequest", value = "Identifies the request as a delete stems request")
  public WsRestStemDeleteRequest getWsRestStemDeleteRequest() {
    return wsRestStemDeleteRequest;
  }

  
  public void setWsRestStemDeleteRequest(
      WsRestStemDeleteRequest wsRestStemDeleteRequest) {
    this.wsRestStemDeleteRequest = wsRestStemDeleteRequest;
  }
  
  
  
}

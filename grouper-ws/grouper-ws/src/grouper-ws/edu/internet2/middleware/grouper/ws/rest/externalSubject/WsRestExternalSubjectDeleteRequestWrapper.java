package edu.internet2.middleware.grouper.ws.rest.externalSubject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to delete an external subject")
public class WsRestExternalSubjectDeleteRequestWrapper {

  private WsRestExternalSubjectDeleteRequest wsRestExternalSubjectDeleteRequest;
  
  @ApiModelProperty(name = "WsRestExternalSubjectDeleteRequest", value = "Identifies the request as a delete external subject request")
  public WsRestExternalSubjectDeleteRequest getWsRestExternalSubjectDeleteRequest() {
    return wsRestExternalSubjectDeleteRequest;
  }

  
  public void setWsExternalSubjectDeleteRequest(
      WsRestExternalSubjectDeleteRequest wsRestExternalSubjectDeleteRequest) {
    this.wsRestExternalSubjectDeleteRequest = wsRestExternalSubjectDeleteRequest;
  }
  
  
  
}

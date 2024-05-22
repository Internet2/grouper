package edu.internet2.middleware.grouper.ws.rest.externalSubject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to save an external subject")
public class WsRestExternalSubjectSaveRequestWrapper {

  private WsRestExternalSubjectSaveRequest wsRestExternalSubjectSaveRequest;
  
  @ApiModelProperty(name = "WsRestExternalSubjectSaveRequest", value = "Identifies the request as a save external subject request")
  public WsRestExternalSubjectSaveRequest getWsRestExternalSubjectSaveRequest() {
    return wsRestExternalSubjectSaveRequest;
  }

  
  public void setWsRestExternalSubjectSaveRequest(
      WsRestExternalSubjectSaveRequest wsRestExternalSubjectSaveRequest) {
    this.wsRestExternalSubjectSaveRequest = wsRestExternalSubjectSaveRequest;
  }
  
  
  
}

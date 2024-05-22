package edu.internet2.middleware.grouper.ws.rest.gshTemplate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Request body to execute GSH template")
public class WsRestGshTemplateExecRequestWrapper {

  private WsRestGshTemplateExecRequest wsRestGshTemplateExecRequest;
  
  @ApiModelProperty(name = "WsRestGshTemplateExecRequest", value = "Identifies the request as an execute GSH template request")
  public WsRestGshTemplateExecRequest getWsRestGshTemplateExecRequest() {
    return wsRestGshTemplateExecRequest;
  }

  
  public void setWsRestGshTemplateExecRequest(
      WsRestGshTemplateExecRequest wsRestGshTemplateExecRequest) {
    this.wsRestGshTemplateExecRequest = wsRestGshTemplateExecRequest;
  }
  
  
  
}

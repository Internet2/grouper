package edu.internet2.middleware.grouper.ws.rest.gshTemplate;

import edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateExecResult;
import io.swagger.annotations.ApiModelProperty;

public class WsExecuteGshTemplateResultsWrapper {
  WsGshTemplateExecResult WsGshTemplateExecResult = null;

  @ApiModelProperty(name = "WsExecuteGshTemplateResult", value = "Identifies the response of an execute GSH template request")
  public WsGshTemplateExecResult getWsExecuteGshTemplateResults() {
    return WsGshTemplateExecResult;
  }

  
  public void setWsExecuteGshTemplateResults(WsGshTemplateExecResult WsGshTemplateExecResult) {
    this.WsGshTemplateExecResult = WsGshTemplateExecResult;
  }
  

}

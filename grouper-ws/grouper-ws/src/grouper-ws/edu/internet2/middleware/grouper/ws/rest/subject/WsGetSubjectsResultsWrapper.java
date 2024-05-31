package edu.internet2.middleware.grouper.ws.rest.subject;


import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsGetSubjectsResultsWrapper {
  WsGetSubjectsResults WsGetSubjectsResults = null;

  @ApiModelProperty(name = "WsGetSubjectsResults", value = "Identifies the response of a get subject request")
  public WsGetSubjectsResults getWsGetSubjectsResults() {
    return WsGetSubjectsResults;
  }

  
  public void setWsGetSubjectsResults(WsGetSubjectsResults wsGetSubjectsResults) {
    WsGetSubjectsResults = wsGetSubjectsResults;
  }
  

}

package edu.internet2.middleware.grouper.ws.rest.externalSubject;

import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectDeleteResults;
import io.swagger.annotations.ApiModelProperty;

public class WsExternalSubjectDeleteResultsWrapper {
  WsExternalSubjectDeleteResults WsExternalSubjectDeleteResults = null;

  @ApiModelProperty(name = "WsExternalSubjectDeleteResults", value = "Identifies the response of an external subject delete request")
  public WsExternalSubjectDeleteResults getWsExternalSubjectDeleteResults() {
    return WsExternalSubjectDeleteResults;
  }

  
  public void setWsExternalSubjectDeleteResults(WsExternalSubjectDeleteResults wsExternalSubjectDeleteResults) {
    WsExternalSubjectDeleteResults = wsExternalSubjectDeleteResults;
  }
  

}

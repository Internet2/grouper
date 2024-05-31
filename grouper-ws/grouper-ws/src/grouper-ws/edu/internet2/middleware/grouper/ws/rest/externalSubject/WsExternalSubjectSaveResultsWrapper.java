package edu.internet2.middleware.grouper.ws.rest.externalSubject;
import io.swagger.annotations.ApiModelProperty;
import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectSaveResults;

public class WsExternalSubjectSaveResultsWrapper {
  WsExternalSubjectSaveResults WsExternalSubjectSaveResults = null;

  @ApiModelProperty(name = "WsExternalSubjectSaveResults", value = "Identifies the response of an external subject save request")
  public WsExternalSubjectSaveResults getWsExternalSubjectSaveResults() {
    return WsExternalSubjectSaveResults;
  }

  
  public void setWsExternalSubjectSaveResults(WsExternalSubjectSaveResults wsExternalSubjectSaveResults) {
    WsExternalSubjectSaveResults = wsExternalSubjectSaveResults;
  }
  

}

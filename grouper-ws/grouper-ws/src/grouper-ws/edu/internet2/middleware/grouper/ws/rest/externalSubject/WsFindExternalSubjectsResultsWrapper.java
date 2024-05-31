package edu.internet2.middleware.grouper.ws.rest.externalSubject;
import edu.internet2.middleware.grouper.ws.coresoap.WsFindExternalSubjectsResults;
import io.swagger.annotations.ApiModelProperty;

public class WsFindExternalSubjectsResultsWrapper {
  WsFindExternalSubjectsResults WsFindExternalSubjectsResults = null;

  @ApiModelProperty(name = "WsFindExternalSubjectsResults", value = "Identifies the response of a find external subjects request")
  public WsFindExternalSubjectsResults getWsFindExternalSubjectsResults() {
    return WsFindExternalSubjectsResults;
  }

  
  public void setWsFindExternalSubjectsResults(WsFindExternalSubjectsResults wsFindExternalSubjectsResults) {
    WsFindExternalSubjectsResults = wsFindExternalSubjectsResults;
  }
  

}

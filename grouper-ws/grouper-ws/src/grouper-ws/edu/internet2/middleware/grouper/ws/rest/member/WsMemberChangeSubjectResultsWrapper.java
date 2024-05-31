package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectResults;
import io.swagger.annotations.ApiModelProperty;

public class WsMemberChangeSubjectResultsWrapper {
  WsMemberChangeSubjectResults WsMemberChangeSubjectResults = null;

  @ApiModelProperty(name = "WsMemberChangeSubjectResults", value = "Identifies the response of a member change subject request")
  public WsMemberChangeSubjectResults getWsMemberChangeSubjectResults() {
    return WsMemberChangeSubjectResults;
  }

  
  public void setWsMemberChangeSubjectResults(WsMemberChangeSubjectResults wsMemberChangeSubjectResults) {
    WsMemberChangeSubjectResults = wsMemberChangeSubjectResults;
  }
  

}

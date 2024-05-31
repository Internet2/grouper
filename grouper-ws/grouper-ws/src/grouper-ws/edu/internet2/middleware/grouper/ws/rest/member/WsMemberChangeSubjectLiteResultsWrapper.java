package edu.internet2.middleware.grouper.ws.rest.member;

import edu.internet2.middleware.grouper.ws.soap.WsMemberChangeSubjectLiteResult;
import io.swagger.annotations.ApiModelProperty;

public class WsMemberChangeSubjectLiteResultsWrapper {
  WsMemberChangeSubjectLiteResult WsMemberChangeSubjectLiteResult = null;

  @ApiModelProperty(name = "WsMemberChangeSubjectLiteResult", value = "Identifies the response of a member change subject lite request")
  public WsMemberChangeSubjectLiteResult getWsMemberChangeSubjectLiteResult() {
    return WsMemberChangeSubjectLiteResult;
  }

  
  public void setWsMemberChangeSubjectLiteResult(WsMemberChangeSubjectLiteResult wsMemberChangeSubjectLiteResult) {
    WsMemberChangeSubjectLiteResult = wsMemberChangeSubjectLiteResult;
  }
  

}

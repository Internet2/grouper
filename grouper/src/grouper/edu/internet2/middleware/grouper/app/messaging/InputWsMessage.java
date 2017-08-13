package edu.internet2.middleware.grouper.app.messaging;

public class InputWsMessage {
  
  private String requestJson;
  
  private InputMessageGrouperHeader inputMessageHeader;

  public InputMessageGrouperHeader getInputMessageHeader() {
    return inputMessageHeader;
  }

  public void setInputMessageHeader(InputMessageGrouperHeader inputMessageHeader) {
    this.inputMessageHeader = inputMessageHeader;
  }

  public String getRequestJson() {
    return requestJson;
  }


  public void setRequestJson(String requestJson) {
    this.requestJson = requestJson;
  }
  

}

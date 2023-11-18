package edu.internet2.middleware.grouperMessagingActiveMQ;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;

public class GrouperMessageActiveMQ implements GrouperMessage {
  
  String messageBody;
  
  String id;
  
  public GrouperMessageActiveMQ(String messageBody, String id) {
    this.messageBody = messageBody;
    this.id = id;
  }

  @Override
  public String getFromMemberId() {
    return null;
  }

  @Override
  public void setFromMemberId(String fromMemberId1) {
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public void setId(String id1) {
    this.id = id1;
  }

  @Override
  public String getMessageBody() {
    return messageBody;
  }

  @Override
  public void setMessageBody(String messageBody) {
    this.messageBody = messageBody;
  }

}

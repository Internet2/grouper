package edu.internet2.middleware.grouperMessagingRabbitmq;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;

public class GrouperMesasgeRabbitmq implements GrouperMessage {
  
  String messageBody;
  
  public GrouperMesasgeRabbitmq(String messageBody) {
    this.messageBody = messageBody;
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
    return null;
  }

  @Override
  public void setId(String id1) {
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

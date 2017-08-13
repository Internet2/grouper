package edu.internet2.middleware.grouper.app.messaging;

public class InputMessageGrouperHeader {
  
  private String messageVersion;
  private String timestampInput;
  private String type;
  private String endpoint;
  private String messageInputUuid;
  private String replyToQueueOrTopicName;
  private String replyToQueueOrTopic;
  private String httpMethod;
  private String httpPath;
  
  public String getMessageVersion() {
    return messageVersion;
  }
  
  public void setMessageVersion(String messageVersion) {
    this.messageVersion = messageVersion;
  }
  
  public String getTimestampInput() {
    return timestampInput;
  }
  
  public void setTimestampInput(String timestampInput) {
    this.timestampInput = timestampInput;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getEndpoint() {
    return endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
  
  public String getMessageInputUuid() {
    return messageInputUuid;
  }
  
  public void setMessageInputUuid(String messageInputUuid) {
    this.messageInputUuid = messageInputUuid;
  }
  
  public String getReplyToQueueOrTopicName() {
    return replyToQueueOrTopicName;
  }
  
  public void setReplyToQueueOrTopicName(String replyToQueueOrTopicName) {
    this.replyToQueueOrTopicName = replyToQueueOrTopicName;
  }
  
  public String getReplyToQueueOrTopic() {
    return replyToQueueOrTopic;
  }
  
  public void setReplyToQueueOrTopic(String replyToQueueOrTopic) {
    this.replyToQueueOrTopic = replyToQueueOrTopic;
  }
  
  public String getHttpMethod() {
    return httpMethod;
  }
  
  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }
  
  public String getHttpPath() {
    return httpPath;
  }
  
  public void setHttpPath(String httpPath) {
    this.httpPath = httpPath;
  }

}

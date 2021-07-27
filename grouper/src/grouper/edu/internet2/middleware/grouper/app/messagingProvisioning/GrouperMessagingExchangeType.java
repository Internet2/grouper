package edu.internet2.middleware.grouper.app.messagingProvisioning;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public enum GrouperMessagingExchangeType {

  DIRECT, TOPIC, HEADERS, FANOUT;
  
  public static GrouperMessagingExchangeType valueOfIgnoreCase(String input, boolean exceptionIfNotFound) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GrouperMessagingExchangeType.class, input, exceptionIfNotFound);
  }
}

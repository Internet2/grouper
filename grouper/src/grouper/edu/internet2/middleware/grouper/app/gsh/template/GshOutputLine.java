package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GshOutputLine {

  /**
   * success, info, error
   */
  private String messageType;
  
  private String text;
  
  private static Set<String> validMessageTypes = GrouperUtil.toSet("success", "info", "error");
  
  public GshOutputLine(String text) {
    this.text = text;
    this.messageType = "success";
  }
  
  public GshOutputLine(String messageType, String text) {
    if (StringUtils.isBlank(messageType)) {
      messageType = "success";
    }
    
    messageType = messageType.toLowerCase();
    
    GrouperUtil.assertion(validMessageTypes.contains(messageType), "message type must be success, info, error.");
    this.messageType = messageType;
    this.text = text;
  }
  
  public String getMessageType() {
    return messageType;
  }

  public String getText() {
    return text;
  }
  
  
}

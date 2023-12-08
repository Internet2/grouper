package edu.internet2.middleware.grouper.app.tableSync;

import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class ProvisioningSampleListener extends EsbListenerBase {

  public static int messageCount = 0;
  
  public ProvisioningSampleListener() {
  }

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    
    GrouperUtil.jsonJacksonNode(eventJsonString);
    
    messageCount++;
    return true;
  }

  @Override
  public void disconnect() {
  }

}

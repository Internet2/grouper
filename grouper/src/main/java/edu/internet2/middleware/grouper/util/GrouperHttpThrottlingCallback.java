package edu.internet2.middleware.grouper.util;


public interface GrouperHttpThrottlingCallback {
  
  boolean setupThrottlingCallback(GrouperHttpClient httpClient);

}

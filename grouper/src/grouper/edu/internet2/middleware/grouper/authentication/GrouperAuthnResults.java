package edu.internet2.middleware.grouper.authentication;

import java.util.Map;

public class GrouperAuthnResults {

  public GrouperAuthnResults() {
    
  }

  private Map<String, GrouperAuthnResult> configIdToGrouperAuthnResult = new java.util.HashMap<String, GrouperAuthnResult>();
  
  public Map<String, GrouperAuthnResult> getConfigIdToGrouperAuthnResult() {
    return configIdToGrouperAuthnResult;
  }
  
}

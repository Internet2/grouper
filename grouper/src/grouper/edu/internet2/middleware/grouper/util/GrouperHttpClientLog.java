package edu.internet2.middleware.grouper.util;

import java.util.HashSet;
import java.util.Set;

public class GrouperHttpClientLog {

  private StringBuilder log = new StringBuilder();
  
  
  public StringBuilder getLog() {
    return log;
  }

  
  public void setLog(StringBuilder log) {
    this.log = log;
  }

  public GrouperHttpClientLog() {
  }
  
}

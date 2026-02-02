package edu.internet2.middleware.grouperClient.jdbc;

import java.util.HashSet;
import java.util.Set;

public class SqlProvisioningCommandsLog {

  private StringBuilder log = new StringBuilder();
  
  
  public StringBuilder getLog() {
    return log;
  }

  
  public void setLog(StringBuilder log) {
    this.log = log;
  }

  public SqlProvisioningCommandsLog() {
  }
  
}

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

  /**
   * 
   */
  private Set<String> doNotLogParameters = new HashSet<String>();

  /**
   * 
   */
  private Set<String> doNotLogHeaders = new HashSet<String>();

  public GrouperHttpClientLog() {
  }

  public GrouperHttpClientLog assignDoNotLogParameters(String paramsCommaSeparated) {
    this.doNotLogParameters = GrouperUtil.nonNull(GrouperUtil.toSet(GrouperUtil.splitTrim(paramsCommaSeparated, ",")));
    return this;
  }
  
  public GrouperHttpClientLog assignDoNotLogHeaders(String headersCommaSeparated) {
    this.doNotLogHeaders = GrouperUtil.nonNull(GrouperUtil.toSet(GrouperUtil.splitTrim(headersCommaSeparated, ",")));
    return this;
  }
  public GrouperHttpClientLog assignDoNotLogParameters(Set<String> params) {
    this.doNotLogParameters = GrouperUtil.nonNull(params);
    return this;
  }
  
  public GrouperHttpClientLog assignDoNotLogHeaders(Set<String> headers) {
    this.doNotLogHeaders = GrouperUtil.nonNull(headers);
    return this;
  }


  
  public Set<String> getDoNotLogParameters() {
    return doNotLogParameters;
  }

  
  public Set<String> getDoNotLogHeaders() {
    return doNotLogHeaders;
  }

  
}

package edu.internet2.middleware.grouper.ddl;

import java.util.Map;
import java.util.TreeMap;

import org.apache.ddlutils.model.Index;

public class GrouperDdlCompareIndex {

  private boolean missing;
  
  private boolean extra;
  
  private boolean correct;
  
  private String name;
  
  private Map<String, GrouperDdlCompareIndexColumn> grouperDdlCompareColumns = new TreeMap<String, GrouperDdlCompareIndexColumn>();
  
  private Index databaseIndex;
  
  private Index javaIndex;
  
  public Index getDatabaseIndex() {
    return databaseIndex;
  }
  
  public void setDatabaseIndex(Index databaseIndex) {
    this.databaseIndex = databaseIndex;
  }
  
  public Index getJavaIndex() {
    return javaIndex;
  }
  
  public void setJavaIndex(Index javaIndex) {
    this.javaIndex = javaIndex;
  }

  public boolean isMissing() {
    return missing;
  }

  
  public void setMissing(boolean missing) {
    this.missing = missing;
  }

  
  public boolean isExtra() {
    return extra;
  }

  
  public void setExtra(boolean extra) {
    this.extra = extra;
  }

  
  public boolean isCorrect() {
    return correct;
  }

  
  public void setCorrect(boolean correct) {
    this.correct = correct;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public Map<String, GrouperDdlCompareIndexColumn> getGrouperDdlCompareColumns() {
    return grouperDdlCompareColumns;
  }

  
}

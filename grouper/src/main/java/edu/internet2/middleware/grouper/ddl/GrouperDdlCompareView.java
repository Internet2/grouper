package edu.internet2.middleware.grouper.ddl;

import java.util.ArrayList;
import java.util.List;

public class GrouperDdlCompareView {

  private boolean missing;
  
  private boolean extra;
  
  private boolean correct;
  
  private String name;
  
  private List<GrouperDdlCompareColumn> grouperDdlCompareColumns = new ArrayList<GrouperDdlCompareColumn>();

  
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

  
  public List<GrouperDdlCompareColumn> getGrouperDdlCompareColumns() {
    return grouperDdlCompareColumns;
  }

  
  public void setGrouperDdlCompareColumns(
      List<GrouperDdlCompareColumn> grouperDdlCompareColumns) {
    this.grouperDdlCompareColumns = grouperDdlCompareColumns;
  }
  
  
}

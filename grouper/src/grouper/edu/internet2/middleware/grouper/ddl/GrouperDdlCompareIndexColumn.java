package edu.internet2.middleware.grouper.ddl;

import org.apache.ddlutils.model.IndexColumn;

public class GrouperDdlCompareIndexColumn {

  private IndexColumn databaseColumn;
  
  private IndexColumn javaColumn;

  
  public IndexColumn getDatabaseColumn() {
    return databaseColumn;
  }


  
  public void setDatabaseColumn(IndexColumn databaseColumn) {
    this.databaseColumn = databaseColumn;
  }


  
  public IndexColumn getJavaColumn() {
    return javaColumn;
  }


  
  public void setJavaColumn(IndexColumn javaColumn) {
    this.javaColumn = javaColumn;
  }


  private String name;
  
  private boolean correct;
  
  private boolean missing;
  
  private boolean extra;

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public boolean isCorrect() {
    return correct;
  }

  
  public void setCorrect(boolean correct) {
    this.correct = correct;
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

  
  
}

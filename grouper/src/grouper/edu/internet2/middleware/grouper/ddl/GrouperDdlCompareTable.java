package edu.internet2.middleware.grouper.ddl;

import java.util.Map;
import java.util.TreeMap;

import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;

public class GrouperDdlCompareTable {

  private Table databaseTable;
  
  private Table javaTable;
  
  
  public Table getDatabaseTable() {
    return databaseTable;
  }

  
  public void setDatabaseTable(Table databaseTable) {
    this.databaseTable = databaseTable;
  }

  
  public Table getJavaTable() {
    return javaTable;
  }

  
  public void setJavaTable(Table javaTable) {
    this.javaTable = javaTable;
  }

  private Map<String, ForeignKey> databaseForeignKeys = new TreeMap<String, ForeignKey>();

  private Map<String, ForeignKey> javaForeignKeys = new TreeMap<String, ForeignKey>();
  
  public Map<String, ForeignKey> getDatabaseForeignKeys() {
    return databaseForeignKeys;
  }
  
  public Map<String, ForeignKey> getJavaForeignKeys() {
    return javaForeignKeys;
  }

  private boolean missing;
  
  private boolean extra;
  
  private boolean correct;
  
  private String name;
  
  private Map<String, GrouperDdlCompareColumn> grouperDdlCompareColumns = new TreeMap<String, GrouperDdlCompareColumn>();

  private Map<String, GrouperDdlCompareIndex> grouperDdlCompareIndexes = new TreeMap<String, GrouperDdlCompareIndex>();

  public Map<String, GrouperDdlCompareIndex> getGrouperDdlCompareIndexes() {
    return grouperDdlCompareIndexes;
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
  
  public Map<String, GrouperDdlCompareColumn> getGrouperDdlCompareColumns() {
    return grouperDdlCompareColumns;
  }
  
}

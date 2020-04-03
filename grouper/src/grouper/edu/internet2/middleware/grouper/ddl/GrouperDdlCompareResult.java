package edu.internet2.middleware.grouper.ddl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * result of ddl compare
 * @author mchyzer
 *
 */
public class GrouperDdlCompareResult {

  private int errorCount = 0;
  
  private int warningCount = 0;
  
  /**
   * 
   */
  public void errorIncrement() {
    this.errorCount++;
  }

  public void warningIncrement() {
    this.warningCount++;
  }
  
  
  
  
  public int getErrorCount() {
    return errorCount;
  }

    
  public int getWarningCount() {
    return warningCount;
  }



  private StringBuilder result = new StringBuilder();
  
  private Map<String, GrouperDdlCompareTable> grouperDdlCompareTables = new TreeMap<String, GrouperDdlCompareTable>();

  private Map<String, GrouperDdlCompareView> grouperViewsInJava = new TreeMap<String, GrouperDdlCompareView>();
  
  public Map<String, GrouperDdlCompareView> getGrouperViewsInJava() {
    return grouperViewsInJava;
  }
  
  public StringBuilder getResult() {
    return result;
  }

  
  public void setResult(StringBuilder result) {
    this.result = result;
  }

  
  public Map<String, GrouperDdlCompareTable> getGrouperDdlCompareTables() {
    return grouperDdlCompareTables;
  }

  
}

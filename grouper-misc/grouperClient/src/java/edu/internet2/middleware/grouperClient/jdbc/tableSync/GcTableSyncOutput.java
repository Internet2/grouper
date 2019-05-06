/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;


/**
 *
 */
public class GcTableSyncOutput {

  /**
   * 
   */
  public GcTableSyncOutput() {
  }

  /**
   * total count
   */
  private int total;
  
  
  /**
   * @return the total
   */
  public int getTotal() {
    return this.total;
  }

  
  /**
   * @param total1 the total to set
   */
  public void setTotal(int total1) {
    this.total = total1;
  }

  /**
   * total insert
   */
  private int insert;
  
  
  /**
   * @return the insert
   */
  public int getInsert() {
    return this.insert;
  }


  
  /**
   * @param insert1 the insert to set
   */
  public void setInsert(int insert1) {
    this.insert = insert1;
  }

  /**
   * total update
   */
  private int update;
  
  
  /**
   * @return the update
   */
  public int getUpdate() {
    return this.update;
  }


  
  /**
   * @param update1 the update to set
   */
  public void setUpdate(int update1) {
    this.update = update1;
  }

  /**
   * total delete
   */
  private int delete;
  
  
  /**
   * @return the delete
   */
  public int getDelete() {
    return this.delete;
  }


  
  /**
   * @param delete1 the delete to set
   */
  public void setDelete(int delete1) {
    this.delete = delete1;
  }

  /**
   * message
   */
  private String message;


  
  /**
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }


  
  /**
   * @param message1 the message to set
   */
  public void setMessage(String message1) {
    this.message = message1;
  }
  
}

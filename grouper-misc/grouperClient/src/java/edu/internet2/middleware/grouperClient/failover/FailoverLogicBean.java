package edu.internet2.middleware.grouperClient.failover;

/**
 * bean in callback for failover logic
 * @author mchyzer
 *
 */
public class FailoverLogicBean {

  /**
   * failover logic bean
   */
  public FailoverLogicBean() {
    
  }
  
  
  /**
   * 
   * @param runningInNewThread1
   * @param connectionName1
   */
  public FailoverLogicBean(boolean runningInNewThread1, String connectionName1) {
    super();
    this.runningInNewThread = runningInNewThread1;
    this.connectionName = connectionName1;
  }



  /** if it is running in new thread */
  private boolean runningInNewThread;
  
  /** the connection name which is running */
  private String connectionName;

  /**
   * if it is running in new thread
   * @return if it is running in new thread
   */
  public boolean isRunningInNewThread() {
    return this.runningInNewThread;
  }

  /**
   * if it is running in new thread
   * @param runningInNewThread1
   */
  public void setRunningInNewThread(boolean runningInNewThread1) {
    this.runningInNewThread = runningInNewThread1;
  }

  /**
   * the connection name which is running
   * @return the connection name which is running
   */
  public String getConnectionName() {
    return this.connectionName;
  }

  /**
   * the connection name which is running
   * @param connectionName1
   */
  public void setConnectionName(String connectionName1) {
    this.connectionName = connectionName1;
  }
  
  
  
}

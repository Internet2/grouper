package edu.internet2.middleware.grouperClient.failover;

/**
 * bean in callback for failover logic
 * @author mchyzer
 *
 */
public class FailoverLogicBean {

  /** true if this is the last connection in the list of connections to try */
  private boolean lastConnection = false;
  
  /**
   * failover logic bean
   */
  public FailoverLogicBean() {
    
  }
  
  /**
   * true if this is the last connection in the list of connections to try
   * @return true if this is the last connection
   */
  public boolean isLastConnection() {
    return this.lastConnection;
  }

  /**
   * true if this is the last connection in the list of connections to try
   * @param lastConnection1
   */
  public void setLastConnection(boolean lastConnection1) {
    this.lastConnection = lastConnection1;
  }


  /**
   * 
   * @param runningInNewThread1
   * @param connectionName1
   * @param isLastConnection 
   */
  public FailoverLogicBean(boolean runningInNewThread1, String connectionName1, boolean isLastConnection) {
    super();
    this.runningInNewThread = runningInNewThread1;
    this.connectionName = connectionName1;
    this.lastConnection = isLastConnection;
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

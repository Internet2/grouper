/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

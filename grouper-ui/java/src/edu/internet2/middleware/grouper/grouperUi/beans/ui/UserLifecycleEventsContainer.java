package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

public class UserLifecycleEventsContainer {
  private List<UserLifecycleEventContainer> userLifecycleEventContainers;

  /**
   * how many successes
   */
  private int successCount;
  
  /**
   * how many failures
   */
  private int failureCount;
  
  public List<UserLifecycleEventContainer> getUserLifecycleEventContainers() {
    return userLifecycleEventContainers;
  }

  
  public void setUserLifecycleEventContainers(
      List<UserLifecycleEventContainer> userLifecycleEventContainers) {
    this.userLifecycleEventContainers = userLifecycleEventContainers;
  }
  
  /**
   * how many successes
   * @return successes
   */
  public int getSuccessCount() {
    return this.successCount;
  }

  /**
   * how many successes
   * @param successCount1
   */
  public void setSuccessCount(int successCount1) {
    this.successCount = successCount1;
  }

  /**
   * how many failures
   * @return failures
   */
  public int getFailureCount() {
    return this.failureCount;
  }

  /**
   * how many failures
   * @param failuresCount1
   */
  public void setFailureCount(int failuresCount1) {
    this.failureCount = failuresCount1;
  }
}

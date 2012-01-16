package edu.internet2.middleware.grouperClient.failover;


/**
 * Logic to run.  If there is a problem or timeout, try a different connection
 * @author mchyzer
 * @param <V> return type of logic
 *
 */
public interface FailoverLogic<V> {

  /**
   * Logic to run.  If there is a problem or timeout, try a different connection
   * Note, if there are threadlocal things to set, make sure to set them in the logic
   * @param failoverLogicBean if running in new thread, and connection name
   * @return whatever it returns
   */
  public abstract V logic(FailoverLogicBean failoverLogicBean);

}

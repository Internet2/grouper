package edu.internet2.middleware.grouperClient.failover;


/**
 * Database logic to run in multiple connection if problem
 * @author mchyzer
 *
 */
public interface FailoverLogic<K,V> {

  /**
   * Database logic to run in multiple connection if problem
   * @param config is the name to run the code in or some part of the config for this connection
   * @return whatever it returns
   */
  public abstract V logic(K config);

}

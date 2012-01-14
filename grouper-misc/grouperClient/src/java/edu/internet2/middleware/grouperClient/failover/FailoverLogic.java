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
   * @param config is the name to run the code in or some part of the config for this connection
   * @param connectionName is one of the connection names from the config
   * @return whatever it returns
   */
  public abstract V logic(String connectionName);

}

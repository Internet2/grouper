package edu.internet2.middleware.grouperClient.failover;

import java.util.List;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * configuration of a failover connection type
 * @author mchyzer
 *
 */
public class FailoverConfig {

  /**
   * 
   */
  public FailoverConfig() {
    
    {
      int minutesToKeepErrors = GrouperClientUtils.propertiesValueInt("grouperClient.minutesToKeepErrors", 5, false);
      this.setMinutesToKeepErrors(minutesToKeepErrors);
    }

    {
      int secondsForClassesToLoad = GrouperClientUtils.propertiesValueInt("grouperClient.secondsForClassesToLoad", 20, false);
      this.setSecondsForClassesToLoad(secondsForClassesToLoad);
    }

    
  }
  
  /**
   * if your app has a slow startup time, and the initial connections are timing out
   * esp if you arent just using the command line client (e.g. if using it as a jar), then add more time here
   */
  private int secondsForClassesToLoad = -1;
  
  /**
   * if your app has a slow startup time, and the initial connections are timing out
   * esp if you arent just using the command line client (e.g. if using it as a jar), then add more time here
   * @return seconds for classes to load
   */
  public int getSecondsForClassesToLoad() {
    return this.secondsForClassesToLoad;
  }
  
  /**
   * if your app has a slow startup time, and the initial connections are timing out
   * esp if you arent just using the command line client (e.g. if using it as a jar), then add more time here
   * @param secondsForClassesToLoad1
   */
  public void setSecondsForClassesToLoad(int secondsForClassesToLoad1) {
    this.secondsForClassesToLoad = secondsForClassesToLoad1;
  }

  /**
   * string that identifies this failover config from other failover config, e.g. webServiceReadOnlyPool
   */
  private String connectionType;

  /**
   * string that identifies this failover config from other failover config, e.g. webServiceReadOnlyPool
   * @return the connection type
   */
  public String getConnectionType() {
    return this.connectionType;
  }

  /**
   * string that identifies this failover config from other failover config, e.g. webServiceReadOnlyPool
   * @param connectionType1
   */
  public void setConnectionType(String connectionType1) {
    this.connectionType = connectionType1;
  }

  /**
   * names of the connections in the pool, if it is active/standby, then these are ordered from more
   * important to less important
   */
  private List<String> connectionNames;

  /**
   * names of the connections in the pool, note that the "connectionNames" list will be tried first if
   * same number of errors recently.  For example if you want to try the read/write connections first,
   * and this operation is possible to be used for readonly, then it will try the first tier
   * 
   */
  private List<String> connectionNamesSecondTier;
  
  /**
   * names of the connections in the pool, note that the "connectionNames" list will be tried first if
   * same number of errors recently
   * @return connection names second tier
   */
  public List<String> getConnectionNamesSecondTier() {
    return this.connectionNamesSecondTier;
  }

  /**
   * names of the connections in the pool, note that the "connectionNames" list will be tried first if
   * same number of errors recently
   * @param connectionNamesSecondTier1
   */
  public void setConnectionNamesSecondTier(List<String> connectionNamesSecondTier1) {
    this.connectionNamesSecondTier = connectionNamesSecondTier1;
  }

  /**
   * names of the connections in the pool, if it is active/standby, then these are ordered from more
   * important to less important
   * @return the list
   */
  public List<String> getConnectionNames() {
    return this.connectionNames;
  }

  /**
   * names of the connections in the pool, if it is active/standby, then these are ordered from more
   * important to less important
   * @param connectionNames1
   */
  public void setConnectionNames(List<String> connectionNames1) {
    this.connectionNames = connectionNames1;
  }

  /**
   * minutes to remember that there was an error for a connection name of a connection type
   */
  private int minutesToKeepErrors = -1;

  /**
   * minutes to remember that there was an error for a connection name of a connection type
   * @return minutes to keep errors
   */
  public int getMinutesToKeepErrors() {
    return this.minutesToKeepErrors;
  }

  /**
   * minutes to remember that there was an error for a connection name of a connection type
   * @param minutesToKeepErrors1
   */
  public void setMinutesToKeepErrors(int minutesToKeepErrors1) {
    this.minutesToKeepErrors = minutesToKeepErrors1;
  }

  /** actice/active or active/standby */
  private FailoverStrategy failoverStrategy;
  
  
  
  
  /**
   * actice/active or active/standby
   * @return actice/active or active/standby
   */
  public FailoverStrategy getFailoverStrategy() {
    return failoverStrategy;
  }

  /**
   * actice/active or active/standby
   * @param failoverStrategy1
   */
  public void setFailoverStrategy(FailoverStrategy failoverStrategy1) {
    this.failoverStrategy = failoverStrategy1;
  }

  /**
   * if we are active/active, then the same connection will
   * be used for a certain number of seconds.  If this is -1, then 
   * always keep the same server (unless errors)   
   */
  private int affinitySeconds = 28800;

  
  /**
   * if we are active/active, then the same connection will
   * be used for a certain number of seconds.  If this is -1, then 
   * always keep the same server (unless errors)   
   * @return affinity seconds
   */
  public int getAffinitySeconds() {
    return this.affinitySeconds;
  }

  /**
   * if we are active/active, then the same connection will
   * be used for a certain number of seconds.  If this is -1, then 
   * always keep the same server (unless errors)   
   * @param affinitySeconds1
   */
  public void setAffinitySeconds(int affinitySeconds1) {
    this.affinitySeconds = affinitySeconds1;
  }

  /**
   * when a connection is attempted, this is the timeout that it will use before trying
   * another connection
   */
  private int timeoutSeconds = 30;

  /**
   * when a connection is attempted, this is the timeout that it will use before trying
   * another connection
   * @return timeout seconds
   */
  public int getTimeoutSeconds() {
    return this.timeoutSeconds;
  }

  /**
   * when a connection is attempted, this is the timeout that it will use before trying
   * another connection
   * @param timeoutSeconds1
   */
  public void setTimeoutSeconds(int timeoutSeconds1) {
    this.timeoutSeconds = timeoutSeconds1;
  }

  /**
   * after all connections have been attempted, it will wait for this long
   * to see if any finish
   */
  private int extraTimeoutSeconds = 15;

  
  
  /**
   * after all connections have been attempted, it will wait for this long
   * to see if any finish
   * @return extra timeout seconds
   */
  public int getExtraTimeoutSeconds() {
    return this.extraTimeoutSeconds;
  }

  /**
   * after all connections have been attempted, it will wait for this long
   * to see if any finish
   * @param extraTimeoutSeconds1
   */
  public void setExtraTimeoutSeconds(int extraTimeoutSeconds1) {
    this.extraTimeoutSeconds = extraTimeoutSeconds1;
  }



  /**
   * failover strategy to employ with each pool type
   *
   */
  public static enum FailoverStrategy {
    
    /**
     * if should try allow by equal chance (when there is a reason to pick and not affinity
     */
    activeActive,
    
    /**
     * if all other things are queal, try the first connection type
     */
    activeStandby;
  }
  
}

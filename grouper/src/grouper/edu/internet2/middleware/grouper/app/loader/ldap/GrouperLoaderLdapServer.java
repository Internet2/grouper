/*
 * @author mchyzer
 * $Id: GrouperLoaderDb.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import org.apache.commons.lang.StringUtils;



/**
 * db profile from grouper.properties (or possibly grouper.hibernate.properties)
 */
public class GrouperLoaderLdapServer {
  
  /** user to login to ldap, e.g. uid=someapp,ou=people,dc=myschool,dc=edu */
  private String user;
  
  /** pass to login to db */
  private String pass;
  
  /** 
   * note the URL should start with ldap: or ldaps: if it is SSL.  
   * It should contain the server and port (optional if not default), and baseDn, 
   * e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
   */
  private String url;
  
  /** db driver to use to login */
  private String driver;

  /** optional, if you are using tls, set this to TRUE.  Generally you will not be using an SSL URL to use TLS... */
  private boolean tls = false;

  
  
  /**
   * optional, if you are using tls, set this to TRUE.  Generally you will not be using an SSL URL to use TLS...
   * @return if tls
   */
  public boolean isTls() {
    return this.tls;
  }
  
  /**
   * optional, if you are using tls, set this to TRUE.  Generally you will not be using an SSL URL to use TLS...
   * @param tls1
   */
  public void setTls(boolean tls1) {
    this.tls = tls1;
  }

  /** if using sasl, this is authz id */
  private String saslAuthorizationId;

  /** if using sasl, this is the realm */
  private String saslRealm;

  /** batch size for results */
  private int batchSize = -1;
  
  /** count limit for results  */
  private int countLimit = -1;
  
  /** time limit is for of search operation */
  private int timeLimit = -1;

  /** timeout is for connection timeouts */
  private int timeout = -1;

  /** minimum pool size */
  private int minPoolSize = -1;

  /** maximum pool size */
  private int maxPoolSize = -1;

  /** if should validate on check in to pool */
  private boolean validateOnCheckIn = false;
  
  /** if should validate on check out of pool (default true if all other validate methods are false) */
  private boolean validateOnCheckOut = true;

  /** if should validate periodically while in pool */
  private boolean validatePeriodically = false;

  /** if validating periodically, this is the period in millis */
  private int validateTimerPeriod = -1;

  /** period for which prune timer will run, in millis */
  private int pruneTimerPeriod = -1;
  
  /** if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes) */
  private int expirationTime = -1;

  
  
  /**
   * if using sasl, this is authz id
   * @return authz id
   */
  public String getSaslAuthorizationId() {
    return this.saslAuthorizationId;
  }

  /**
   * if using sasl, this is authz id
   * @param saslAuthorizationId1
   */
  public void setSaslAuthorizationId(String saslAuthorizationId1) {
    this.saslAuthorizationId = saslAuthorizationId1;
  }

  /**
   * if using sasl, this is the realm
   * @return sasl realm
   */
  public String getSaslRealm() {
    return this.saslRealm;
  }

  /**
   * if using sasl, this is the realm
   * @param saslRealm1
   */
  public void setSaslRealm(String saslRealm1) {
    this.saslRealm = saslRealm1;
  }

  /**
   * batch size for results
   * @return batch size for results
   */
  public int getBatchSize() {
    return this.batchSize;
  }

  /**
   * batch size for results
   * @param batchSize1
   */
  public void setBatchSize(int batchSize1) {
    this.batchSize = batchSize1;
  }

  /**
   * count limit for results
   * @return count limit for results
   */
  public int getCountLimit() {
    return this.countLimit;
  }

  /**
   * count limit for results
   * @param countLimit1
   */
  public void setCountLimit(int countLimit1) {
    this.countLimit = countLimit1;
  }

  /**
   * time limit is for of search operation
   * @return time limit is for of search operation
   */
  public int getTimeLimit() {
    return this.timeLimit;
  }

  /**
   * time limit is for of search operation
   * @param timeLimit1
   */
  public void setTimeLimit(int timeLimit1) {
    this.timeLimit = timeLimit1;
  }

  /**
   * timeout is for connection timeouts
   * @return timeout is for connection timeouts
   */
  public int getTimeout() {
    return this.timeout;
  }

  /**
   * timeout is for connection timeouts
   * @param timeout1
   */
  public void setTimeout(int timeout1) {
    this.timeout = timeout1;
  }

  /**
   * minimum pool size
   * @return minimum pool size
   */
  public int getMinPoolSize() {
    return this.minPoolSize;
  }

  /**
   * minimum pool size
   * @param minPoolSize1
   */
  public void setMinPoolSize(int minPoolSize1) {
    this.minPoolSize = minPoolSize1;
  }

  /**
   * maximum pool size
   * @return maximum pool size
   */
  public int getMaxPoolSize() {
    return this.maxPoolSize;
  }

  /**
   * maximum pool size
   * @param maxPoolSize1
   */
  public void setMaxPoolSize(int maxPoolSize1) {
    this.maxPoolSize = maxPoolSize1;
  }

  /**
   * if should validate on check in to pool
   * @return if should validate on check in to pool
   */
  public boolean isValidateOnCheckIn() {
    return this.validateOnCheckIn;
  }

  /**
   * if should validate on check in to pool
   * @param validateOnCheckIn1
   */
  public void setValidateOnCheckIn(boolean validateOnCheckIn1) {
    this.validateOnCheckIn = validateOnCheckIn1;
  }

  /**
   * if should validate on check out of pool (default true if all other validate methods are false)
   * @return if should validate on check out of pool (default true if all other validate methods are false)
   */
  public boolean isValidateOnCheckOut() {
    return this.validateOnCheckOut;
  }

  /**
   * if should validate on check out of pool (default true if all other validate methods are false)
   * @param validateOnCheckOut1
   */
  public void setValidateOnCheckOut(boolean validateOnCheckOut1) {
    this.validateOnCheckOut = validateOnCheckOut1;
  }

  /**
   * if should validate periodically while in pool
   * @return if should validate periodically while in pool
   */
  public boolean isValidatePeriodically() {
    return this.validatePeriodically;
  }

  /**
   * if should validate periodically while in pool
   * @param validatePeriodically1
   */
  public void setValidatePeriodically(boolean validatePeriodically1) {
    this.validatePeriodically = validatePeriodically1;
  }

  /**
   * if validating periodically, this is the period in millis
   * @return if validating periodically, this is the period in millis
   */
  public int getValidateTimerPeriod() {
    return this.validateTimerPeriod;
  }

  /**
   * if validating periodically, this is the period in millis
   * @param validateTimerPeriod1
   */
  public void setValidateTimerPeriod(int validateTimerPeriod1) {
    this.validateTimerPeriod = validateTimerPeriod1;
  }

  /**
   * period for which prune timer will run, in millis
   * @return period for which prune timer will run, in millis
   */
  public int getPruneTimerPeriod() {
    return this.pruneTimerPeriod;
  }

  /**
   * period for which prune timer will run, in millis
   * @param pruneTimerPeriod1
   */
  public void setPruneTimerPeriod(int pruneTimerPeriod1) {
    this.pruneTimerPeriod = pruneTimerPeriod1;
  }

  /**
   * if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes)
   * @return if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes)
   */
  public int getExpirationTime() {
    return this.expirationTime;
  }

  /**
   * if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes)
   * @param expirationTime1
   */
  public void setExpirationTime(int expirationTime1) {
    this.expirationTime = expirationTime1;
  }

  
  /**
   * note, this is generated by eclipse, with the exception of password
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return "GrouperLoaderLdapServer [batchSize=" + batchSize + ", countLimit="
        + countLimit + ", driver=" + driver + ", expirationTime=" + expirationTime
        + ", maxPoolSize=" + maxPoolSize + ", minPoolSize=" + minPoolSize + ", pass="
        + (StringUtils.isBlank(pass) ? "" : "XXXXX") + ", pruneTimerPeriod=" + pruneTimerPeriod 
        + ", saslAuthorizationId=" + saslAuthorizationId + ", saslRealm=" + saslRealm
        + ", timeLimit=" + timeLimit + ", timeout=" + timeout + ", tls=" + tls + ", url="
        + url + ", user=" + user + ", validateOnCheckIn=" + validateOnCheckIn
        + ", validateOnCheckOut=" + validateOnCheckOut + ", validatePeriodically="
        + validatePeriodically + ", validateTimerPeriod=" + validateTimerPeriod + "]";
  }

  /**
   * user to login to ldap e.g. uid=someapp,ou=people,dc=myschool,dc=edu
   * @return the user
   */
  public String getUser() {
    return this.user;
  }

  
  /**
   * user to login to ldap e.g. uid=someapp,ou=people,dc=myschool,dc=edu
   * @param user1 the user to set
   */
  public void setUser(String user1) {
    this.user = user1;
  }

  
  /**
   * pass to login to db
   * @return the pass
   */
  public String getPass() {
    return this.pass;
  }

  
  /**
   * pass to login to db
   * @param pass1 the pass to set
   */
  public void setPass(String pass1) {
    this.pass = pass1;
  }

  
  /**
   * note the URL should start with ldap: or ldaps: if it is SSL.  
   * It should contain the server and port (optional if not default), and baseDn, 
   * e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
   * @return the url
   */
  public String getUrl() {
    return this.url;
  }

  
  /**
   * note the URL should start with ldap: or ldaps: if it is SSL.  
   * It should contain the server and port (optional if not default), and baseDn, 
   * e.g. ldaps://ldapserver.school.edu:636/dc=school,dc=edu
   * @param url1 the url to set
   */
  public void setUrl(String url1) {
    this.url = url1;
  }

  
  /**
   * db driver to use to login
   * @return the driver
   */
  public String getDriver() {
    return this.driver;
  }

  
  /**
   * db driver to use to login
   * @param driver1 the driver to set
   */
  public void setDriver(String driver1) {
    this.driver = driver1;
  }

  /**
   * empty constructor
   */
  public GrouperLoaderLdapServer() {
    //empty  
  }
  
}

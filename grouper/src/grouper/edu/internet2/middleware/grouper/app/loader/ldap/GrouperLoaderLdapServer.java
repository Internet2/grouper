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
/*
 * @author mchyzer
 * $Id: GrouperLoaderDb.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.pool.LdapValidator;
import org.apache.commons.lang.StringUtils;



/**
 * db profile from grouper.properties (or possibly grouper.hibernate.properties)
 */
public class GrouperLoaderLdapServer {

  /**
   * get the base dn from the URL, e.g. ldaps://server/baseDn would return baseDn
   * @return base dn or null if none
   */
  public String getBaseDn() {
    String baseDn = null;
    //must have three slashes, e.g. ldaps://something/baseDn
    if (this.url != null && StringUtils.countMatches(this.url, "/") == 3 && !this.url.endsWith("/")) {
      //get the string after the last slash
      int indexOfLastSlash = this.url.lastIndexOf('/');
      baseDn = StringUtils.trimToNull(this.url.substring(indexOfLastSlash+1, this.url.length()));
    }
    return baseDn;
  }
  
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

  /** if validating, the validating function */
  private LdapValidator<Ldap> validator = null;

  /** period for which prune timer will run, in millis */
  private int pruneTimerPeriod = -1;
  
  /** if connections expire after a certain amount of time, this is it, in millis, defaults to 300000 (5 minutes) */
  private int expirationTime = -1;

  /** if the ldap server has a max page size, then this will get the results in pages */
  private int pagedResultsSize = -1;

  /**
   * ldap.personLdap.referral: set to 'follow' if using AD and using paged results size
   */
  private String referral = null;

  /**
   * load this vt-ldap config file before the configs here.  load from classpath
   */
  private String configFileFromClasspath = null;
  
  /**
   * load this vt-ldap config file before the configs here.  load from classpath
   * @return the configFileFromClasspath
   */
  public String getConfigFileFromClasspath() {
    return this.configFileFromClasspath;
  }
  
  /**
   * load this vt-ldap config file before the configs here.  load from classpath
   * @param configFileFromClasspath1 the configFileFromClasspath to set
   */
  public void setConfigFileFromClasspath(String configFileFromClasspath1) {
    this.configFileFromClasspath = configFileFromClasspath1;
  }

  /**
   * ldap.personLdap.referral: set to 'follow' if using AD and using paged results size
   * @return the referral
   */
  public String getReferral() {
    return this.referral;
  }
  
  /**
   * ldap.personLdap.referral: set to 'follow' if using AD and using paged results size
   * @param referral1 the referral to set
   */
  public void setReferral(String referral1) {
    this.referral = referral1;
  }

  /**
   * if the ldap server has a max page size, then this will get the results in pages
   * @return page size
   */
  public int getPagedResultsSize() {

    return this.pagedResultsSize;

  }

  /**
   * if the ldap server has a max page size, then this will get the results in pages
   * @param pagedResultsSize1
   */
  public void setPagedResultsSize(int pagedResultsSize1) {

    this.pagedResultsSize = pagedResultsSize1;

  }

  
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
   * if validating, the LDAPFactory validator
   * @param validator
   */
  public void setValidator(LdapValidator<Ldap> validator) {
    this.validator = validator;
  }

  /**
   * if validating, the LDAPFactory validator
   * @return the LDAPFactory validator
   */
  public LdapValidator<Ldap> getValidator() {
    return this.validator;
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
    return "GrouperLoaderLdapServer [batchSize=" + batchSize
        + ", configFileFromClasspath: " + this.configFileFromClasspath
        + ", countLimit="
        + countLimit + ", driver=" + driver + ", expirationTime=" + expirationTime
        + ", maxPoolSize=" + maxPoolSize + ", minPoolSize=" + minPoolSize
        + ", pagedResultsSize=" + pagedResultsSize
        + ", pass="
        + (StringUtils.isBlank(pass) ? "" : "XXXXX") 
        + ", pruneTimerPeriod=" + pruneTimerPeriod + ", referral=" + referral
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

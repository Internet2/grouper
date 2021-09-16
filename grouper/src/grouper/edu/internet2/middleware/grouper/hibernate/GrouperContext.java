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
 * $Id: GrouperContext.java,v 1.7 2009-08-20 07:24:37 isgwb Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.audit.GrouperEngineIdentifier;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * <pre>
 * holds threadlocal information about the current context of the database transactions.
 * 
 * The inner context is for operations like addGroup.
 * The outer context is for e.g. web requests like UI or WS
 * </pre>
 */
public class GrouperContext {
  
  /** for testing, see how many queries */
  public static long totalQueryCount = 0;
  
  /**
   * grouper engine, e.g. grouperUI
   */
  private String grouperEngine;
  
  /**
   * see if there is an inner context
   * @return true if inner context
   */
  public static boolean contextExistsInner() {
    return currentInnerContext.get() != null;
  }
  
  /** ip address of caller, or of gsh host */
  private String callerIpAddress;

  /**
   * id of the current context, this is lazy loaded 
   */
  private String contextId;

  /**
   * count the number of queries that an audit requires 
   */
  private int queryCount;

  /** member id of the logged in user */
  private String loggedInMemberId;
  
  /** member id that the logged in user is acting as */
  private String loggedInMemberIdActAs;
  
  /**
   * this is not a timestamp, but rather is the nanos of when it started 
   */
  private long startedNanos = System.nanoTime();
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperContext.class);

  /**
   * assign fields in audit entry from outer audit (note, this might not exist)
   * @param auditEntry
   */
  public static void assignAuditEntryFieldsOuter(AuditEntry auditEntry) {

    auditEntry.setCreatedOn(new Timestamp(System.currentTimeMillis()));
    auditEntry.setEnvName(GrouperConfig.retrieveConfig().propertyValueString("grouper.env.name"));
    auditEntry.setGrouperVersion(GrouperVersion.grouperVersion());
    auditEntry.setServerUserName(System.getProperty("user.name"));
    String serverHost = null;
    try {
      serverHost = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {
      //dont worry about it
      LOG.info(e);
    }
    auditEntry.setServerHost(serverHost);
    GrouperContext grouperDefaultContext = defaultContext.get();
    if (grouperDefaultContext != null) {
      
      auditEntry.setGrouperEngine(grouperDefaultContext.grouperEngine);
      auditEntry.setUserIpAddress(grouperDefaultContext.callerIpAddress);
      auditEntry.setLoggedInMemberId(grouperDefaultContext.loggedInMemberId);
      auditEntry.setActAsMemberId(grouperDefaultContext.loggedInMemberIdActAs);
      if(auditEntry.getActAsMemberId()==null) {
    	  GrouperSession s = GrouperSession.staticGrouperSession();
    	  //If there isn't an ActAsMemberId, but there is an active GrouperSession
    	  //and its subject doesn't match the loggedInMemberId, use it for the actAsMemberId.
    	  //Means that from GSH, where there is no loggedInMemberId, the current GrouperSession
    	  //determines the actAsMemberId which means that audit log entries can be filtered by
    	  //the nominal subject performing the action
    	  if(s != null && !s.getMemberUuid().equals(auditEntry.getLoggedInMemberId()) 
    	      && !SubjectHelper.eq(s.getSubject(), SubjectFinder.findRootSubject())) {
    		  auditEntry.setActAsMemberId(s.getMemberUuid());
    	  }
    	}

    }

    GrouperContext grouperOuterContext = currentOuterContext.get();
    if (grouperOuterContext != null) {
      
      if (!StringUtils.isBlank(grouperOuterContext.callerIpAddress)) {
        auditEntry.setUserIpAddress(grouperOuterContext.callerIpAddress);
      }
      
      if (!StringUtils.isBlank(grouperOuterContext.loggedInMemberId)) {
        auditEntry.setLoggedInMemberId(grouperDefaultContext.loggedInMemberId);
      }
      if (!StringUtils.isBlank(grouperOuterContext.loggedInMemberIdActAs)) {       
        auditEntry.setActAsMemberId(grouperDefaultContext.loggedInMemberIdActAs);
      }
    }
  }
  
  /**
   * 
   * @return the grouper enginge
   */
  public GrouperEngineBuiltin getGrouperEngine() {
    return GrouperEngineBuiltin.valueOfIgnoreCase(this.grouperEngine, false, false);
  }

  /**
   * assign fields in audit entry
   * @param auditEntry
   */
  public static void assignAuditEntryFields(AuditEntry auditEntry) {

    assignAuditEntryFieldsOuter(auditEntry);
    
    GrouperContext grouperInnerContext = currentInnerContext.get();
    if (grouperInnerContext == null) {
      LOG.debug("grouperInnerContext is null, was it not started?");
      return;
    }
    auditEntry.setContextId(grouperInnerContext.getContextId());
    //divide nanos by 1000 to get micros
    auditEntry.setDurationMicroseconds((System.nanoTime() - grouperInnerContext.startedNanos)/1000);
    auditEntry.setQueryCount(grouperInnerContext.queryCount);
    
    if (StringUtils.isBlank(auditEntry.getGrouperEngine())) {
      auditEntry.setGrouperEngine(grouperInnerContext.grouperEngine);
    }
    
  }
  
  /**
   * context around a web request (e.g. UI or WS)
   */
  private static ThreadLocal<GrouperContext> currentOuterContext = 
    new InheritableThreadLocal<GrouperContext>();
  
  /**
   * default settings if not another set
   */
  private static ThreadLocal<GrouperContext> defaultContext = 
    new InheritableThreadLocal<GrouperContext>();
  
  /**
   * 
   * @return the default context
   */
  public static GrouperContext retrieveDefaultContext() {
    return defaultContext.get();
  }
  
  /**
   * 
   */
  private static ThreadLocal<GrouperContext> currentInnerContext = 
    new InheritableThreadLocal<GrouperContext>();

  /**
   * internal use only, current inner context
   * @return current inner context
   */
  public static GrouperContext internal_retrieveCurrentInnerContext() {
    return currentInnerContext.get();
  }
  
  /**
   * internal use only, set current inner context
   * @param newCurrentInnerContext current inner context
   */
  public static void internal_assignCurrentInnerContext(GrouperContext newCurrentInnerContext) {
    if (newCurrentInnerContext == null) { 
      currentInnerContext.remove();
    } else {
      currentInnerContext.set(newCurrentInnerContext);
    }
  }
  
  /**
   * internal use only, current outer context
   * @return current outer context
   */
  public static GrouperContext internal_retrieveCurrentOuterContext() {
    return currentOuterContext.get();
  }
  
  /**
   * internal use only, set current outer context
   * @param newCurrentOuterContext current outer context
   */
  public static void internal_assignCurrentOuterContext(GrouperContext newCurrentOuterContext) {
    if (newCurrentOuterContext == null) { 
      currentOuterContext.remove();
    } else {
      currentOuterContext.set(newCurrentOuterContext);
    }
  }
  
  /**
   * internal use only, default context
   * @return default context
   */
  public static GrouperContext internal_retrieveDefaultContext() {
    return defaultContext.get();
  }
  
  /**
   * internal use only, set default context
   * @param newDefaultContext default context
   */
  public static void internal_assignDefaultContext(GrouperContext newDefaultContext) {
    if (newDefaultContext == null) { 
      defaultContext.remove();
    } else {
      defaultContext.set(newDefaultContext);
    }
  }
  
  /**
   * retrieve current context id
   * @param requireContext true to require context (if required in grouper.properties)
   * @return context id
   */
  public static String retrieveContextId(boolean requireContext) {
    GrouperContext grouperContextInner = currentInnerContext.get();
    if (grouperContextInner == null) {
      //TODO make these default to true before 1.5
      if (requireContext) {
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("audit.requireAuditsForAllActions", false)) {
          throw new RuntimeException("No context found");
        }
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("audit.logAuditsForMissingActions", false)) {
          LOG.warn("No context found here is the stack", new RuntimeException("Not an exception, just need the stack"));
        }
      }
      return null;
    }
    return grouperContextInner.getContextId();
  }
  
  /**
   * create a new context if one doesnt already exist
   * @return true if created one, false if already existed
   */
  static boolean createNewInnerContextIfNotExist() {
    if (currentInnerContext.get() != null) {
      return false;
    }
    GrouperContext grouperContextPrivate = new GrouperContext();
    currentInnerContext.set(grouperContextPrivate);
    return true;
  }
  
  /**
   * drop the current default context
   */
  public static void deleteDefaultContext() {
    defaultContext.remove();
  }
  
  /**
   * @param exceptionIfDefaultAlreadyExists
   * @param grouperEngineIdentifier
   * @param useServerIpAddressAsUserIpAddress if the users use this server, use that for user ip address
   * @return the context
   */
  public static GrouperContext createNewDefaultContext(GrouperEngineIdentifier grouperEngineIdentifier, 
      boolean exceptionIfDefaultAlreadyExists, boolean useServerIpAddressAsUserIpAddress) {
    
    if (exceptionIfDefaultAlreadyExists && defaultContext.get() != null) {
      throw new RuntimeException("Default context is already set: " 
          + defaultContext.get() + ", " + grouperEngineIdentifier);
    }
    
    GrouperContext grouperContext = new GrouperContext();
    grouperContext.grouperEngine = grouperEngineIdentifier.getGrouperEngine();
    defaultContext.set(grouperContext);
    
    if (useServerIpAddressAsUserIpAddress) {
      try {
        grouperContext.setCallerIpAddress(InetAddress.getLocalHost().getHostAddress());
      } catch (UnknownHostException uhe) {
        //not the end of the world
        LOG.info(uhe);
      }
    }
    
    return grouperContext;
  }

  /**
   * tell the context another query occurred
   */
  public static void incrementQueryCount() {
    incrementQueryCount(1);
  }

  /**
   * tell the context another query occurred
   */
  public static void incrementQueryCount(int queries) {
    
    totalQueryCount+=queries;
    
    GrouperContext grouperContextInner = currentInnerContext.get();
    if (grouperContextInner != null) {
      grouperContextInner.queryCount+=queries;
    }
    
    GrouperContext grouperContextOuter = currentOuterContext.get();
    if (grouperContextOuter != null) {
      grouperContextOuter.queryCount+=queries;
    }
  }

  /**
   * delete the private context if just created
   */
  static void deleteInnerContext() {
    currentInnerContext.remove();
  }

  /**
   * context id
   * @return context id
   */
  public String getContextId() {
    if (this.contextId == null) {
      this.contextId = GrouperUuid.getUuid();
    }
    return this.contextId;
  }

  /**
   * ip address of caller, or of gsh host
   * @return the ip address
   */
  public String getCallerIpAddress() {
    return this.callerIpAddress;
  }

  /**
   * ip address of caller, or of gsh host
   * @param callerIpAddress1
   */
  public void setCallerIpAddress(String callerIpAddress1) {
    this.callerIpAddress = callerIpAddress1;
  }

  /**
   * member id of the logged in user
   * @return member id
   */
  public String getLoggedInMemberId() {
    return this.loggedInMemberId;
  }

  /**
   * member id of the logged in user
   * @param loggedInMemberId1
   */
  public void setLoggedInMemberId(String loggedInMemberId1) {
    this.loggedInMemberId = loggedInMemberId1;
  }

  /**
   * member id that the logged in user is acting as
   * @return member id
   */
  public String getLoggedInMemberIdActAs() {
    return this.loggedInMemberIdActAs;
  }

  /**
   * member id that the logged in user is acting as
   * @param loggedInMemberIdActAs1
   */
  public void setLoggedInMemberIdActAs(String loggedInMemberIdActAs1) {
    this.loggedInMemberIdActAs = loggedInMemberIdActAs1;
  }
}

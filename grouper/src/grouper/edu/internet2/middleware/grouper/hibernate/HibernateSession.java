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
/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.internal.SessionImpl;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperReadonlyException;
import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
import edu.internet2.middleware.grouper.exception.GrouperStaleStateException;
import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Hibernate helper class.  These are kept in a threadlocal to keep 
 * transactions going smoothly.  If you are in a nested callback situation,
 * and in the same transaction, then the HibernateSession instance will be
 * different, but the underlying Session (from hibernate) object will be the same.  
 * To get an instanceof HibernateSession, use the callbackHibernateSession
 * inverse of control method.
 * 
 * </pre>
 * 
 * @author mchyzer
 * 
 */
public class HibernateSession {

  /**
   * error message when readonly mode
   */
  private static final String READONLY_ERROR = "Grouper is in readonly mode (perhaps due to maintenance), you cannot perform an operation which changes the data!";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(HibernateSession.class);

  /**
   * 
   * @return the class
   */
  public ByObject byObject(){
    return new ByObject(this);
  }
  
  /** save point count for testing */
  static int savePointCount = 0;
  
  /** threadlocal to store if we are in readonly mode */
  private static ThreadLocal<Boolean> threadlocalReadonly = new ThreadLocal<Boolean>();
    
  /**
   * @return the internal_threadlocalReadonly
   */
  public static Boolean internal_retrieveThreadlocalReadonly() {
    return threadlocalReadonly.get();
  }

  
  /**
   * @param internal_threadlocalReadonly the internal_threadlocalReadonly to set
   */
  public static void internal_assignThreadlocalReadonly(Boolean internal_threadlocalReadonly) {
    if (internal_threadlocalReadonly == null) {
      threadlocalReadonly.remove();
    } else {
      threadlocalReadonly.set(internal_threadlocalReadonly);
    }
  }

  /**
   * if readonly by threadlocal or config param
   * @return true if readonly
   */
  public static boolean isReadonlyMode() {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.api.readonly", false)) {
      return true;
    }
    
    Boolean threadlocalBoolean = threadlocalReadonly.get();
    
    return threadlocalBoolean != null && threadlocalBoolean;
    
  }

  /**
   * 
   * assign that grouper is in readonly mode, make sure to call clear in a finally block
   */
  public static void threadLocalReadonlyAssign() {
    threadlocalReadonly.set(true);
  }

  /**
   * in finally block call this to not make grouper readonly anymore
   */
  public static void threadLocalReadonlyClear() {
    threadlocalReadonly.remove();
  }
  
  /**
   * construct a hibernate session based on existing hibernate session (if
   * applicable), and a transaction type. If these conflict, then throw grouper
   * dao exception exception
   * 
   * @param parentHibernateSession is the parent session
   *          if exists
   * @param grouperTransactionType
   *          that this was created with
   * @throws GrouperDAOException
   *           if something conflicts (e.g. read/write if exists, and exists is
   *           readonly
   */
  @SuppressWarnings("deprecation")
  private HibernateSession(HibernateSession parentHibernateSession,
      GrouperTransactionType grouperTransactionType) throws GrouperDAOException {

    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    GrouperTransactionType originalGrouperTransactionType = grouperTransactionType;
    try {

      if (LOG.isDebugEnabled()) {
        debugMap.put("grouperTransactionType", grouperTransactionType);
      }
      
      boolean okToUseHibernate = GrouperDdlUtils.okToUseHibernate();
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("okToUseHibernate", okToUseHibernate);
      }
        
      if (!okToUseHibernate) {
        if (GrouperConfig.retrieveConfig().propertyValueBoolean("ddlutils.failIfNotRightVersion", true)) {
          throw new RuntimeException("Database schema ddl is not up to date, or has issues, check logs and config ddl in grouper.properties and run: gsh -registry -check");
        }
      }
      
      //if readonly, then dont allow read/write transactions
      boolean readonlyMode = isReadonlyMode();

      if (LOG.isDebugEnabled()) {
        debugMap.put("readonlyMode", readonlyMode);
      }
        
      if (readonlyMode) {
        if (grouperTransactionType != null && grouperTransactionType.isTransactional()) {
          grouperTransactionType = GrouperTransactionType.READONLY_OR_USE_EXISTING;
          
          if (LOG.isDebugEnabled() && grouperTransactionType != originalGrouperTransactionType) {
            debugMap.put("readonlyGrouperTransactionTypeChangedTo", grouperTransactionType);
          }
          originalGrouperTransactionType = grouperTransactionType;
        }
      }
      
      boolean parentSessionExists = parentHibernateSession != null;
      if (LOG.isDebugEnabled()) {
        debugMap.put("parentSessionExists", parentSessionExists);
      }
      
      this.immediateGrouperTransactionTypeDeclared = grouperTransactionType;
      
      //if parent is none, then make sure this is a new transaction (not dependent on none)
      if (parentSessionExists) {
        
        this.cachingEnabled = parentHibernateSession.cachingEnabled;
  
      }
  
      //if parent is none, then make sure this is a new transaction (not dependent on none)
      if (parentSessionExists && !grouperTransactionType.isNewAutonomous() && 
          parentHibernateSession.activeHibernateSession().immediateGrouperTransactionTypeUsed.isTransactional()) {
  
        //if there is a parent, then it is inherited.  even if not autonomous, only inherit if not parent of none
        this.parentSession = parentHibernateSession;
      
        //make sure the transaction types jive with each other
        this.immediateGrouperTransactionTypeDeclared.checkCompatibility(
            this.parentSession.getGrouperTransactionType());
      }
      
      boolean newHibernateSession = this.isNewHibernateSession();
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("newHibernateSession", newHibernateSession);
      }
      
      if (newHibernateSession) {
        
        if (grouperTransactionType == null) {
          throw new NullPointerException("transaction type is null in hibernate session");
        }
        
        this.immediateGrouperTransactionTypeUsed = grouperTransactionType
            .grouperTransactionTypeToUse();
  
        // need a hibernate session (note, if none, then we dont need a session?)
        if (!GrouperTransactionType.NONE.equals(grouperTransactionType)) {
          this.immediateSession = GrouperDAOFactory.getFactory().getSession();
        }

        if (LOG.isDebugEnabled()) {
          debugMap.put("immediateGrouperTransactionTypeUsed", this.immediateGrouperTransactionTypeUsed);
          debugMap.put("immediateGrouperTransactionTypeReadonly", this.immediateGrouperTransactionTypeUsed.isReadonly());
        }

        // if not readonly, declare a transaction
        if (!this.immediateGrouperTransactionTypeUsed.isReadonly()) {
          if (LOG.isDebugEnabled()) {
            debugMap.put("beginTransaction", "true");
          }
          this.immediateTransaction = this.immediateSession.beginTransaction();
  
          String useSavepointsString = GrouperConfig.retrieveConfig().propertyValueString("jdbc.useSavePoints");
          boolean useSavepoints;
          if (StringUtils.isBlank(useSavepointsString)) {
            useSavepoints = !GrouperDdlUtils.isHsql();
          } else {
            useSavepoints = GrouperUtil.booleanValue(useSavepointsString);
          }

          if (LOG.isDebugEnabled()) {
            debugMap.put("useSavepoints", useSavepoints);
          }
          
          if (useSavepoints && (parentSessionExists   // && this.activeHibernateSession().isTransactionActive()  && !this.activeHibernateSession().isReadonly() 
              || GrouperConfig.retrieveConfig().propertyValueBoolean("jdbc.useSavePointsOnAllNewTransactions", false))) {
            try {
              this.savepoint = ((SessionImpl)this.activeHibernateSession().getSession()).connection().setSavepoint();
              savePointCount++;
            } catch (SQLException sqle) {
              throw new RuntimeException("Problem setting save point for transaction type: " 
                  + grouperTransactionType, sqle);
            }
          } else if (GrouperDdlUtils.isHsql() && parentSessionExists) {
            //do this for tests...
            savePointCount++;
          }
        }
      }
      
      
      addStaticHibernateSession(this);
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("hibernateSession", this.toString());
        LOG.debug(GrouperUtil.stack());
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  /** hibernate session object of parent if nested, or null */
  private HibernateSession parentSession = null;

  /**
   * hibernate session object can be accessed by user, if there is a parent,
   * this will be null.
   */
  private Session immediateSession = null;

  /**
   * if read/write, this will exist, though the user cant access directly. if
   * there is a parent, this will ne null
   */
  private Transaction immediateTransaction = null;

  /**
   * if read/write, postgres needs to rollback to save point if nested transaction
   */
  private Savepoint savepoint = null;
  
  /**
   * the transaction type this was setup as. note, if the type is new, then it
   * might change from what it was declared as...
   */
  private GrouperTransactionType immediateGrouperTransactionTypeUsed = null;

  /**
   * the transaction type this was setup as. this is the one declared in
   * callback
   */
  private GrouperTransactionType immediateGrouperTransactionTypeDeclared = null;

  /**
   * provide ability to turn off all caching for this session
   */
  private boolean cachingEnabled = true;
  
  /**
   * provide ability to turn off all caching for this session
   * @return the enabledCaching
   */
  public boolean isCachingEnabled() {
    return this.cachingEnabled;
  }

  /**
   * provide ability to turn off all caching for this session
   * note, you can also use a try/finally with HibUtils.assignDisallowCacheThreadLocal() and 
   * HibUtils.clearDisallowCacheThreadLocal()
   * @param enabledCaching1 the enabledCaching to set
   */
  public void setCachingEnabled(boolean enabledCaching1) {
    this.cachingEnabled = enabledCaching1;
  }

  /**
   * store the hib2 connection in thread local so other classes can get it
   */
  private static ThreadLocal<Set<HibernateSession>> staticSessions = new ThreadLocal<Set<HibernateSession>>();

  /**
   * this is for internal purposes only, dont use this unless you know what you are doing
   * @return the set of hibernate sessions
   */
  public static Set<HibernateSession> _internal_staticSessions() {
    return getHibernateSessionSet();
  }
  
  /**
   * get the threadlocal set of hibernate sessions (or create)
   * 
   * @return the set
   */
  private static Set<HibernateSession> getHibernateSessionSet() {
    Set<HibernateSession> hibSet = staticSessions.get();
    if (hibSet == null) {
      // note the sessions are in order
      hibSet = new LinkedHashSet<HibernateSession>();
      staticSessions.set(hibSet);
    }
    return hibSet;
  }

  /**
   * this should remove the last hibernate session which should be the same as
   * the one passed in
   * 
   * @param hibernateSession
   */
  private static void removeStaticHibernateSession(HibernateSession hibernateSession) {
    getHibernateSessionSet().remove(hibernateSession);
  }

  /**
   * call this at the end of requests to make sure everything is cleared out or
   * call periodically...
   */
  public static void resetAllThreadLocals() {
    getHibernateSessionSet().clear();
  }

  /**
   * set the threadlocal hibernate session
   * 
   * @param hibernateSession
   */
  private static void addStaticHibernateSession(HibernateSession hibernateSession) {
    Set<HibernateSession> hibSet = getHibernateSessionSet();
    hibSet.add(hibernateSession);
    // cant have more than 20, something is wrong
    if (hibSet.size() > 20) {
      hibSet.clear();
      throw new RuntimeException(
          "There is probably a problem that there are 20 nested new HibernateSessions called!");
    }
  }

  /**
   * get the current hibernate session.  dont call this unless you know what you are doing
   * @return the current hibernate session
   */
  public static HibernateSession _internal_hibernateSession() {
    return staticHibernateSession();
  }
  
  /**
   * get the threadlocal hibernate session. access this through inverse of
   * control (granted you wont get the exact same instance...)
   * 
   * @return the hibernate session or null if none there
   */
  private static HibernateSession staticHibernateSession() {
    Set<HibernateSession> hibSet = getHibernateSessionSet();
    int size = hibSet.size();
    if (size == 0) {
      return null;
    }
    // get the second to last index
    return (HibernateSession) GrouperUtil.get(hibSet, size - 1);
  }

  /**
   * close all sessions, but dont throw errors, based on throwable
   * @param t 
   */
  public static void _internal_closeAllHibernateSessions(Throwable t) {
    //if there is an exception, close all sessions
    try {
      for (HibernateSession hibernateSession : HibernateSession._internal_staticSessions()) {
        try {
          HibernateSession._internal_hibernateSessionCatch(hibernateSession, t);
        } catch (Exception e) {
          //swallow I guess
          LOG.debug("Error handling hibernate error", e);
        } finally {
          try {
            HibernateSession._internal_hibernateSessionFinally(hibernateSession);
          } catch (Throwable t3) {
            LOG.debug("Error in finally for hibernate session", t3);
          }
        }
      }
    } catch (Throwable t2) {
      LOG.debug("Problem closing sessions", t2);
    }
  }
  
  /**
   * dont call this method unless you know what you are doing
   * @param grouperTransactionType
   * @return the hiberate session for internal purposes
   * @throws GrouperDAOException
   */
  public static HibernateSession _internal_hibernateSession(GrouperTransactionType grouperTransactionType) throws GrouperDAOException {
    HibernateSession hibernateSession = new HibernateSession(staticHibernateSession(), grouperTransactionType);
    return hibernateSession;
  }
  
  /**
   * end a hibernate session.  dont call this unless you know what you are doing
   * @param hibernateSession 
   * @throws SQLException 
   */
  @SuppressWarnings("deprecation")
  public static void _internal_hibernateSessionEnd(HibernateSession hibernateSession) throws SQLException {
    
    //since we have long running transactions, we need to flush our work,
    //and disassociate objects with the session...
    Session session = hibernateSession.activeHibernateSession().immediateSession;

    //if we are readonly, and we have work, then that is bad
    if (hibernateSession.isReadonly() 
        && session != null && session.isDirty()) {
      ((SessionImpl)session).connection().rollback();
      //when i retrieve a bunch of fields, this doesnt work.  why???
      //throw new RuntimeException("Hibernate session is readonly, but some committable work was done!");
    }
    
    // maybe we didnt commit. if new session, and no exception, and not
    // committed or rolledback,
    // then commit.
    if (hibernateSession.isNewHibernateSession() && !hibernateSession.isReadonly()
        && hibernateSession.immediateTransaction.getStatus().isOneOf(TransactionStatus.ACTIVE)) {

      LOG.debug("endTransactionAutoCommit");
      
      assertNotGrouperReadonly();
      
      hibernateSession.immediateTransaction.commit();

    } else {
      //only do this if a nested transaction
      
      if (session != null && !hibernateSession.isNewHibernateSession()) {
        //put all the queries on the wire
        session.flush();

        //clear out session to avoid duplicate objects in session
        session.clear();
      }
    }
    
  }

  /**
   * make sure not readonly mode
   */
  public static void assertNotGrouperReadonly() {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.api.readonly", false)) {
      throw new GrouperReadonlyException(READONLY_ERROR);
    }
  }

  /**
   * catch and handle an exception while working with hibernate session.  Dont call this if you dont know what you are doing.
   * @param hibernateSession
   * @param e 
   * @throws GrouperDAOException 
   */
  @SuppressWarnings("deprecation")
  public static void _internal_hibernateSessionCatch(HibernateSession hibernateSession, Throwable e) throws GrouperDAOException {

    try {
      //if there was a save point, rollback (since postgres doesnt like a failed query not rolled back)
      if (hibernateSession != null && hibernateSession.savepoint != null) {
        try {
          ((SessionImpl)hibernateSession.activeHibernateSession().getSession()).connection().rollback(hibernateSession.savepoint);
        } catch (SQLException sqle) {
          throw new RuntimeException("Problem rolling back savepoint", sqle);
        }
      }
    } catch (RuntimeException re) {
      //hmmm, dont die on a rollback, but put it in the original exception
      if (!GrouperUtil.injectInException(e, "Exception rolling back savepoint in exception catch: " + ExceptionUtils.getFullStackTrace(re))) {
        LOG.error("Error", e);
      }
    }    
    
    try {
      // maybe we didnt rollback. if new session, and exception, and not
      // committed or rolledback,
      // then rollback.
      //CH 20080220: should we always rollback?  or if not rollback, flush and clear?
      if (hibernateSession != null && hibernateSession.isNewHibernateSession() && !hibernateSession.isReadonly()) {
        if (hibernateSession.immediateTransaction.getStatus().isOneOf(TransactionStatus.ACTIVE)) {
          LOG.debug("endTransactionRollback");
          hibernateSession.immediateTransaction.rollback();
        }
      }
    } catch (RuntimeException re) {
      //hmmm, dont die on a rollback, but put it in the original exception
      if (!GrouperUtil.injectInException(e, "Exception rolling back in exception catch: " + ExceptionUtils.getFullStackTrace(re))) {
        LOG.error("Error", e);
      }
    }
    
    //postgres logs in nextException, so see if there is one there
    GrouperUtil.logErrorNextException(LOG, e, 100);
    
    String errorString = "Problem in HibernateSession: " + hibernateSession;
    // rethrow
    if (e instanceof GrouperDAOException) {
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString);
      }
      throw (GrouperDAOException) e;
    }
    if (e instanceof StaleObjectStateException) {
      throw new GrouperStaleObjectStateException(errorString, e);
    }
    if (e instanceof StaleStateException) {
      throw new GrouperStaleStateException(errorString, e);
    }
    // if hibernate exception, repackage
    if (e instanceof HibernateException) {
      throw new GrouperDAOException(errorString, e);
    }
    if (e instanceof HookVeto) {
      throw (HookVeto)e;
    }
    if (e instanceof GrouperValidationException) {
      throw (GrouperValidationException)e;
    }
    // if runtime, then rethrow
    if (e instanceof RuntimeException) {
      if (!GrouperUtil.injectInException(e, errorString)) {
        LOG.error(errorString);
      }
      throw (RuntimeException) e;
    }
    // if exception and not handled, convert to GrouperDaoException
    throw new GrouperDAOException(errorString, e);

  }
  
  /**
   * finally block from hibernate session (dont call unless you know what you are doing
   * 
   * @param hibernateSession
   * @return if closed
   */
  public static boolean _internal_hibernateSessionFinally(HibernateSession hibernateSession) {
    if (hibernateSession != null) {
      // take out of threadlocal stack
      removeStaticHibernateSession(hibernateSession);
      // take out of threadlocal if supposed to
      if (hibernateSession.isNewHibernateSession()) {
        // we should close the hibernate session if we opened it, and if not
        // already closed
        // transaction is already closed...
        return closeSessionIfNotClosed(hibernateSession.immediateSession);
      }
    }
    return false;
  }
  
  /**
   * call this to send a callback for the hibernate session object. cant use
   * inverse of control for this since it runs it
   * 
   * @param grouperTransactionType
   *          is enum of how the transaction should work.
   * @param auditControl WILL_AUDIT if caller will create an audit record, WILL_NOT_AUDIT if not
   * @param hibernateHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws GrouperDAOException
   *           if there is a problem, will preserve runtime exceptions so they are
   *           thrown to the caller
   */
  public static Object callbackHibernateSession(
      GrouperTransactionType grouperTransactionType, AuditControl auditControl, HibernateHandler hibernateHandler)
      throws GrouperDAOException {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    
    Object ret = null;
    HibernateSession hibernateSession = null;

    try {
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("grouperTransactionType", grouperTransactionType == null ? null : grouperTransactionType.name());
        debugMap.put("auditControl", auditControl == null ? null : auditControl);
      }
      
      hibernateSession = _internal_hibernateSession(grouperTransactionType);

      if (LOG.isDebugEnabled()) {

        debugMap.put("hibernateSession", hibernateSession.toString());

        StringBuilder sessionsThreadLocal = new StringBuilder();
        boolean first = true;
        for (HibernateSession theHibernateSession : getHibernateSessionSet()) {
          if (!first) {
            sessionsThreadLocal.append(", ");
          }
          sessionsThreadLocal.append(Integer.toHexString(theHibernateSession.hashCode()));
          first = false;
        }
        debugMap.put("hibernateSessionsInThreadLocal", sessionsThreadLocal.toString());
      }
      
      HibernateHandlerBean hibernateHandlerBean = new HibernateHandlerBean();
      boolean willCreateAudit = AuditControl.WILL_AUDIT.equals(auditControl);
      if (LOG.isDebugEnabled()) {
        debugMap.put("willCreateAudit", willCreateAudit);
      }
      hibernateHandlerBean.setCallerWillCreateAudit(willCreateAudit);

      //see if the caller will audit.  if not, then it is up to this call
      boolean callerWillAudit = GrouperContext.contextExistsInner();

      //create a new context
      boolean createdContext = willCreateAudit ? GrouperContext.createNewInnerContextIfNotExist() : false;

      if (LOG.isDebugEnabled()) {
        debugMap.put("createdContext", createdContext);
      }

      try {
        hibernateHandlerBean.setCallerWillCreateAudit(callerWillAudit);
        hibernateHandlerBean.setNewContext(createdContext);
        
        hibernateHandlerBean.setHibernateSession(hibernateSession);
        
        ret = hibernateHandler.callback(hibernateHandlerBean);
      } finally {
        //if we created a context, then remove it
        if (createdContext) {
          GrouperContext.deleteInnerContext();
        }
      }
      _internal_hibernateSessionEnd(hibernateSession);

    } catch (Throwable e) {
      _internal_hibernateSessionCatch(hibernateSession, e);
    } finally {
      boolean closed = _internal_hibernateSessionFinally(hibernateSession);
      if (LOG.isDebugEnabled()) {
        debugMap.put("closedSession", closed);
        LOG.debug(GrouperUtil.stack());
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    return ret;

  }

  /**
   * do a hql query with proper error handling and in an enclosing transaction
   * (if applicable), or a new one if not
   * @return the class
   */
  public static ByHqlStatic byHqlStatic() {
    return new ByHqlStatic();
  }

  /**
   * do a sql query with proper error handling and in an enclosing transaction
   * (if applicable), or a new one if not
   * @return the class
   */
  public static BySqlStatic bySqlStatic() {
    return new BySqlStatic();
  }

  /**
   * do a criteria query with proper error handling and in an enclosing transaction
   * (if applicable), or a new one if not
   * @return the class
   */
  public static ByCriteriaStatic byCriteriaStatic() {
    return new ByCriteriaStatic();
  }

  /**
   * do an object operation 
   * with proper error handling and in an enclosing transaction
   * (if applicable), or a new one if not
   * @return the class
   */
  public static ByObjectStatic byObjectStatic() {
    return new ByObjectStatic();
  }

  /**
   * Close session. Do not throw or log error. This is good in a finally block
   * to make sure DB connections are returned.
   * 
   * @param session
   *          is hibernate session to close
   * @return if closed
   */
  private static boolean closeSessionIfNotClosed(Session session) {

    if (session != null) {

      try {
        // if already closed (not sure why), just ignore
        if (session.isConnected() && session.isOpen()) {
          session.close();
          return true;
        }
      } catch (Exception e) {
        // swallow the exception... no throwing, no logging
      }
    }
    return false;
  }

  /**
   * descriptive toString for error handling
   */
  @Override
  public String toString() {
    try {
      return "HibernateSession (" + Integer.toHexString(this.hashCode()) + "): " 
          + (this.isNewHibernateSession() ? "new" : "notNew") + ", " 
          + (this.isReadonly() ? "readonly" : "notReadonly") + ", "
          + (this.getGrouperTransactionType() == null ? null : this.getGrouperTransactionType().name()) + ", "
          + (this.isTransactionActive() ? "activeTransaction" : "notActiveTransaction" ) 
          + ", session (" + (this.getSession() == null ? null : Integer.toHexString(this.getSession().hashCode())) + ")"
          ;
    } catch (NullPointerException npe) {
      throw npe;
    }
  }

  /**
   * hibernate session object can be accessed by user.
   * 
   * @return the session
   */
  public Session getSession() {
    return this.activeHibernateSession().immediateSession;
  }

  /**
   * misc actions for hibernate session
   * @return the class
   */
  public HibernateMisc misc(){
    return new HibernateMisc(this);
  }

  /**
   * hql action for hibernate
   * @return the byhql
   */
  public ByHql byHql(){
    return new ByHql(this);
  }

  /**
   * hql action for hibernate
   * @return the byhql
   */
  public BySql bySql(){
    return new BySql(this);
  }

  /**
   * see if this is a new hibernate session
   * 
   * @return the newHibernateSession
   */
  public boolean isNewHibernateSession() {
    // if no parent, then it is new, unless it is a new autonomous transaction
    //in which case there will be no parent...
    return this.parentSession == null;
  }

  /**
   * if this is readonly (based on this declaration or underlying)
   * 
   * @return the readonly
   */
  public boolean isReadonly() {
    return this.getGrouperTransactionType().isReadonly();
  }

  /**
   * get this object if new, or underlying if exist
   * 
   * @return the hibernate session
   */
  private HibernateSession activeHibernateSession() {
    return this.parentSession == null ? this : this.parentSession
        .activeHibernateSession();
  }

  /**
   * this will return the underlying (if exist) transaction type, and if not,
   * then the one this was constructed with
   * 
   * @return the hibernate transaction type
   */
  public GrouperTransactionType getGrouperTransactionType() {
    return this.activeHibernateSession().immediateGrouperTransactionTypeUsed;
  }

  /**
   * commit (perhaps, depending on type)
   * @param grouperCommitType is type of commit
   * @return true if committed, false if not
   */
  public boolean commit(GrouperCommitType grouperCommitType) {

    assertNotGrouperReadonly();
    
    switch (grouperCommitType) {
      case COMMIT_IF_NEW_TRANSACTION:
        if (this.isNewHibernateSession()) {
          LOG.debug("endTransactionCommitIfNew");
          this.activeHibernateSession().immediateTransaction.commit();
          this.activeHibernateSession().savepoint = null;
          return true;
        }
        break;
      case COMMIT_NOW:
        LOG.debug("endTransactionCommitNow");
        this.activeHibernateSession().immediateTransaction.commit();
        this.activeHibernateSession().savepoint = null;
        return true;
    }
    return false;
  }

  /**
   * see if tx is active (not committed or rolled back, see Hibernate transaction
   * if there is no transaction, it will return false
   * @return true if active, false if not or not transaction
   */
  public boolean isTransactionActive() {
    if (this.isReadonly()) {
      return false;
    }
    return this.activeHibernateSession().immediateTransaction == null ? false : this
        .activeHibernateSession().immediateTransaction.getStatus().isOneOf(TransactionStatus.ACTIVE);
  }

  /**
   * rollback (perhaps, depending on type)
   * @param grouperRollbackType is type of rollback
   * @return true if rollback, false if not
   */
  public boolean rollback(GrouperRollbackType grouperRollbackType) {
    switch (grouperRollbackType) {
      case ROLLBACK_IF_NEW_TRANSACTION:
        if (this.isNewHibernateSession()) {
          LOG.debug("endTransactionRollbackIfNew");
          this.activeHibernateSession().immediateTransaction.rollback();
          return true;
        }
        break;
      case ROLLBACK_NOW:
        if (this.activeHibernateSession() != null && this.activeHibernateSession().immediateTransaction != null) { 
          LOG.debug("endTransactionRollbackNow");
          this.activeHibernateSession().immediateTransaction.rollback();
        }
        return true;
    }
    return false;
  }

  /**
   * Query hibernate objects by sql, get the list, and evict
   * @param query SQL query, but have curly brackets per hibernate spec, e.g.
   * SELECT {cat.*} FROM CAT {cat} WHERE cat.name='barney'
   * @param aliasOfObject is a string or array of strings of alias of the object in 
   * the SQL e.g. is SELECT {cat.*} FROM ... the alias is "cat"
   * @param types is the Class or Class[] of type of object(s) returned
   * @param params prepared statement params (null, Object, Object[], or List of Objects)
   * @param paramTypes prepared statement types(null, Type, Type[], or List of Types)
   * @return the array of objects
   */
  List retrieveListBySql(String query, String aliasOfObject, Class types,
      Object params, Object paramTypes) {
    
    List list = null;

    Query hibQuery = retrieveQueryBySql(query, aliasOfObject, types, params, paramTypes);
    
    list = hibQuery.list();
    if (list != null) {
      //make sure objects are not attached to hib session
      //only do this if hibernate objects
      if (types != null) {
        HibUtils.evict(this, list, true);
      }
    }
    return list;
  }

  /**
   * Query hibernate objects by sql
   * @param query SQL query, but have curly brackets per hibernate spec, e.g.
   * SELECT {cat.*} FROM CAT {cat} WHERE cat.name='barney'
   * @param aliasOfObject is a string or array of strings of alias of the object in 
   * the SQL e.g. is SELECT {cat.*} FROM ... the alias is "cat"
   * @param types is the Class or Class[] of type of object(s) returned
   * @param params prepared statement params (null, Object, Object[], or List of Objects)
   * @param paramTypes prepared statement types(null, Type, Type[], or List of Types)
   * @return the array of objects
   */
  @SuppressWarnings("unchecked") Query retrieveQueryBySql(String query, String aliasOfObject, Class types,
      Object params, Object paramTypes) {

    Query hibQuery = null;

    try {

      hibQuery = this.getSession().createSQLQuery(query);

      if (aliasOfObject != null && types != null) {
        hibQuery = ((SQLQuery)hibQuery).addEntity(aliasOfObject, types);
      } else if (types != null) {
        //if no entity then just get the list of object[] or objects
        //CH 061105 adding support for native queries for lists of object[]'s or objects
        hibQuery = ((SQLQuery)hibQuery).addEntity(types);
      }

      attachParams(hibQuery, params, paramTypes);

      
    } catch (Throwable he) {
      throw new RuntimeException("Error querying for array of objects, query: " + query + ", "
          + HibUtils.paramsToString(params, paramTypes), he);
    }

    return hibQuery;

  }


  /**
   * Attach params for a prepared statement
   * @param query is the hibernate query to attach to
   * @param params (null, Object, Object[], or List of Objects)
   * @param types (null, Type, Type[], or List of Types)
   */
  @SuppressWarnings("deprecation")
  static void attachParams(Query query, Object params, Object types) {

    //nothing to do if nothing to do
    if (GrouperUtil.length(params) == 0 && GrouperUtil.length(types) == 0) {
      return;
    }

    if (GrouperUtil.length(params) != GrouperUtil.length(types)) {
      throw new RuntimeException("The params length must equal the types length and params " +
      "and types must either both or neither be null");
    }

    int paramLength = -1;


    List paramList = GrouperUtil.toList(params);
    List typeList = GrouperUtil.toList(types);


      paramLength = paramList.size();

      if (paramLength != typeList.size()) {
        throw new RuntimeException("The params length " + paramLength
            + " must equal the types length " + typeList.size());
      }

      //loop through, set the params
      for (int i = 0; i < paramLength; i++) {
        //massage types
        Object param = paramList.get(i);
        Type type = (Type) typeList.get(i);
        
        //convert date
        if (type.equals(StandardBasicTypes.DATE)) {
          type = StandardBasicTypes.TIMESTAMP;
          //convert the data
          param = GrouperUtil.toTimestamp(param);
        }
        
        query.setParameter(i, param, type);
      }


    }

}

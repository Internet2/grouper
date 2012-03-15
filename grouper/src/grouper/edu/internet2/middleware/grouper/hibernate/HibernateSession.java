/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperReadonlyException;
import edu.internet2.middleware.grouper.exception.GrouperStaleObjectStateException;
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

    if (!GrouperDdlUtils.okToUseHibernate()) {
      if (GrouperConfig.getPropertyBoolean("ddlutils.failIfNotRightVersion", true)) {
        throw new RuntimeException("Database schema ddl is not up to date, or has issues, check logs and config ddl in grouper.properties and run: gsh -registry -check");
      }
    }
    
    //if readonly, then dont allow read/write transactions
    if (GrouperConfig.getPropertyBoolean("grouper.api.readonly", false)) {
      if (grouperTransactionType != null && grouperTransactionType.isTransactional()) {
        grouperTransactionType = GrouperTransactionType.READONLY_OR_USE_EXISTING;
      }
    }
    
    //if we arent using nested transactions, then just use parent if there is one...
    if (!GrouperConfig.getPropertyBoolean("ddlutils.use.nestedTransactions", true) && parentHibernateSession != null) {
      grouperTransactionType = parentHibernateSession.getGrouperTransactionType();
      //we dont want new transactions... not sure what happens if none... hmm
      if (grouperTransactionType.isNewAutonomous()) {
        if (grouperTransactionType == GrouperTransactionType.READ_WRITE_NEW) {
          grouperTransactionType = GrouperTransactionType.READ_WRITE_OR_USE_EXISTING;
        } else if (grouperTransactionType == GrouperTransactionType.READONLY_NEW) {
          grouperTransactionType = GrouperTransactionType.READONLY_OR_USE_EXISTING;
        }
      }
      LOG.debug("Not using nested transactions, converting transaction type to: " + parentHibernateSession.getGrouperTransactionType());
    }
    
    this.immediateGrouperTransactionTypeDeclared = grouperTransactionType;
    
    //if parent is none, then make sure this is a new transaction (not dependent on none)
    if (parentHibernateSession != null) {
      
      this.cachingEnabled = parentHibernateSession.cachingEnabled;

    }

    //if parent is none, then make sure this is a new transaction (not dependent on none)
    if (parentHibernateSession != null && !grouperTransactionType.isNewAutonomous() && 
        parentHibernateSession.activeHibernateSession().immediateGrouperTransactionTypeUsed.isTransactional()) {

      //if there is a parent, then it is inherited.  even if not autonomous, only inherit if not parent of none
      this.parentSession = parentHibernateSession;
    
      //make sure the transaction types jive with each other
      this.immediateGrouperTransactionTypeDeclared.checkCompatibility(
          this.parentSession.getGrouperTransactionType());
    }
    
    if (this.isNewHibernateSession()) {
      
      if (grouperTransactionType == null) {
        throw new NullPointerException("transaction type is null in hibernate session");
      }
      
      this.immediateGrouperTransactionTypeUsed = grouperTransactionType
          .grouperTransactionTypeToUse();

      // need a hibernate session (note, if none, then we dont need a session?)
      if (!GrouperTransactionType.NONE.equals(grouperTransactionType)) {
        this.immediateSession = GrouperDAOFactory.getFactory().getSession();
      }

      // if not readonly, declare a transaction
      if (!this.immediateGrouperTransactionTypeUsed.isReadonly()) {
        this.immediateTransaction = this.immediateSession.beginTransaction();

        String useSavepointsString = GrouperConfig.getProperty("jdbc.useSavePoints");
        boolean useSavepoints;
        if (StringUtils.isBlank(useSavepointsString)) {
          useSavepoints = !GrouperDdlUtils.isHsql();
        } else {
          useSavepoints = GrouperUtil.booleanValue(useSavepointsString);
        }
        
        if (useSavepoints && (parentHibernateSession != null   // && this.activeHibernateSession().isTransactionActive()  && !this.activeHibernateSession().isReadonly() 
            || GrouperConfig.getPropertyBoolean("jdbc.useSavePointsOnAllNewTransactions", false))) {
          try {
            this.savepoint = this.activeHibernateSession().getSession().connection().setSavepoint();
            savePointCount++;
          } catch (SQLException sqle) {
            throw new RuntimeException("Problem setting save point for transaction type: " 
                + grouperTransactionType, sqle);
          }
        } else if (GrouperDdlUtils.isHsql() && parentHibernateSession != null) {
          //do this for tests...
          savePointCount++;
        }
      }
    }
    
    
    addStaticHibernateSession(this);
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
      session.connection().rollback();
      //when i retrieve a bunch of fields, this doesnt work.  why???
      //throw new RuntimeException("Hibernate session is readonly, but some committable work was done!");
    }
    
    // maybe we didnt commit. if new session, and no exception, and not
    // committed or rolledback,
    // then commit.
    if (hibernateSession.isNewHibernateSession() && !hibernateSession.isReadonly()
        && hibernateSession.immediateTransaction.isActive()) {
      
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
    if (GrouperConfig.getPropertyBoolean("grouper.api.readonly", false)) {
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

    //if there was a save point, rollback (since postgres doesnt like a failed query not rolled back)
    if (hibernateSession != null && hibernateSession.savepoint != null) {
      try {
        hibernateSession.activeHibernateSession().getSession().connection().rollback(hibernateSession.savepoint);
      } catch (SQLException sqle) {
        throw new RuntimeException("Problem rolling back savepoint", sqle);
      }
    }
    
    
    // maybe we didnt rollback. if new session, and exception, and not
    // committed or rolledback,
    // then rollback.
    //CH 20080220: should we always rollback?  or if not rollback, flush and clear?
    if (hibernateSession != null && hibernateSession.isNewHibernateSession() && !hibernateSession.isReadonly()) {
      if (hibernateSession.immediateTransaction.isActive()) {
        hibernateSession.immediateTransaction.rollback();
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
    // if hibernate exception, repackage
    if (e instanceof HibernateException) {
      throw new GrouperDAOException(errorString, e);
    }
    if (e instanceof HookVeto) {
      throw (HookVeto)e;
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
   * @param hibernateSession
   */
  public static void _internal_hibernateSessionFinally(HibernateSession hibernateSession) {
    if (hibernateSession != null) {
      // take out of threadlocal stack
      removeStaticHibernateSession(hibernateSession);
      // take out of threadlocal if supposed to
      if (hibernateSession.isNewHibernateSession()) {
        // we should close the hibernate session if we opened it, and if not
        // already closed
        // transaction is already closed...
        closeSessionIfNotClosed(hibernateSession.immediateSession);
      }
    }

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
    Object ret = null;
    HibernateSession hibernateSession = null;

    try {
      
      hibernateSession = _internal_hibernateSession(grouperTransactionType);
      
      HibernateHandlerBean hibernateHandlerBean = new HibernateHandlerBean();
      boolean willCreateAudit = AuditControl.WILL_AUDIT.equals(auditControl);
      hibernateHandlerBean.setCallerWillCreateAudit(willCreateAudit);

      //see if the caller will audit.  if not, then it is up to this call
      boolean callerWillAudit = GrouperContext.contextExistsInner();

      //create a new context
      boolean createdContext = willCreateAudit ? GrouperContext.createNewInnerContextIfNotExist() : false;
      
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
      _internal_hibernateSessionFinally(hibernateSession);
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
   */
  private static void closeSessionIfNotClosed(Session session) {

    if (session != null) {

      try {
        // if already closed (not sure why), just ignore
        if (session.isConnected() && session.isOpen()) {
          session.close();
        }
      } catch (Exception e) {
        // swallow the exception... no throwing, no logging
      }
    }
  }

  /**
   * descriptive toString for error handling
   */
  @Override
  public String toString() {
    return "HibernateSession: isNew: " + this.isNewHibernateSession() + ", isReadonly: "
        + this.isReadonly() + ", grouperTransactionType: "
        + this.getGrouperTransactionType().name();
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
          this.activeHibernateSession().immediateTransaction.commit();
          this.activeHibernateSession().savepoint = null;
          return true;
        }
        break;
      case COMMIT_NOW:
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
        .activeHibernateSession().immediateTransaction.isActive();
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
          this.activeHibernateSession().immediateTransaction.rollback();
          return true;
        }
        break;
      case ROLLBACK_NOW:
        if (this.activeHibernateSession() != null && this.activeHibernateSession().immediateTransaction != null) { 
          this.activeHibernateSession().immediateTransaction.rollback();
        }
        return true;
    }
    return false;
  }

}

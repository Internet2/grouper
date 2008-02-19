/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
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

  /** logger */
  private static final Log LOG = LogFactory.getLog(HibernateSession.class);

  
  
  /**
   * construct a hibernate session based on existing hibernate session (if
   * applicable), and a transaction type. If these conflict, then throw grouper
   * dao exception exception
   * 
   * @param hibernateSession
   *          if exists
   * @param grouperTransactionType
   *          that this was created with
   * @throws GrouperDAOException
   *           if something conflicts (e.g. read/write if exists, and exists is
   *           readonly
   */
  private HibernateSession(HibernateSession hibernateSession,
      GrouperTransactionType grouperTransactionType)
      throws GrouperDAOException {

    this.parentSession = hibernateSession;
    if (this.isNewHibernateSession()) {
      this.immediateGrouperTransactionTypeDeclared = grouperTransactionType;
      this.immediateGrouperTransactionTypeUsed = grouperTransactionType
          .grouperTransactionTypeToUse();

      // need a hibernate session
      this.immediateSession = GrouperDAOFactory.getFactory().getSession();
      addStaticHibernateSession(hibernateSession);

      // if not readonly, declare a transaction
      if (!this.immediateGrouperTransactionTypeUsed.isReadonly()) {
        this.immediateTransaction = this.immediateSession.beginTransaction();
      }
    }
  }

  /** hibernate session object of parent if nested, or null */
  private HibernateSession                          parentSession                             = null;

  /**
   * hibernate session object can be accessed by user, if there is a parent,
   * this will be null.
   */
  private Session                                   immediateSession                          = null;

  /**
   * if read/write, this will exist, though the user cant access directly. if
   * there is a parent, this will ne null
   */
  private Transaction                               immediateTransaction                      = null;

  /**
   * the transaction type this was setup as. note, if the type is new, then it
   * might change from what it was declared as...
   */
  private GrouperTransactionType                  immediateGrouperTransactionTypeUsed     = null;

  /**
   * the transaction type this was setup as. this is the one declared in
   * callback
   */
  @SuppressWarnings("unused")
  private GrouperTransactionType                  immediateGrouperTransactionTypeDeclared = null;

  /**
   * store the hib2 connection in thread local so other classes can get it, e.g.
   * for blobs
   */
  private static ThreadLocal<Set<HibernateSession>> staticSessions                            = new ThreadLocal<Set<HibernateSession>>();

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
  private static void removeStaticHibernateSession(
      HibernateSession hibernateSession) {
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
  private static void addStaticHibernateSession(
      HibernateSession hibernateSession) {
    Set<HibernateSession> hibSet = getHibernateSessionSet();
    hibSet.add(hibernateSession);
    // cant have more than 15, something is wrong
    if (hibSet.size() > 15) {
      hibSet.clear();
      throw new RuntimeException(
          "There is probably a problem that there are 10 nested new HibernateSessions called!");
    }
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
   * call this to send a callback for the hibernate session object. cant use
   * inverse of control for this since it runs it
   * 
   * @param grouperTransactionType
   *          is enum of how the transaction should work.
   * @param hibernateHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws GrouperDAOException
   *           if there is a problem
   */
  public static Object callbackHibernateSession(
      GrouperTransactionType grouperTransactionType,
      HibernateHandler hibernateHandler) throws GrouperDAOException {
    Object ret = null;
    HibernateSession hibernateSession = staticHibernateSession();

    try {
      hibernateSession = new HibernateSession(hibernateSession,
          grouperTransactionType);

      ret = hibernateHandler.callback(hibernateSession);

      // maybe we didnt commit. if new session, and no exception, and not
      // committed or rolledback,
      // then commit.
      if (hibernateSession.isNewHibernateSession()
          && !hibernateSession.isReadonly()) {
        if (hibernateSession.immediateTransaction.isActive()) {
          hibernateSession.immediateTransaction.commit();
        }
      }

    } catch (Throwable e) {
      // maybe we didnt rollback. if new session, and exception, and not
      // committed or rolledback,
      // then rollback.
      if (hibernateSession.isNewHibernateSession()
          && !hibernateSession.isReadonly()) {
        if (hibernateSession.immediateTransaction.isActive()) {
          hibernateSession.immediateTransaction.rollback();
        }
      }
      String errorString = "Problem in HibernateSession: " + hibernateSession;
      // rethrow
      if (e instanceof GrouperDAOException) {
        LOG.error(errorString);
        throw (GrouperDAOException) e;
      }
      // if hibernate exception, repackage
      if (e instanceof HibernateException) {
        throw new GrouperDAOException(errorString, e);
      }
      // if runtime, then rethrow
      if (e instanceof RuntimeException) {
        // note, it would be nice to get this in the exception
        LOG.error(errorString);
        throw (RuntimeException) e;
      }
      // if exception and not handled, convert to GrouperDaoException
      throw new GrouperDAOException(errorString, e);

    } finally {

      // take out of threadlocal if supposed to
      if (hibernateSession.isNewHibernateSession()) {
        // take out of threadlocal stack
        removeStaticHibernateSession(hibernateSession);
        // we should close the hibernate session if we opened it, and if not
        // already closed
        // transaction is already closed...
        closeSessionIfNotClosed(hibernateSession.immediateSession);
      }
    }
    return ret;

  }

  /**
   * do a hql query with proper error handling and in an enclosing transaction
   * (if applicable), or a new one if not
   * @return the class
   */
  public static ByHqlStatic byHqlStatic(){
    return new ByHqlStatic();
  }

  /**
   * do an object operation 
   * with proper error handling and in an enclosing transaction
   * (if applicable), or a new one if not
   * @return the class
   */
  public static ByObjectStatic byObjectStatic(){
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
    return "HibernateSession: isNew: " + this.isNewHibernateSession()
        + ", isReadonly: " + this.isReadonly() + ", grouperTransactionType: "
        + this.getGrouperTransactionType();
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
   * see if this is a new hibernate session
   * 
   * @return the newHibernateSession
   */
  public boolean isNewHibernateSession() {
    // if no parent, then it is new
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
    switch (grouperCommitType) {
    case COMMIT_IF_NEW_TRANSACTION:
      if (this.isNewHibernateSession()) {
        this.activeHibernateSession().immediateTransaction.commit();
        return true;
      }
      break;
    case COMMIT_NOW:
      this.activeHibernateSession().immediateTransaction.commit();
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
    return this.activeHibernateSession().immediateTransaction == null ?
        false : this.activeHibernateSession().immediateTransaction.isActive();
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
      this.activeHibernateSession().immediateTransaction.rollback();
      return true;
    }
    return false;
  }
  
}

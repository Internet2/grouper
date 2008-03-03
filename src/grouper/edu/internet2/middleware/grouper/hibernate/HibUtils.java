/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import java.sql.PreparedStatement;
import java.util.List;

import org.hibernate.Session;

/**
 * @author mchyzer
 *
 */
public class HibUtils {

  /**
   * close a prepared statement
   * @param preparedStatement
   */
  public static void closeQuietly(PreparedStatement preparedStatement) {
    try {
      if (preparedStatement != null) {
        preparedStatement.close();
      }
    } catch (Exception e) {
      //forget about it
    }
  }
  
  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession, can be null if not known
   * @param session from hibernate
   * @param object to evict that was just retrieved
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession, Session session, 
      Object object, boolean onlyEvictIfNotNew) {
    if (object instanceof List) {
      HibUtils.evict(hibernateSession, session, (List)object, onlyEvictIfNotNew);
      return;
    }
    
    //dont worry about it if new and only evicting if not new
    if (hibernateSession != null && hibernateSession.isNewHibernateSession() && onlyEvictIfNotNew) {
      return;
    }
    
    //not sure it could ever be null...
    if (object != null) {
      session.evict(object);
    }
  }
  
  /**
   * <pre>
   * evict a list of objects from hibernate.  do this always for two reasons:
   * 1. If you edit an object that is in the hibernate session, and commit, it will
   * commit those changes magically.  Only objects called session.save(obj) or 
   * update etc should be committed
   * 2. If you select an object, then try to store it back (but have a different
   * reference, e.g. if the DTO went through it, then you will get an exception:
   * "a different object with the same identifier value was already associated with the session"
   * </pre>
   * @param hibernateSession grouper hibernateSession
   * @param session from hibernate
   * @param list of objects from hibernate to evict
   * @param onlyEvictIfNotNew true to only evict if this is a nested tx
   */
  public static void evict(HibernateSession hibernateSession, Session session, 
      List<Object> list, boolean onlyEvictIfNotNew) {
    if (list == null) {
      return;
    }
    for (Object object : list) {
      evict(hibernateSession, session, object, onlyEvictIfNotNew);
    }
  }
  
}
